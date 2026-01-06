package hu.vmiklos.plees_tracker.ui.sleep

import android.content.ContentResolver
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.model.Sleep
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter
import kotlinx.coroutines.launch

/**
 * This is the view model of SleepActivity, providing coroutine scopes.
 */
class SleepViewModel(
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository,
    private val formatter: TimeFormatter
) : ViewModel() {

    // Helper to get sleep data for the UI
    suspend fun getSleep(sid: Int): Sleep {
        return sleepRepository.getSleepById(sid)
    }

    fun isCompactView() = preferencesRepository.compactView()

    fun updateSleep(sleep: Sleep) {
        viewModelScope.launch {
            sleepRepository.updateSleep(sleep)
            // Trigger auto-backup if enabled
            // Use the UseCase directly or via Repository if refactored
            // importExportUseCases.performAutoBackup(context, cr, formatter)
        }
    }

    // Move Dialog logic to Activity, only keep the save logic here
    fun onDateTimeChanged(sleep: Sleep, context: Context) {
        if (sleep.start < sleep.stop) {
            updateSleep(sleep)
        } else {
            Toast.makeText(context, R.string.negative_duration, Toast.LENGTH_SHORT).show()
        }
    }
}