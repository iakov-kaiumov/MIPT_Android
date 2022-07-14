package dev.phystech.mipt.repositories

import android.content.Context
import dev.phystech.mipt.Application

class SchedulerNotesRepository private constructor() {

    private val pref = Application.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun get(schedulerId: Int): String? {
        return pref.getString("note $schedulerId", null)
    }

    fun set(schedulerId: Int, value: String) {
        pref.edit().putString("note $schedulerId", value).apply()
    }


    companion object {
        private const val PREF_NAME = "schedulers.notes"

        val shared: SchedulerNotesRepository = SchedulerNotesRepository()
    }
}