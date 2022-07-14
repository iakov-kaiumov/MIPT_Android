package dev.phystech.mipt.ui.fragments.study.guest

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.Application
import dev.phystech.mipt.BuildConfig
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.SchedulersFilesAdapter
import dev.phystech.mipt.base.BaseFragment
import java.io.File

/** Расписание для гостя
 */
class SchedulersGuestFragment: BaseFragment(), SchedulersGuestContract.View {

    lateinit var recyclerView: RecyclerView
    private val presenter: SchedulersGuestContract.Presenter = SchedulersGuestPresenter()


    // LIFE CIRCLE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedulers_guest, container, false)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    //  BASE VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        recyclerView = view.findViewById(R.id.recycler)
    }


    //  CONTRACT VIEW
    override fun setAdapter(adapter: SchedulersFilesAdapter) {
        recyclerView.adapter = adapter
    }

    override fun showFile(file: File) {
//        val intent = Intent(Intent.ACTION_VIEW, file.toUri())
//        intent.type = "*/*"
//        startActivity(intent)

        val myMime: MimeTypeMap = MimeTypeMap.getSingleton()
        val uri = FileProvider.getUriForFile(Application.context, BuildConfig.APPLICATION_ID + ".provider", file)
        val newIntent = Intent(Intent.ACTION_VIEW, uri)
        val mimeType: String? = myMime.getMimeTypeFromExtension(file.extension)
        newIntent.type = mimeType

        try {
            val pm = activity!!.packageManager
            if (newIntent.resolveActivity(pm) != null) {
                startActivity(newIntent)
            }

//            context?.startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show()
        }
    }

    override fun showFile(file: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file))
        startActivity(intent)
    }

    override fun checkIOPermission(requestIfNeeded: Boolean): Boolean {
        context?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (requestIfNeeded) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                }

                return false
            }

            return true
        }

        return false
    }


}