package dev.phystech.mipt.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.ContextMenu
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.phystech.mipt.R
import dev.phystech.mipt.notification.MyFirebaseMessagingService
import dev.phystech.mipt.repositories.Storage
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.activities.authorization.AuthorizationActivity
import dev.phystech.mipt.ui.activities.main.MainActivity
import kotlin.math.max
import kotlin.math.min

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_what_news)

        val isFirstRun = Storage.shared.isFirstRun()
        val isNewAppVersion = Storage.shared.isNewAppVersion()
        arrayOf(1, 2, 3).joinToString { v -> v.toString() }

        val intent = if (isFirstRun) {
            Intent(this, OnBoardingActivity::class.java)
        } else {
            UserRepository.shared.loadUserData()
            if (UserRepository.shared.getToken() != null && UserRepository.shared.userRole != null) {

                val intent = Intent(this, MainActivity::class.java)

                val bundle = getIntent().extras
                if (bundle != null) {
                    val convid = bundle.getString(MyFirebaseMessagingService.EXT_CONV_ID)
                    if (convid != null && !convid.isEmpty()) intent.putExtra(MyFirebaseMessagingService.EXT_CONV_ID, convid)
                    val externalUrl = bundle.getString(MyFirebaseMessagingService.EXT_EXTERNAL_URL)
                    if (externalUrl != null && !externalUrl.isEmpty()) intent.putExtra(MyFirebaseMessagingService.EXT_EXTERNAL_URL, externalUrl)
                }

                intent
            } else {
                Intent(this, AuthorizationActivity::class.java)
            }
        }

        intent.let {
            startActivity(it)
            finish()
        }
    }
}