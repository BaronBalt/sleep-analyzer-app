package hu.vmiklos.plees_tracker.ui.stats

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import hu.vmiklos.plees_tracker.R
import hu.vmiklos.plees_tracker.ui.util.WindowInsetsUtil

class StatsActivity : AppCompatActivity() {

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        WindowInsetsUtil.applyEdgeToEdge(this)

        title = getString(R.string.stats)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.stats.observe(this) { buckets ->
            val fragments = supportFragmentManager

            populateFragment(
                fragments.findFragmentById(R.id.last_week_body),
                buckets.lastWeek
            )
            populateFragment(
                fragments.findFragmentById(R.id.last_two_weeks_body),
                buckets.lastTwoWeeks
            )
            populateFragment(
                fragments.findFragmentById(R.id.last_month_body),
                buckets.lastMonth
            )
            populateFragment(
                fragments.findFragmentById(R.id.last_year_body),
                buckets.lastYear
            )
            populateFragment(
                fragments.findFragmentById(R.id.all_time_body),
                buckets.allTime
            )
        }
    }

    private fun populateFragment(
        fragment: Fragment?,
        uiModel: StatsUiModel
    ) {
        val view = fragment?.view ?: return

        view.findViewById<TextView>(R.id.fragment_stats_sleeps)
            .text = uiModel.sleepCount

        view.findViewById<TextView>(R.id.fragment_stats_average)
            .text = uiModel.averageDuration

        view.findViewById<TextView>(R.id.fragment_stats_daily)
            .text = uiModel.dailyDuration
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
