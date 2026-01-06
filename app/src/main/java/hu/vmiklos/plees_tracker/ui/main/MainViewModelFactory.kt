package hu.vmiklos.plees_tracker.ui.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.usecase.ImportExportUseCases
import hu.vmiklos.plees_tracker.domain.usecase.StatsUseCases
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter

class MainViewModelFactory(
    private val application: Application,
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository,
    private val importExportUseCases: ImportExportUseCases,
    private val statsUseCases: StatsUseCases,
    private val formatter: TimeFormatter
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                application,
                sleepRepository,
                preferencesRepository,
                importExportUseCases,
                formatter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}