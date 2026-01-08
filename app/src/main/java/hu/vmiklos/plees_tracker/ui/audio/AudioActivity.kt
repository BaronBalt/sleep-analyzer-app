package hu.vmiklos.plees_tracker.ui.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.ui.util.WindowInsetsUtil.handleWindowInsets
import java.util.Locale

class AudioActivity : AppCompatActivity() {

    private lateinit var viewModel: AudioViewModel

    private lateinit var dbText: TextView
    private lateinit var statusText: TextView
    private lateinit var toggleButton: ToggleButton

    @Suppress("MissingPermission")
    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.startRecording()
            } else {
                toggleButton.isChecked = false
                statusText.text = getString(R.string.mic_permission_required)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        handleWindowInsets(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.audio_title)

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val preferencesRepository = PreferencesRepository(preferences)

        val factory = AudioViewModelFactory(preferencesRepository)
        viewModel = ViewModelProvider(this, factory)[AudioViewModel::class.java]

        dbText = findViewById(R.id.current_db_value)
        statusText = findViewById(R.id.sleep_status_value)
        toggleButton = findViewById(R.id.start_recording_button)

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startRecordingWithPermission()
            } else {
                viewModel.stopRecording()
            }
        }

        viewModel.noiseDb.observe(this) { db ->
            dbText.text = String.format(Locale.getDefault(), "%.1f dB", db)
        }

        viewModel.sleepFriendly.observe(this) { isFriendly ->
            statusText.text = if (isFriendly) {
                getString(R.string.sleep_friendly)
            } else {
                getString(R.string.not_sleep_friendly)
            }
        }
    }

    private fun startRecordingWithPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startRecording()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopRecording()
        toggleButton.isChecked = false
    }
}
