package dev.phystech.mipt.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.api.UserDataResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.ui.fragments.study.StudentSchedulersColorFactory
import dev.phystech.mipt.ui.fragments.study.TeacherSchedulersColorFactory
import dev.phystech.mipt.ui.utils.SchedulersColorFactory
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.UserRole
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.subjects.BehaviorSubject

class UserRepository private constructor() {

    val preferences: SharedPreferences
    var userInfo: UserDataResponseModel.UserInfoResponse? = null
    var userInfoRx: BehaviorSubject<UserDataResponseModel.UserInfoResponse?> = BehaviorSubject.create()

    init {
        val context: Context = Application.context
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun login(code: String, callback: ((Boolean, String?) -> Unit)? = null) {
        ApiClient.shared.getToken(code).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.token != null) {
                        setToken(it.token!!)
                        loadUserData(callback)
//                        callback?.invoke(true, null)
                    } else {
                        callback?.invoke(true, it.content?.message)
                    }
                }, {
                    callback?.invoke(false, null)
                })
    }

    fun fakeLogin(userID: Int, callback: ((Boolean) -> Unit)?) {
        ApiClient.shared.login(userID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    preferences.edit()
                        .putString(KEY_FAKE_TOKEN, it.data?.accessToken)
                        .apply()
                }

                loadUserData { isSuccess, msg ->
                    callback?.invoke(it.status.isSuccess)
                }

            }, {
                callback?.invoke(false)
            })


    }

    fun loadUserData(callback: ((Boolean, String?) -> Unit)? = null) {
        ApiClient.shared.getUserInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.data?.user?.id?.let { userId ->
                    setUserId(userId)
                    ChatUtils.userId = userId
                }
                userInfo = it.data?.user
                userInfoRx.onNext(it.data?.user)
                callback?.invoke(true, null)
            }, {
                callback?.invoke(false, null)
            })
    }

    fun sendDeviceToken(token: String?) {
        if (token != null)
            ApiClient.shared.sendDeviceToken(token)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({}, {})
    }

    fun getUserMessagePreferences(callback: ((Boolean, String?) -> Unit)? = null) {
        ApiClient.shared.getUserMessagePreferences()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(it.data?.emailNotify ?: false, null)
            }, {
                callback?.invoke(false, it.localizedMessage)
            })
    }

    fun setUserMessagePreferences(value: Boolean, callback: ((Boolean, String?) -> Unit)? = null) {
        val valueInt = if (value) 1 else 0
        ApiClient.shared.setUserMessagePreferences(valueInt)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(it.data?.emailNotify ?: !value, null)
            }, {
                callback?.invoke(!value, it.localizedMessage)
            })
    }

    fun containFakeAccount(): Boolean {
        return preferences.contains(KEY_FAKE_TOKEN)
    }

    fun logout(callback: ((Boolean) -> Unit)? = null) {
        ApiClient.shared.logout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(true)
            }, {
                callback?.invoke(false)
            })
    }

    fun fakeLogout() {
        userInfo = null
        userInfoRx = BehaviorSubject.create()
        preferences.edit().remove(KEY_FAKE_TOKEN).apply()
        loadUserData { _, _ ->  }
    }


    fun setCode(value: String) {
        preferences.edit().putString(KEY_CODE, value).apply()
    }

    fun getCode(): String? {
        return preferences.getString(KEY_CODE, null)
    }

    fun setUserId(value: String) {
        preferences.edit().putString(KEY_ID, value).apply()
    }

    fun getUserId(): String? {
        return preferences.getString(KEY_ID, null)
    }

    fun setToken(value: String) {
        preferences.edit().putString(KEY_TOKEN, value).apply()
    }

    fun getToken(): String? {
        if (preferences.contains(KEY_FAKE_TOKEN)) {
            return preferences.getString(KEY_FAKE_TOKEN, null)
        }

        return preferences.getString(KEY_TOKEN, null)
    }


    fun clear() {
        preferences.edit().clear().apply()
    }

    var userRole: UserRole?
        get() {
            val intValue = preferences.getInt(KEY_ROLE, -1)
            return UserRole.getByValue(intValue)
        }
        set(value) {
            if (value == null) return

            preferences.edit().putInt(KEY_ROLE, value.value).apply()
        }

    fun createSchedulersColorFactory(): SchedulersColorFactory {
        if (userRole == UserRole.Employee)
            return TeacherSchedulersColorFactory()
        else return StudentSchedulersColorFactory()
    }


    companion object {
        private const val PREFERENCES_NAME = "USER_PREFERENCES"

        private const val KEY_CODE = "USER_AUTH_CODE"
        private const val KEY_TOKEN = "USER_AUTH_CODE"
        private const val KEY_ID = "USER_AUTH_ID"
        private const val KEY_FAKE_TOKEN = "USER_FAKE_TOKEN"
        private const val KEY_ROLE = "USER_ROLE"


        val shared: UserRepository = UserRepository()
    }
}