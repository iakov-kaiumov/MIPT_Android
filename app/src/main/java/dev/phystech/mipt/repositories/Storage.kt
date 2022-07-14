package dev.phystech.mipt.repositories

import android.content.Context
import android.content.SharedPreferences
import dev.phystech.mipt.Application
import dev.phystech.mipt.utils.AppConfig.alwaysClearPreferences

class Storage private constructor() {

    private val preferences: SharedPreferences


    init {
        val context = Application.context
        preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

        if (alwaysClearPreferences) {
            preferences.edit().clear().apply()
        }
    }


    fun isFirstRun(): Boolean {
        return preferences.getBoolean(KEY_IS_FIRST_RUN, true)
    }

    fun isNewAppVersion(): Boolean {
        try {
            val pInfo = Application.context.packageManager.getPackageInfo(Application.context.packageName, 0)
            val cVersionName = pInfo.versionName
            val pVersionName = preferences.getString(KEY_LAST_APP_VERSION, null)

            preferences.edit().putString(KEY_LAST_APP_VERSION, cVersionName).apply()
            return cVersionName != pVersionName

        } catch (e: Exception) {
            return false
        }
    }

    fun setFirstRun(value: Boolean) {
        preferences.edit().putBoolean(KEY_IS_FIRST_RUN, value).apply()
    }



    companion object {
        private const val prefName = "STORAGE_PREFERENCES"

//        private const val KEY_IS_FIRST_RUN = "IS_FIRST_RUN_2"
        private const val KEY_IS_FIRST_RUN = "IS_FIRST_RUN_CHATS"
        private const val KEY_LAST_APP_VERSION = "LAST_VERSION"
        val shared: Storage = Storage()
    }
}