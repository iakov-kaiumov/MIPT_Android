package dev.phystech.mipt.ui.activities.authorization

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.utils.UserRole

interface AuthorizationContract {
    interface View: BaseView {
        fun showConfirmDialog(@StringRes withTitle: Int, @StringRes withDescription: Int? = null)
        fun <T>showActivity(activityClass: Class<T>, withExtras: Bundle? = null, withFinish: Boolean = true) where T: BaseView
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun login(withType: UserRole)
        abstract fun loginConfirmed()
        abstract fun authSuccess(withRole: UserRole? = null)
        abstract fun authError()
        abstract fun checkDebugSignIn(context: Context, callback: ((Boolean) -> Unit))
    }
}