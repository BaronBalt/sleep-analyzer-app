package hu.vmiklos.plees_tracker.ui.audio

import androidx.lifecycle.ViewModel
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository

class AudioViewModel(
    private val sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository
): ViewModel() {

}