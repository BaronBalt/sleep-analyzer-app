package hu.vmiklos.plees_tracker.domain.usecase

import hu.vmiklos.plees_tracker.domain.model.Sleep
import hu.vmiklos.plees_tracker.domain.model.StatFunction
import hu.vmiklos.plees_tracker.util.median
import java.util.Calendar
import kotlin.math.roundToLong

class StatsUseCases {
    fun countSleeps(sleeps: List<Sleep>): Int = sleeps.size
    fun averageDuration(sleeps: List<Sleep>): Long {
        if (sleeps.isEmpty()) return 0L
        val totalSeconds = sleeps.sumOf { (it.stop - it.start) / 1000 }
        return totalSeconds / sleeps.size
    }
    fun dailyDuration(
        sleeps: List<Sleep>,
        ignoreEmptyDays: Boolean,
        statFunction: StatFunction
    ): Long {
        // Day -> sum (in seconds) map.
        val sums = HashMap<Long, Long>()
        var minKey: Long = Long.MAX_VALUE
        var maxKey: Long = 0
        for (sleep in sleeps) {
            var diff = sleep.stop - sleep.start
            diff /= 1000

            // Calculate stop day
            val stopDate = Calendar.getInstance()
            stopDate.timeInMillis = sleep.stop

            val day = Calendar.getInstance()
            day.timeInMillis = 0
            val startYear = stopDate.get(Calendar.YEAR)
            day.set(Calendar.YEAR, startYear)
            val startMonth = stopDate.get(Calendar.MONTH)
            day.set(Calendar.MONTH, startMonth)
            val startDay = stopDate.get(Calendar.DAY_OF_MONTH)
            day.set(Calendar.DAY_OF_MONTH, startDay)
            val key = day.timeInMillis
            minKey = minOf(minKey, key)
            maxKey = maxOf(maxKey, key)

            val sum = sums[key]
            if (sum != null) {
                sums[key] = sum + diff
            } else {
                sums[key] = diff
            }
        }

        if (sums.isEmpty()) {
            return 0L
        }

        // Now determine the number of covered days. This is usually just the number of keys, but it
        // can be more, in case a whole 24h period was left out.
        val msPerDay = 86400 * 1000
        var count = (maxKey - minKey) / msPerDay + 1
        if (ignoreEmptyDays) {
            count = sums.keys.size.toLong()
        }

        // Return the raw duration as a Long
        return when (statFunction) {
            StatFunction.AVERAGE -> sums.values.sum() / count
            StatFunction.MEDIAN -> median(sums.values.toLongArray()).roundToLong()
        }
    }

    fun filterAfter(sleeps: List<Sleep>, afterMillis: Long): List<Sleep> {
        return sleeps.filter { it.stop > afterMillis }
    }
}