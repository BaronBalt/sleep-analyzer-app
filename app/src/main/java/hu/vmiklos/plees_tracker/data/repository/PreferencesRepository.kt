package hu.vmiklos.plees_tracker.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import hu.vmiklos.plees_tracker.data.local.liveDataBoolean
import hu.vmiklos.plees_tracker.data.local.liveDataLong
import hu.vmiklos.plees_tracker.domain.model.StatFunction

class PreferencesRepository(private val preferences: SharedPreferences) {
    fun compactView(): Boolean {
        return preferences.getBoolean("compact_view", true)
    }

    fun prettyBackup(): Boolean {
        return preferences.getBoolean("pretty_boolean", false)
    }

    fun ignoreEmptyDays() = preferences.getBoolean("ignore_empty_days", true)

    fun statFunction(): StatFunction {
        return if (preferences.getBoolean("use_median", false)) {
            StatFunction.MEDIAN
        } else {
            StatFunction.AVERAGE
        }
    }

    // Logic for the 'active' tracking session
    fun getStartTimestamp(): Long = preferences.getLong("start", 0L)

    fun setStartTimestamp(timestamp: Long) {
        preferences.edit {
            putLong("start", timestamp)
        }
    }

    fun startTimeStampLive(): LiveData<Long> =
        preferences.liveDataLong("start", 0L)

    fun getStartDelay(): Int {
        val startDelayStr = preferences.getString("sleep_start_delta", "0") ?: "0"
        return startDelayStr.toIntOrNull() ?: 0
    }

    fun getBedtimeHour(): Int = getPreferencesToken("bedtime", 0, 22)
    fun getBedtimeMinute(): Int = getPreferencesToken("bedtime", 1, 0)
    fun getBedTime(): String? = preferences.getString("bedtime", "")
    fun getWakeup(): String? = preferences.getString("wakeup", "")
    fun getWakeupHour(): Int = getPreferencesToken("wakeup", 0, 7)
    fun getWakeupMinute(): Int = getPreferencesToken("wakeup", 1, 0)

    private fun getPreferencesToken(name: String, index: Int, default: Int): Int {
        val pref = preferences.getString(name, "") ?: return default
        val tokens = pref.split(":")
        return if (tokens.size == 2) tokens[index].toIntOrNull() ?: default else default
    }

    fun getAutoBackupPath(): String? {
        return preferences.getString("auto_backup_path", null)
    }

    fun isAutoBackupEnabled(): Boolean {
        return preferences.getBoolean("auto_backup_enabled", false)
    }

    fun useMedianLive(): LiveData<Boolean?> {
        return preferences.liveDataBoolean("use_median", false)
    }
}