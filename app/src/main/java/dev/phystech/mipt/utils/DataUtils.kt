package edu.phystech.iag.kaiumov.shedule.utils

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.IOUtils
import dev.phystech.mipt.R
import dev.phystech.mipt.models.Schedule
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.models.api.SchedulersDataResponseModel
import java.io.FileNotFoundException


object DataUtils {
    private const val ENCODING = "UTF-8"
    private const val DELIMITER = "|"
    private const val SCHEDULE_PATH = "schedule.json"
    private const val FIRST_ODD_WEEK_DATE_PATH = "firstOddWeekDate.json"
    private const val PROF_PATH = "prof.json"

    internal fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    internal fun loadProfessorsList(context: Context) : ArrayList<String> {
        return Gson().fromJson(
                IOUtils.toString(context.assets.open(PROF_PATH), ENCODING),
                object : TypeToken<ArrayList<String>>() {}.type
        )
    }

    internal fun loadMainKey(context: Context) : String? {
        val key = context.resources.getString(R.string.pref_main_group_key)
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
    }

    internal fun modifyMainKey(context: Context, group: String) {
        val key = context.resources.getString(R.string.pref_main_group_key)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(key, group)
        editor.apply()
    }

    internal fun loadNotificationKey(context: Context) : String? {
        val key = context.resources.getString(R.string.pref_notification_group_key)
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
    }

    internal fun modifyNotificationKey(context: Context, group: String) {
        val key = context.resources.getString(R.string.pref_notification_group_key)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(key, group)
        editor.apply()
    }

    internal fun loadKeys(context: Context): List<String>? {
        val key = context.resources.getString(R.string.pref_groups_key)
        val s = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
        return s?.split(DELIMITER)
    }

    internal fun addKey(context: Context, key: String) {
        val keys = (loadKeys(context)
                ?: List(0) { "" }).toMutableList()
        keys.add(key)
        modifyKeys(context, keys)
    }

    internal fun modifyKeys(context: Context, keys: List<String>) {
        var s = ""
        for (i in 0 until keys.size) {
            s += keys[i]
            if (i < keys.size - 1)
                s += DELIMITER
        }
        val key = context.resources.getString(R.string.pref_groups_key)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(key, if (keys.isEmpty()) null else s)
        editor.apply()
    }

    internal fun saveSchedule(context: Context, schedule: Schedule) {
        val outputStream = context.openFileOutput(SCHEDULE_PATH, Context.MODE_PRIVATE)
        val json = Gson().toJson(schedule)
        outputStream.write(json.toByteArray())
        outputStream.close()
    }

    internal fun saveSchedule(context: Context, schedule: SchedulersDataResponseModel.Data) {
        val outputStream = context.openFileOutput(SCHEDULE_PATH, Context.MODE_PRIVATE)
        val json = Gson().toJson(schedule)
        outputStream.write(json.toByteArray())
        outputStream.close()
    }

    internal fun loadSchedule(context: Context): Schedule? {
        return try {
            Gson().fromJson(
                IOUtils.toString(context.openFileInput(SCHEDULE_PATH), ENCODING),
                object : TypeToken<Schedule>() {}.type
            )
        } catch (e: FileNotFoundException) {
            return null
        }
    }

    internal fun loadScheduleData(context: Context): SchedulersDataResponseModel.Data? {
        return try {
            Gson().fromJson(
                    IOUtils.toString(context.openFileInput(SCHEDULE_PATH), ENCODING),
                    object : TypeToken<SchedulersDataResponseModel.Data>() {}.type
            )
        } catch (e: FileNotFoundException) {
            return null
//            val schedule = loadScheduleFromAssets(context)
//            saveSchedule(context, schedule)
//            schedule
        }
    }

    internal fun loadTeachers(context: Context): HashMap<String, Teacher> {
        return try {
            Gson().fromJson(
                IOUtils.toString(context.openFileInput("teachers.json"), ENCODING),
                object : TypeToken<HashMap<String, Teacher>>() {}.type
            )
        } catch (e: FileNotFoundException) {
            return hashMapOf()
        }


    }

    internal fun saveFirstOddWeekDate(context: Context, dateTime: Long) {
        val outputStream = context.openFileOutput(FIRST_ODD_WEEK_DATE_PATH, Context.MODE_PRIVATE)
        val json = Gson().toJson(dateTime)
        outputStream.write(json.toByteArray())
        outputStream.close()
    }

    internal fun loadFirstOddWeekDate(context: Context): Long? {
        return try {
            Gson().fromJson(
                IOUtils.toString(context.openFileInput(FIRST_ODD_WEEK_DATE_PATH), ENCODING),
                object : TypeToken<Long>() {}.type
            )
        } catch (e: FileNotFoundException) {
            return null
        }
    }
}
