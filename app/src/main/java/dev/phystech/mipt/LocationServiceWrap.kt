package dev.phystech.mipt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import dev.phystech.mipt.ui.activities.main.MainActivity
import io.reactivex.rxjava3.subjects.BehaviorSubject

class LocationServiceWrap(private val context: Context): LocationListener {

    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val coordinates: BehaviorSubject<Location> = BehaviorSubject.create()

    init {
        startListen()
    }

    fun startListen(fromPermissionRedsut: Boolean = false) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            if (!fromPermissionRedsut)
                ActivityCompat.requestPermissions(
                    (context as MainActivity),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE
                )

            return
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100f, this)
    }




    override fun onLocationChanged(location: Location) {
        Log.d("Location_wrap", "LOC: ${location.latitude}, ${location.longitude}")
        coordinates.onNext(location)
    }

    override fun onProviderDisabled(provider: String) {
        print(provider)
//        super.onProviderDisabled(provider)
    }

    override fun onProviderEnabled(provider: String) {
        print(provider)
        startListen()
//        super.onProviderEnabled(provider)
    }


    companion object {
        lateinit var shared: LocationServiceWrap
            private set

        const val REQUEST_CODE = 3000

        fun create(context: Context) {
            shared = LocationServiceWrap(context)
        }
    }
}