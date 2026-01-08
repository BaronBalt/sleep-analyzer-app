package hu.vmiklos.plees_tracker.ui.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt

class AudioViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    // Exposed smoothed dB value
    private val _noiseDb = MutableLiveData<Double>()
    val noiseDb: LiveData<Double> = _noiseDb

    private val _sleepFriendly = MutableLiveData<Boolean>()
    val sleepFriendly: LiveData<Boolean> = _sleepFriendly
    private val FRIENDLY_ENTER = 40.0  // roughly quiet room
    private val FRIENDLY_EXIT = 60.0   // getting loud
    private var lastFriendly = true


    // Exponential Moving Average (EMA)
    private var smoothedDb: Double? = null
    private val smoothingFactor = 0.1 // Maybe add this as a setting in the future 0.1/0.2/0.3

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (isRecording) return

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        viewModelScope.launch(Dispatchers.Default) {
            val buffer = ShortArray(bufferSize)

            while (isActive && isRecording) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    val db = calculateDb(buffer, readSize)
                    val smoothed = smooth(db)

                    Log.d("AudioDebug", "raw=$db smoothed=$smoothed")

                    _noiseDb.postValue(smoothed)
                    updateSleepFriendly(smoothed)
                }
            }
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        smoothedDb = null
    }

    private fun smooth(currentDb: Double): Double {
        smoothedDb = if (smoothedDb == null) {
            currentDb
        } else {
            smoothedDb!! * (1 - smoothingFactor) + currentDb * smoothingFactor
        }
        return smoothedDb!!
    }

    private fun calculateDb(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            val sample = buffer[i].toDouble()
            sum += sample * sample
        }

        val rms = sqrt(sum / readSize)

        // Converting to 0-100 instead of the negative numbers that are read from mic
        val db = 20 * log10(maxOf(rms, 1.0) / Short.MAX_VALUE)
        return (db + 100).coerceIn(0.0, 100.0)
    }

    private fun updateSleepFriendly(db: Double) {
        lastFriendly = when {
            lastFriendly && db > FRIENDLY_EXIT -> false
            !lastFriendly && db < FRIENDLY_ENTER -> true
            else -> lastFriendly
        }
        _sleepFriendly.postValue(lastFriendly)
    }

    override fun onCleared() {
        stopRecording()
        super.onCleared()
    }
}
