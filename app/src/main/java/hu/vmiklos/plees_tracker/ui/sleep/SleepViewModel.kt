package hu.vmiklos.plees_tracker.ui.sleep

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.model.Sleep
import kotlinx.coroutines.launch

/**
 * This is the view model of SleepActivity, providing coroutine scopes.
 */
class SleepViewModel(
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    suspend fun getSleep(sid: Int): Sleep = sleepRepository.getSleepById(sid)

    fun isCompactView() = preferencesRepository.compactView()

    fun updateSleep(sleep: Sleep) {
        viewModelScope.launch {
            sleepRepository.updateSleep(sleep)
            // Trigger auto-backup if enabled
            // Use the UseCase directly or via Repository if refactored
            // importExportUseCases.performAutoBackup(context, cr, formatter)
        }
    }

    fun onDateTimeChanged(sleep: Sleep, context: Context) {
        if (sleep.start < sleep.stop) {
            updateSleep(sleep)
        } else {
            Toast.makeText(context, R.string.negative_duration, Toast.LENGTH_SHORT).show()
        }
    }
}