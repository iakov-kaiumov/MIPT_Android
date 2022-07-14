package dev.phystech.mipt.ui.activities.authorization

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseActivity
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.ui.activities.auth_webview.AuthorizationWebViewActivity
import dev.phystech.mipt.utils.UserRole

class AuthorizationActivity: BaseActivity(), AuthorizationContract.View {

    private val presenter: AuthorizationContract.Presenter = AuthorizationPresenter()

    lateinit var rlLoginStudent: RelativeLayout
    lateinit var rlLoginEmployee: RelativeLayout
    lateinit var rlLoginGuest: RelativeLayout


    //  LIFE CIRCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindView()
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            when (resultCode) {
                AuthorizationWebViewActivity.RESULT_SUCCESS -> {
                    val roleInt = data?.extras?.getInt("role")
                    val role = UserRole.getByValue(roleInt ?: -1)
                    presenter.authSuccess(role)
                }
                AuthorizationWebViewActivity.RESULT_ERROR -> {
                    presenter.authError()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    //  CONTRACT
    override fun showConfirmDialog(withTitle: Int, withDescription: Int?) {
        val alert = AlertDialog.Builder(this)
            .setTitle(withTitle)
            .setMessage(withDescription ?: R.string.empty)
            .setPositiveButton(R.string.yes) { _, _ ->
                presenter.loginConfirmed()
            }.setNegativeButton(R.string.no) { _, _ -> }
            .create()

        alert.show()
    }

    override fun <T> showActivity(activityClass: Class<T>, withExtras: Bundle?, withFinish: Boolean) where T : BaseView {
        val intent = Intent(this, activityClass)
        withExtras?.let { intent.putExtras(it) }
        startActivityForResult(intent, REQUEST_CODE)

        if (withFinish) finish()
    }


    //  OTHERS
    private fun bindView() {
        //  binding
        rlLoginStudent = findViewById(R.id.rlLoginStudent)
        rlLoginEmployee = findViewById(R.id.rlLoginEmployee)
        rlLoginGuest = findViewById(R.id.rlLoginGuest)

        //  set listeners
        rlLoginStudent.setOnClickListener(this::loginStudentPressed)
        rlLoginEmployee.setOnClickListener(this::loginEmployeePressed)
        rlLoginGuest.setOnClickListener(this::loginGuestPressed)
    }


    //  EVENTS
    private fun loginStudentPressed(view: View) {
        presenter.login(UserRole.Student)
    }

    private fun loginEmployeePressed(view: View) {
        presenter.login(UserRole.Employee)
    }

    private fun loginGuestPressed(view: View) {
        presenter.login(UserRole.Guest)
    }

    companion object {
        private const val REQUEST_CODE = 102
    }

}