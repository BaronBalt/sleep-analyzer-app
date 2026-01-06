package hu.vmiklos.plees_tracker.system.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.data.repository.PreferencesRepository
import hu.vmiklos.plees_tracker.domain.model.DataModel
import hu.vmiklos.plees_tracker.ui.main.MainActivity
import hu.vmiklos.plees_tracker.ui.util.TimeFormatter

/**
 * A foreground service that just keeps the app alive, so the state is not lost
 * while tracking is on.
 */
class MainService(
    private val preferencesRepository: PreferencesRepository,
    private val formatter: TimeFormatter
) : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Avoid unwanted vibration.
            channel.vibrationPattern = longArrayOf(0)
            channel.enableVibration(true)

            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        var contentText = ""
        DataModel.start?.let { start ->
            contentText = String.format(
                getString(R.string.sleeping_since),
                formatter.formatTimestamp(start, preferencesRepository.compactView())
            )
        }
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_CODE, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "Notification"
        private const val NOTIFICATION_CODE = 1
    }
}