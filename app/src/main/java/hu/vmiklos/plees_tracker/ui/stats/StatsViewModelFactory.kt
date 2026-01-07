package hu.vmiklos.plees_tracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.usecase.StatsUseCases
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter

class StatsViewModelFactory(
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository,
    private val statsUseCases: StatsUseCases,
    private val formatter: TimeFormatter
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(
                sleepRepository,
                preferencesRepository,
                statsUseCases,
                formatter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
