package dev.phystech.mipt

import android.content.Context
import android.location.LocationManager
import android.util.AttributeSet
import android.view.View
import com.google.gson.Gson
import dev.phystech.mipt.models.api.ContactsDataResponseModel
import dev.phystech.mipt.repositories.AssetsRepository
import dev.phystech.mipt.utils.ScheduleApp
import io.realm.Realm
import io.realm.RealmConfiguration

class Application: ScheduleApp() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        AssetsRepository.create(this)
        initRealm()

        //LocationsService
        val lm = getSystemService(LOCATION_SERVICE) as? LocationManager

    }

    private fun initRealm() {
        Realm.init(this)

        val version = 2L
        val config = RealmConfiguration.Builder()
            .schemaVersion(version)
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.setDefaultConfiguration(config)
    }


    companion object {
        lateinit var context: Context
            private set
    }
}