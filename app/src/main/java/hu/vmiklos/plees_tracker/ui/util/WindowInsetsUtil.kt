package hu.vmiklos.plees_tracker.ui.util

import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import hu.vmiklos.plees_tracker.R

object WindowInsetsUtil {
    fun applyEdgeToEdge(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val rootView = activity.findViewById<View>(R.id.root)
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
                WindowInsetsCompat.CONSUMED
            }

            val nightMask =
                activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightMask == Configuration.UI_MODE_NIGHT_NO) {
                WindowCompat.getInsetsController(
                    activity.window,
                    activity.window.decorView
                ).isAppearanceLightStatusBars = true
            }
        }
    }

    fun handleWindowInsets(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Handle edge-to-edge mode
            val rootView = activity.findViewById<View>(R.id.root)
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
                WindowInsetsCompat.CONSUMED
            }
            val resources = activity.resources
            val nightMask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightMask == Configuration.UI_MODE_NIGHT_NO) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = true
            }
        }
    }
}