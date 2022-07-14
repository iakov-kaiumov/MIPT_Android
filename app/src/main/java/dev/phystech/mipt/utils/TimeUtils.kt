package edu.phystech.iag.kaiumov.shedule.utils

import android.content.res.Resources
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtils {
    /**
     * @return positive if t1 > t2, negative if t1 < t2, zero if t1 == t2
     */
    fun compareTime(t1: String, t2: String): Int {
        val parser =  SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return try {
            (parser.parse(t1)!!.time - parser.parse(t2)!!.time).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun length(t1: String, t2: String): Double {
        val parser =  SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return try {
            (parser.parse(t2)!!.time - parser.parse(t1)!!.time) / 1000 / 3600.0
        } catch (e: Exception) {
            1.0
        }
    }

    fun getCalendarTime(day: Int, t: String): Calendar {
        val calendar = Calendar.getInstance()
        val split = t.split(":")
        calendar.set(Calendar.DAY_OF_WEEK, if (day in 1..5) day + 1 else day - 6)
        calendar.set(Calendar.HOUR_OF_DAY, split[0].toInt())
        calendar.set(Calendar.MINUTE, split[1].toInt())
        return calendar
    }

    /**
     * @return day number from Monday - 1 to Sunday - 7
     */
    fun getCurrentDay(): Int {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2
        return if (currentDay in 0..6) currentDay + 1 else 7
    }

    fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + calendar.get(Calendar.MINUTE)
    }


    fun getLastMessageTime(unixSeconds: Long): String {

        var result: String = ""

        // узнаем год и день сегодня и вчера
        val calendar = Calendar.getInstance()

        val todayDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear: Int = calendar.get(Calendar.YEAR);

        calendar.add(Calendar.DATE, -1)
        val yesterdayDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val yesterdayYear: Int = calendar.get(Calendar.YEAR);

        // Узнаем год и день сообщения
        calendar.time = Date(unixSeconds * 1000L)

        val messageDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val messageYear: Int = calendar.get(Calendar.YEAR);

        if (messageYear == todayYear && messageDayOfYear == todayDayOfYear) {
            // Сегодня
            val df =  SimpleDateFormat("HH:mm", Locale.getDefault())
            result = df.format(calendar.time);
        } else if (messageYear == yesterdayYear && messageDayOfYear == yesterdayDayOfYear) {
            // Вчера
            result = Application.context.getString(R.string.yesterday)
        } else if (messageYear < todayYear) {
            // В прошлом году
            val df =  SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            result = df.format(calendar.time);
        } else {
            // В этом году
            val df =  SimpleDateFormat("dd.MM", Locale.getDefault())
            result = df.format(calendar.time);
        }

        return result
    }

    fun getMessageTime(unixSeconds: Long): String {
        var result: String = ""
        val calendar = Calendar.getInstance()
        calendar.time = Date(unixSeconds * 1000L)
        val hour = calendar.get(GregorianCalendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val min = calendar.get(GregorianCalendar.MINUTE).toString().padStart(2, '0')
        result = "$hour:$min"
        return result
    }

    fun getMessageDayOfYear(unixSeconds: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.time = Date(unixSeconds * 1000L)
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    fun getMessageDate(unixSeconds: Long): String {

        var result: String = ""

        // узнаем год и день сегодня и вчера
        val calendar = Calendar.getInstance()

        val todayDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear: Int = calendar.get(Calendar.YEAR);

        calendar.add(Calendar.DATE, -1)
        val yesterdayDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val yesterdayYear: Int = calendar.get(Calendar.YEAR);

        // Узнаем год и день сообщения
        calendar.time = Date(unixSeconds * 1000L)

        val messageDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val messageYear: Int = calendar.get(Calendar.YEAR);

        if (messageYear == todayYear && messageDayOfYear == todayDayOfYear) {
            // Сегодня
            result = Application.context.getString(R.string.today)
        } else if (messageYear == yesterdayYear && messageDayOfYear == yesterdayDayOfYear) {
            // Вчера
            result = Application.context.getString(R.string.yesterday)
        } else if (messageYear < todayYear) {
            // В прошлом году
            val df =  SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            result = df.format(calendar.time);
        } else {
            // В этом году
            val df =  SimpleDateFormat("dd MMMM", Locale.getDefault())
            result = df.format(calendar.time);
        }

        return result
    }

    fun getUserLastActive(date: Date): String {

        var result: String = ""

        // узнаем год и день сегодня и вчера
        val calendar = Calendar.getInstance()

        val todayDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear: Int = calendar.get(Calendar.YEAR);

        calendar.add(Calendar.DATE, -1)
        val yesterdayDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val yesterdayYear: Int = calendar.get(Calendar.YEAR);

        // Узнаем год и день сообщения
        calendar.time = date

        val messageDayOfYear: Int = calendar.get(Calendar.DAY_OF_YEAR)
        val messageYear: Int = calendar.get(Calendar.YEAR);

        if (messageYear == todayYear && messageDayOfYear == todayDayOfYear) {
            // Сегодня
            result = Application.context.getString(R.string.today)
        } else if (messageYear == yesterdayYear && messageDayOfYear == yesterdayDayOfYear) {
            // Вчера
            result = Application.context.getString(R.string.yesterday)
        } else {
            // Дата
            val df =  SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            result = df.format(calendar.time);
        }

//        else if (messageYear < todayYear) {
//            // В прошлом году
//            val df =  SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
//            result = df.format(calendar.time);
//        } else {
//            // В этом году
//            val df =  SimpleDateFormat("dd MMMM", Locale.getDefault())
//            result = df.format(calendar.time);
//        }

        return result
    }

    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }
}