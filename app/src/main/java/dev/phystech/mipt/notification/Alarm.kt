package dev.phystech.mipt.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import dev.phystech.mipt.Application
import edu.phystech.iag.kaiumov.shedule.utils.DataUtils
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import dev.phystech.mipt.R
import dev.phystech.mipt.models.Schedule
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerItemAlarmModel
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.utils.Keys
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// https://developer.android.com/training/scheduling/alarms

object Alarm {

    private const val INTERVAL_WEEK = 7 * 24 * 3600 * 1000L
    private var firstOddWeekDate: Date? = null
    
    class AlarmNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val notificationEnabled = preferences.getBoolean(context.resources.getString(R.string.pref_notification_enabled_key), false)
            // display
            if (notificationEnabled) {
                (intent.getBundleExtra(Keys.ITEM)
                    ?.getSerializable(Keys.ITEM) as? SchedulerItemAlarmModel)
                    ?.let { scheduleItem ->
                        Notificator.showNotification(context, scheduleItem)
                    }
            }
        }
    }

    // Cancel previous notifications
    private fun resetAlarm(context: Context, alarmManager: AlarmManager, preferences: SharedPreferences) {
        val notSize = preferences.getInt(context.resources.getString(R.string.pref_notification_size_key), 0)
        for (i in 0 until notSize) {
            val intent = Intent(context, AlarmNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.cancel(pendingIntent)
        }
    }

    // Cancel previous notifications
    private fun resetAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        resetAlarm(context, alarmManager, preferences)
    }

    fun schedule(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val minutesBefore = preferences.getString(context.resources.getString(R.string.pref_notification_before_key), "5")!!.toInt()
        val items = schedule.timetable.values.firstOrNull() ?: return
        // Cancel previous alarms
        resetAlarm(context, alarmManager, preferences)
        // Save new notification queue size
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putInt(context.resources.getString(R.string.pref_notification_size_key), items.size)
        editor.apply()

        val even = getDateOdd(Date())

        // Create new notifications queue
        for (i in 0 until items.size) {
            val itemOrigin = items[i]
            val item: SchedulerItemAlarmModel = SchedulerItemAlarmModel.fromSchedulerItem(itemOrigin)

            val intent = Intent(context, AlarmNotificationReceiver::class.java)
            val bundle = Bundle()
            bundle.putSerializable(Keys.ITEM, item)
            intent.putExtra(Keys.ITEM, bundle)
            val pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE)
            val time = TimeUtils.getCalendarTime(item.day, item.startTime)
            val now = Calendar.getInstance()

            if (even != null) {
                val oddValue = if (even == 0) 1 else 2
                if (itemOrigin.evenodd <= 0) {
                    if (time.timeInMillis - now.timeInMillis < 0) {
                        time.add(Calendar.DAY_OF_YEAR, 7)
                    }
                } else {
                    if (oddValue == itemOrigin.evenodd) {
                        if (time.timeInMillis - now.timeInMillis < 0) {
                            time.add(Calendar.DAY_OF_YEAR, 14)
                        }
                    } else {
                        time.add(Calendar.DAY_OF_YEAR, 7)
                    }
                }
            } else {
                if (time.timeInMillis - now.timeInMillis < 0) {
                    time.add(Calendar.DAY_OF_YEAR, 7)
                }
            }

            time.add(Calendar.MINUTE, -minutesBefore)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis, INTERVAL_WEEK, pendingIntent)
        }

        // For test notification
//        items.firstOrNull()?.let { item ->
//            val intent = Intent(context, AlarmNotificationReceiver::class.java)
//            val bundle = Bundle()
//            bundle.putSerializable(Keys.ITEM, item)
//            intent.putExtra(Keys.ITEM, bundle)
//            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            val now = Calendar.getInstance()
//
//            now.add(Calendar.SECOND, 5)
//
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, now.timeInMillis, INTERVAL_WEEK, pendingIntent)
//        }
    }

//    fun schedule(context: Context, schedule: Schedule) {
//        val key = DataUtils.loadNotificationKey(context)
//        if (key == null) {
//            resetAlarm(context)
//            return
//        }
//        schedule(context, schedule, key)
//    }
    
    fun schedule(context: Context) {
//        val key = DataUtils.loadNotificationKey(context)
//        if (key == null) {
//            resetAlarm(context)
////            return
//        }
        firstOddWeekDate = DataUtils.loadFirstOddWeekDate(context)?.let { Date(it) }
        val schedule = DataUtils.loadSchedule(context) ?: return
        schedule(context, schedule)
    }

    fun getDateOdd(date: Date): Int? {
        firstOddWeekDate?.let { firstOddWeekDate ->
            val calendar = GregorianCalendar()
            calendar.time = firstOddWeekDate
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)

            val date1 = calendar.time

            calendar.time = date
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)

            val date2 = calendar.time

            val between = date2.time - date1.time
            val betweenDays = TimeUnit.MILLISECONDS.toDays(between) / 7 % 2
            return betweenDays.toInt()
        }

        return null
    }
}