package dev.phystech.mipt.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.MeetItem
import dev.phystech.mipt.network.NetworkUtils
import java.io.File


class FileUtils private constructor() {

    fun getFile(fileModel: MeetItem.FileModel): File? {
        val model = fileModel.signatures.firstOrNull() ?: return null
        val fileUrl = NetworkUtils.getImageUrl(model.id, model.dir, model.path)
        val dataDir = Application.context.getExternalFilesDir(null)
        val file = File(dataDir?.path, model.name.toString())

        return file
    }

    fun downloadFile(fileModel: MeetItem.FileModel, someCallBack: (()->Unit)?=null): Boolean {


        val model = fileModel.signatures.firstOrNull() ?: return false
        val fileUrl = NetworkUtils.getImageUrl(model.id, model.dir, model.path)
        val dataDir = Application.context.getExternalFilesDir(null)
        val file = File(dataDir?.path, model.name.toString())

        if (file.exists()) return true

        file.mkdirs()
        file.delete()
        file.createNewFile()

        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(fileUrl))

        request.allowScanningByMediaScanner()
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(file.toUri())
        val dm: DownloadManager = Application.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager


        dm.enqueue(request)
        Toast.makeText(
            Application.context,
            "Downloading ${file.name}",
            Toast.LENGTH_LONG
        ).show()

        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                someCallBack?.invoke();
            }
        }
        Application.context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return false
    }


    companion object {
        val shared: FileUtils = FileUtils()
    }
}