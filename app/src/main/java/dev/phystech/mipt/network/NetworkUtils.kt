package dev.phystech.mipt.network

import android.content.Context
import android.content.Intent

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

import android.net.NetworkInfo
import android.provider.Settings
import android.util.Log


class NetworkUtils {

    companion object {
        const val SERVER_ADDRESS = "https://app.mipt.ru"

        fun getImageUrl(id: String?, dir: String?, path: String?): String {
            return "https://appadmin.mipt.ru/get-file.php?id=${id}&dir=${dir}&path=${path}&style=medium"
        }

        private var activeNetwork: NetworkInfo? = null

        private fun getNetworkInfo(context: Context): NetworkInfo? {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo
        }

        fun isConnected(context: Context): Boolean {
            activeNetwork = getNetworkInfo(context)
            return activeNetwork != null && activeNetwork!!.isConnected
        }

        fun openSettings(context: Context) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }
    }
}

//  https://appadmin.mipt.ru/get-file.php?id=${id}&dir=${dir}&path=${path}&style=medium