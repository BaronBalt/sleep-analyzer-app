package hu.vmiklos.plees_tracker.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.data.repository.SleepRepository
import hu.vmiklos.plees_tracker.domain.model.Sleep
import hu.vmiklos.plees_tracker.domain.usecase.StatsUseCases
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter
import java.util.Calendar

class StatsViewModel(
    sleepRepository: SleepRepository,
    private val preferencesRepository: PreferencesRepository,
    private val statsUseCases: StatsUseCases,
    private val formatter: TimeFormatter
) : ViewModel() {

    /**
     * Public state observed by the Activity
     */
    val stats: LiveData<StatsBuckets> =
        sleepRepository.sleepsLive().map { sleeps ->
            buildBuckets(sleeps)
        }

    private fun buildBuckets(sleeps: List<Sleep>): StatsBuckets {
        fun after(days: Int): List<Sleep> {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -days)
            val afterMillis = cal.timeInMillis
            return sleeps.filter { it.stop > afterMillis }
        }

        return StatsBuckets(
            lastWeek = buildUiModel(after(7)),
            lastTwoWeeks = buildUiModel(after(14)),
            lastMonth = buildUiModel(after(30)),
            lastYear = buildUiModel(after(365)),
            allTime = buildUiModel(sleeps)
        )
    }

    private fun buildUiModel(sleeps: List<Sleep>): StatsUiModel {
        val compact = preferencesRepository.compactView()
        val ignoreEmpty = preferencesRepository.ignoreEmptyDays()
        val statFunction = preferencesRepository.statFunction()

        val count = statsUseCases.countSleeps(sleeps)
        val avgSeconds = statsUseCases.averageDuration(sleeps)
        val dailySeconds =
            statsUseCases.dailyDuration(sleeps, ignoreEmpty, statFunction)

        return StatsUiModel(
            sleepCount = count.toString(),
            averageDuration = formatter.formatDuration(avgSeconds, compact),
            dailyDuration = formatter.formatDuration(dailySeconds, compact)
        )
    }
}
