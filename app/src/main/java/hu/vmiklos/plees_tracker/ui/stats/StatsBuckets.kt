package hu.vmiklos.plees_tracker.ui.stats

data class StatsBuckets(
    val lastWeek: StatsUiModel,
    val lastTwoWeeks: StatsUiModel,
    val lastMonth: StatsUiModel,
    val lastYear: StatsUiModel,
    val allTime: StatsUiModel
)