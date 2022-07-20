package dev.phystech.mipt.ui.activities.authorization

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import dev.phystech.mipt.R
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.ui.activities.auth_webview.AuthorizationWebViewActivity
import dev.phystech.mipt.utils.AppConfig
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.UserRole
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.Realm

class AuthorizationPresenter: AuthorizationContract.Presenter() {
    override fun login(withType: UserRole) {
        when (withType) {
            UserRole.Guest -> {
                view.showConfirmDialog(R.string.authorization_guest_dialog_title, R.string.authorization_guest_dialog_description)
            }
            UserRole.Employee -> {
                val bundle = Bundle()
                bundle.putInt("role", withType.ordinal)
                bundle.putString("url", "https://appadmin.mipt.ru/api/oauth/mipt")
                view.showActivity(AuthorizationWebViewActivity::class.java, withFinish = false, withExtras = bundle)
            }
            UserRole.Student -> {
                val bundle = Bundle()
                bundle.putInt("role", withType.ordinal)
                view.showActivity(AuthorizationWebViewActivity::class.java, withFinish = false, withExtras = bundle)
            }
            UserRole.Test -> {
                val bundle = Bundle()
                bundle.putInt("role", withType.ordinal)
                bundle.putString("url", "https://appadmin.mipt.ru/api/oauth/authorize?response_type=code&state=auth&client_id=2&redirect_uri=appmipt")
                view.showActivity(AuthorizationWebViewActivity::class.java, withFinish = false, withExtras = bundle)
            }
        }
    }

    override fun loginConfirmed() {
//        UserRepository.shared.userRole = UserRole.Guest
//        UserRepository.shared.setToken(AppConfig.defaultToken)
//        ChatUtils.clearChats()

        val bundle = Bundle()
        bundle.putInt("role", 0)
        bundle.putString("url", "https://appadmin.mipt.ru/api/oauth/app?response_type=code&state=auth&client_id=2&redirect_uri=appmipt")

        view.showActivity(AuthorizationWebViewActivity::class.java, withFinish = false, withExtras = bundle)
//        view.showActivity(MainActivity::class.java)
    }

    override fun authSuccess(withRole: UserRole?) {
        view.showProgress()
        val code = UserRepository.shared.getCode()?.let {
            UserRepository.shared.login(it) { success, message ->
                if (success) {
                    UserRepository.shared.userRole = withRole
                    ChatUtils.clearChats()
                    view.showActivity(MainActivity::class.java)
                } else {
                    UserRepository.shared.clear()
                    if (message == null) {
                        view.showMessage(R.string.error_response)
                    } else view.showMessage(message)
                }
            }
        }

    }

    override fun authError() {
        if (AppConfig.ignoreAuthorizationError) {
            ChatUtils.clearChats()
            view.showActivity(MainActivity::class.java)
            return
        }

        view.showMessage(R.string.message_some_error_try_late)
    }

    override fun checkDebugSignIn(context: Context, callback: ((Boolean) -> Unit)) {
        ApiClient.shared.androidVersion()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("PUPA", it.toString())
                val releaseVersion = it.data?.setting?.value
                Log.d("PUPA", releaseVersion.toString())
                val currentVersion = getAppVersion(context)
                Log.d("PUPA", currentVersion)
                if (releaseVersion != null) {
                    callback(currentVersion > releaseVersion)
                } else {
                    callback(false)
                }
            }, {
                callback(false)
            })
    }

    fun getAppVersion(context: Context): String {
        var version = ""
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version
    }
}