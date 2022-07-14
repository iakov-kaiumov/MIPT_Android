package dev.phystech.mipt.ui.fragments.news

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.News
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.ui.fragments.study.scheduler_event_detail.SchedulerEventDetailFragment
import dev.phystech.mipt.ui.utils.ImageGetter
import dev.phystech.mipt.utils.visibility

class NewsDetailsFragment(val model: News) : BaseFragment() {

    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var tvTitle: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDescription: TextView
    private lateinit var backdrop: ImageView
    private lateinit var topImage: ImageView
    private lateinit var webView: WebView
    private lateinit var rlBtnAddToSchedule: RelativeLayout
    private lateinit var tvAddSchedule: TextView

    var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = arguments?.getString(TITLE_KEY)
    }

    override fun onStart() {
        super.onStart()
//        navigationPresenter.setStatusBarTransparency(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news_details, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        collapsingToolbar = view.findViewById(R.id.collapsing_toolbar)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDate = view.findViewById(R.id.tvDate)
        tvDescription = view.findViewById(R.id.tvDescription)
        backdrop = view.findViewById(R.id.backdrop)
        topImage = view.findViewById(R.id.topImage)
        webView = view.findViewById(R.id.webView)
        rlBtnAddToSchedule = view.findViewById(R.id.rlBtnAddToSchedule)
        tvAddSchedule = view.findViewById(R.id.tvAddSchedule)

        tvDescription.movementMethod = LinkMovementMethod()

        collapsingToolbar.title = title

        val ivBack: ImageView = view.findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            mainActivity.supportFragmentManager.popBackStack();
        }

        rlBtnAddToSchedule.setOnClickListener {
            model.attachedEventId?.toIntOrNull()?.let {
                val fragment = SchedulerEventDetailFragment.newInstance(it)
                navigationPresenter.pushFragment(fragment, true)
            }
        }

        setContent()
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    private fun setContent() {
        tvTitle.text = model.title
        tvDate.text = model.date
        rlBtnAddToSchedule.visibility = model.attachedEventId.isNullOrBlank().not().visibility()
        if (ScheduleEventRepository.shared.containsInMyEvents(model.attachedEventId ?: "-")) {
            tvAddSchedule.setText(R.string.add_to_schedules_completed)
            tvAddSchedule.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.secondary_text, null)))
        } else {
            tvAddSchedule.setText(R.string.add_to_schedules)
            tvAddSchedule.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.main_color, null)))
        }

        if (model.image != null) {
            Picasso.get()
                .load(model.image)
                .transform(BlurTransformation(requireContext(), 25, 1))
                .into(backdrop)

            val lp = (topImage.layoutParams as? ConstraintLayout.LayoutParams)
            lp?.dimensionRatio = "${model.imageWidth}:${model.imageHeight}"
            topImage.layoutParams = lp

            Thread {
                val img = Picasso.get()
                    .load(model.image)
                    .get()

                Handler(Looper.getMainLooper()).post {
                    val lp = (topImage.layoutParams as? ConstraintLayout.LayoutParams)
                    lp?.dimensionRatio = "${img.width}:${img.height}"
                    topImage.layoutParams = lp
                    topImage.setImageBitmap(img)
                }
            }.start()

        } else {
            topImage.visibility = false.visibility()

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvDescription.text = Html.fromHtml(
                model.content,
                Html.FROM_HTML_MODE_LEGACY,
                ImageGetter(resources, tvDescription),
                null
            )
        } else {
            tvDescription.text = Html.fromHtml(model.content)
        }

        tvDescription.movementMethod = LinkMovementMethod.getInstance()
    }

//    private val imageGetter = Html.ImageGetter {
//        Picasso.get()
//            .load(it)
//            .get()
//            .toDrawable(resources)
//
//        retResou
//    }

    companion object {
        const val TITLE_KEY = "title"
        const val IMAGE_SIZE_KEY = "image_size_key"
    }
}