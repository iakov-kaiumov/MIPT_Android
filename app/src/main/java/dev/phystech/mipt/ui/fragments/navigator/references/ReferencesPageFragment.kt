package dev.phystech.mipt.ui.fragments.navigator.references

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.CatalogAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.ReferenceModel
import dev.phystech.mipt.utils.visibility


class ReferencesPageFragment : BaseFragment(), ReferencesPageContract.View {

    private val presenter: ReferencesPagePresenter = ReferencesPagePresenter()

    lateinit var recyclerNews: RecyclerView
    lateinit var newsAdapter: CatalogAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news_page, container, false)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    override fun bindView(view: View?) {
        if (view == null) return;

        newsAdapter = CatalogAdapter()
        recyclerNews = view.findViewById(R.id.recyclerNews)
    }

    override fun setAdapter(adapter: CatalogAdapter) {
        recyclerNews.adapter = adapter
    }

    override fun showContactPopup(forModel: ReferenceModel) {
        if (!forModel.containEmail && !forModel.containPhone) return

        val view = layoutInflater.inflate(R.layout.alert_contact, null)
        view.findViewById<TextView>(R.id.tvTitle).text = forModel.getAlertTitle()
        val call: Button = view.findViewById(R.id.btnCall)
        val copy: Button = view.findViewById(R.id.btnCopy)
        val email: Button = view.findViewById(R.id.btnEmail)
        val cancel: Button = view.findViewById(R.id.btnCancel)

        call.visibility = forModel.containPhone.visibility()
        copy.visibility = forModel.containPhone.visibility()
        email.visibility = forModel.containEmail.visibility()

        context?.let { context ->
            val alert = AlertDialog.Builder(context)
                    .setView(view)
                    .create()

            call.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:${forModel.personalPhoneNumber}"))
                startActivity(callIntent)
                alert.cancel()
            }

            copy.setOnClickListener {
                val clipboardManager = getSystemService(context, ClipboardManager::class.java)
                clipboardManager?.setPrimaryClip(ClipData.newPlainText(forModel.personalPhoneNumber, forModel.personalPhoneNumber))
                alert.cancel()
            }

            email.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${forModel.email}"))
                startActivity(callIntent)
                alert.cancel()
            }

            cancel.setOnClickListener {
                alert.cancel()
            }


            alert.show()
        }

    }
}