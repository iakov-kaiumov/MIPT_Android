package dev.phystech.mipt.utils

import android.app.Application
import android.os.AsyncTask
import android.preference.PreferenceManager
import edu.phystech.iag.kaiumov.shedule.utils.DataUtils
import edu.phystech.iag.kaiumov.shedule.utils.ThemeHelper
import dev.phystech.mipt.R
import dev.phystech.mipt.models.Schedule
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.notification.Alarm
import dev.phystech.mipt.notification.Notificator


open class ScheduleApp : Application() {

    private var schedule: Schedule? = null
    val timetable: HashMap<String, ArrayList<ScheduleItem>>?
        get() = schedule?.timetable

    override fun onCreate() {
        super.onCreate()
        // Load schedule from memory
        schedule = DataUtils.loadSchedule(applicationContext)
        // If there is new version in assets, update schedule in memory

        Notificator.buildNotificationChannel(this)
        Alarm.schedule(this)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = sharedPreferences.getString(getString(R.string.pref_theme_key), ThemeHelper.DEFAULT_MODE)
        ThemeHelper.applyTheme(themePref!!)
    }


    fun updateTimeTable(timetable: HashMap<String, ArrayList<ScheduleItem>>) {
        schedule?.timetable = timetable
        AsyncTask.execute {
            schedule?.let { schedule ->
                DataUtils.saveSchedule(applicationContext, schedule)
                Alarm.schedule(applicationContext, schedule)
            }

        }
    }

    fun createNewGroup(name: String) {
//        schedule?.timetable?.set(name, ArrayList())
//        updateTimeTable(timetable)
    }
}