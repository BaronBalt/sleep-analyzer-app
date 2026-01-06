package hu.vmiklos.plees_tracker.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import hu.vmiklos.plees_tracker.data.local.sleep.SleepDao
import hu.vmiklos.plees_tracker.domain.model.Sleep
import java.util.Date

class SleepRepository(
    private val sleepDao: SleepDao,
    private val preferencesRepository: PreferencesRepository
) {
    fun getActiveSessionStart(): Long? {
        val start = preferencesRepository.getStartTimestamp()
        return if (start > 0) start else null
    }

    suspend fun storeSleep(start: Long, stop: Long) {
        // Calculate the delay (moved from DataModel.getStartDelay)
        val startDelayMinutes = preferencesRepository.getStartDelay()
        val startDelayMS = startDelayMinutes * 60 * 1000L

        // Logic for adjusting start time based on delay
        val finalStart = if (start + startDelayMS > stop) {
            stop
        } else {
            start + startDelayMS
        }

        // Create the Sleep entity and save to DB
        val sleep = Sleep().apply {
            this.start = finalStart
            this.stop = stop
        }
        sleepDao.insert(sleep)

        // Clear the start timestamp from preferences
        preferencesRepository.setStartTimestamp(null)
    }

    suspend fun getSleepById(sid: Int): Sleep = sleepDao.getById(sid)

    fun getSleepsAfterLive(after: Date): LiveData<List<Sleep>> {
        // We observe the preference LiveData from our preferencesRepository
        return preferencesRepository.useMedianLive().switchMap { _ ->
            // Whenever "use_median" changes, we re-run the query
            sleepDao.getAfterLive(after.time)
        }
    }

    suspend fun getAllSleeps() = sleepDao.getAll()

    suspend fun insertSleep(sleep: Sleep) = sleepDao.insert(sleep)

    suspend fun insertSleeps(sleeps: List<Sleep>) = sleepDao.insert(sleeps)

    suspend fun updateSleep(sleep: Sleep) = sleepDao.update(sleep)

    suspend fun deleteSleep(sleep: Sleep) = sleepDao.delete(sleep)

    suspend fun deleteAllSleep() = sleepDao.deleteAll()

    suspend fun backupSleeps() {
        val startTime = preferencesRepository.getStartTimestamp()
        val stopTime = System.currentTimeMillis()

        if (startTime > 0) {
            val startDelayMS = preferencesRepository.getStartDelay() * 60 * 1000
            val adjustedStart = if (startTime + startDelayMS > stopTime) stopTime else startTime + startDelayMS

            val sleep = Sleep()
            sleep.start = adjustedStart
            sleep.stop = stopTime

            sleepDao.insert(sleep)

            preferencesRepository.setStartTimestamp(null)
        }
    }

    fun sleepsLive() = sleepDao.getAllLive()
}