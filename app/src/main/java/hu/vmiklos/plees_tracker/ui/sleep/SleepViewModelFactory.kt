package hu.vmiklos.plees_tracker.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter

class SleepViewModelFactory(
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository,
    private val formatter: TimeFormatter
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            return SleepViewModel(
                sleepRepository,
                preferencesRepository,
                formatter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}