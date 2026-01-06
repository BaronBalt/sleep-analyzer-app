package hu.vmiklos.plees_tracker.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    fun formatDuration(seconds: Long, compactView: Boolean): String {
        if (compactView) {
            return String.format(
                Locale.getDefault(), "%d:%02d",
                seconds / 3600, seconds % 3600 / 60
            )
        }

        return String.format(
            Locale.getDefault(), "%d:%02d:%02d",
            seconds / 3600, seconds % 3600 / 60,
            seconds % 60
        )
    }

    fun formatTimestamp(date: Date, compactView: Boolean): String {
        val sdf = if (compactView) {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss XXX", Locale.getDefault())
        }
        return sdf.format(date)
    }

    fun formatDateTime(date: Date, asTime: Boolean, compactView: Boolean): String {
        val sdf = if (asTime) {
            if (compactView) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            }
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        }
        return sdf.format(date)
    }
}