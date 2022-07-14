package dev.phystech.mipt.ui.fragments.services.feedback

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ShareCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.utils.isSuccess
import dev.phystech.mipt.utils.visibility

class FeedbackFragment : BaseFragment() {

    lateinit var etFormQuestion: EditText
    lateinit var tvSend: TextView
    lateinit var tvThanksMessage: TextView
    lateinit var tvShare: TextView
    lateinit var tvRate: TextView
    lateinit var tvSupport: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        etFormQuestion = view.findViewById(R.id.etFormQuestion)
        tvSend = view.findViewById(R.id.tvSend)
        tvThanksMessage = view.findViewById(R.id.tvThanksMessage)
        tvShare = view.findViewById(R.id.tvShare)
        tvRate = view.findViewById(R.id.tvRate)
        tvSupport = view.findViewById(R.id.tvSupport)

        etFormQuestion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                tvThanksMessage.visibility = false.visibility()

                if (s != null && s.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        tvSend.setTextColor(resources.getColor(R.color.logo_color, null))
                    } else {
                        tvSend.setTextColor(resources.getColor(R.color.logo_color))
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        tvSend.setTextColor(resources.getColor(R.color.secondary_text, null))
                    } else {
                        tvSend.setTextColor(resources.getColor(R.color.secondary_text))
                    }
                }
            }
        })

        tvSend.setOnClickListener {
            val question = etFormQuestion.text.toString()
            if (question.isEmpty()) return@setOnClickListener

            showProgress()
            ApiClient.shared.sendSupportQuestion(question)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.status.isSuccess) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.feedback_bottom_text),
                            Toast.LENGTH_SHORT
                        ).show()

                        etFormQuestion.setText(String.empty())
                    }

                }, {
                    hideProgress()
                    tvThanksMessage.visibility = true.visibility()
                   }, {
                    hideProgress()
                    tvThanksMessage.visibility = true.visibility()
                })
        }

        val ivBack: ImageView = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            mainActivity.supportFragmentManager.popBackStack();
        }

        tvShare.setOnClickListener {
            ShareCompat.IntentBuilder(requireContext())
                .setType("text/plain")
                .setChooserTitle(getString(R.string.pref_share_message))
                .setText("${getString(R.string.pref_share_message)}\n${getString(R.string.pref_rate_app_url)}")
                .startChooser()
        }

        tvRate.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RATE_URL))
            startActivity(intent)
        }

        tvSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL))
            startActivity(intent)
        }
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    companion object {
        private const val RATE_URL = "https://play.google.com/store/apps/details?id=dev.phystech.mipt"
        private const val SUPPORT_URL = "https://vk.com/mipt_app"
    }
}