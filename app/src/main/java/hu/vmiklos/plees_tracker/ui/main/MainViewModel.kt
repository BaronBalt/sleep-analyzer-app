package hu.vmiklos.plees_tracker.ui.main

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.local.liveData
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.model.Sleep
import hu.vmiklos.plees_tracker.domain.usecase.ImportExportUseCases
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * This is the view model of MainActivity, providing coroutine scopes.
 */
class MainViewModel(
    application: Application,
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository,
    private val importExportUseCases: ImportExportUseCases,
    private val formatter: TimeFormatter
) : AndroidViewModel(application) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)

    private val _uiEvents = Channel<UiEvent>()

    val uiEvents = _uiEvents.receiveAsFlow()

    fun isTrackingActive(): Boolean {
        // Check if the repository has a saved start time
        return preferencesRepository.getStartTimestamp() != 0L
    }

    val durationSleepsLive: LiveData<List<Sleep>> =
        preferences.liveData("dashboard_duration", "0").switchMap { durationStr ->
            val duration = durationStr?.toInt() ?: 0
            val date = if (duration == 0) {
                Date(0)
            } else {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DATE, duration)
                cal.time
            }
            sleepRepository.getSleepsAfterLive(date)
        }

    fun startSleep() {
        val currentTime = System.currentTimeMillis()
        preferencesRepository.setStartTimestamp(currentTime)
    }

    fun stopSleep(context: Context, cr: ContentResolver) {
        viewModelScope.launch {
            val startTime = preferencesRepository.getStartTimestamp()
            val stopTime = System.currentTimeMillis()

            if (startTime != 0L) {
                // 1. Save to DB
                sleepRepository.storeSleep(startTime, stopTime)

                // 2. Reset state
                preferencesRepository.setStartTimestamp(0L)

                // 3. Trigger Backup via Use Case
                val result = importExportUseCases.performAutoBackup(context, cr, formatter)

                // 4. Handle errors if the backup failed
                result.onFailure { error ->
                    _uiEvents.send(UiEvent.ShowError("Auto-backup failed: ${error.message}"))
                }
            }
        }
    }

    fun exportDataToFile(cr: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            val result = importExportUseCases.exportToFile(cr, uri, formatter)
            result.onSuccess {
                _uiEvents.send(UiEvent.ShowToast(R.string.export_success))
            }.onFailure { error ->
                _uiEvents.send(UiEvent.ShowError(error.message ?: "Unknown error"))
            }
        }
    }

    fun importDataFromCalendar(context: Context, calendarId: String) {
        viewModelScope.launch {
            val result = importExportUseCases.importFromCalendar(context, calendarId)
            result.onSuccess { count ->
                _uiEvents.send(UiEvent.ImportSuccess(count))
            }.onFailure { error ->
                _uiEvents.send(UiEvent.ShowError(error.message ?: "Unknown error"))
            }
        }
    }

    fun exportDataToCalendar(context: Context, calendarId: String) {
        viewModelScope.launch {
            val result = importExportUseCases.exportToCalendar(context, calendarId)
            result.onSuccess {
                _uiEvents.send(UiEvent.ShowToast(R.string.export_success))
            }.onFailure { error ->
                _uiEvents.send(UiEvent.ShowError(error.message ?: "Unknown error"))
            }
        }
    }

    fun importData(cr: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            val result = importExportUseCases.importCsv(cr, uri)
            result.onSuccess { count ->
                _uiEvents.send(UiEvent.ImportSuccess(count))
            }.onFailure { error ->
                _uiEvents.send(UiEvent.ShowError(error.message ?: "Unknown error"))
            }
        }
    }

    fun insertSleep(sleep: Sleep) {
        viewModelScope.launch {
            sleepRepository.insertSleep(sleep)
        }
    }

    fun deleteSleep(sleep: Sleep) {
        viewModelScope.launch {
            sleepRepository.deleteSleep(sleep)
            sleepRepository.backupSleeps()
        }
    }

    fun deleteAllSleep() {
        viewModelScope.launch {
            sleepRepository.deleteAllSleep()
        }
    }
}