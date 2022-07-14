package dev.phystech.mipt.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.News
import dev.phystech.mipt.utils.visibility
import java.lang.Exception
import kotlin.math.min

@SuppressLint("NotifyDataSetChanged")
class NewsAdapter(val contentType: News.Type) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    interface Delegate {
        fun onLastItemLoad()
        fun onNewsClick(model: News)
    }

    var itemsKeySendRequest = ArrayList<String>()
    var delegate: Delegate? = null
    val items: ArrayList<News> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_news, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//        if (filteredItems.size > position)
            viewHolder.bind(items[position])

        if (position == items.size - 2) {
            items[position].id?.let { id ->
                if (!itemsKeySendRequest.contains(id)) {
                    delegate?.onLastItemLoad()
                }

                itemsKeySendRequest.add(id)
            }
        }
    }

    override fun getItemCount() = items.size



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivImage: ImageView = view.findViewById(R.id.ivImage)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvPlace: TextView = view.findViewById(R.id.place)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        private val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)

        private var model: News? = null

        init {
            view.setOnClickListener {
                model?.let { model ->
                    delegate?.onNewsClick(model)
                }
            }
        }

        fun bind(model: News) {
            this.model = model

            tvDate.visibility = (!model.date.isNullOrEmpty()).visibility()
            tvTitle.visibility = (!model.title.isNullOrBlank()).visibility()
            tvDescription.visibility = (!model.content.isNullOrBlank()).visibility()
            tvSubtitle.visibility = model.subtitle.isNullOrEmpty().not().visibility()


            tvDate.text = model.date
            tvTitle.text = model.title
            tvSubtitle.text = model.subtitle
            model.chair?.let { tvPlace.text = it }
            var content = model.content?.substring(0, min(164, model.content?.length ?: 164))
            if (model.content != null) content += "..."

            if (content != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvDescription.text = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    tvDescription.text = Html.fromHtml(content)
                }
            }

            tvPlace.visibility = (model.chair != null).visibility()


            ivImage.visibility = (model.image != null).visibility()

            model.imageWidth = null
            model.imageHeight = null

            model.image?.let { image ->
                Thread {
                    try {
                        val img = Picasso.get().load(image).get()
                        Handler(Looper.getMainLooper()).post {
                            val lp = (ivImage.layoutParams as? ConstraintLayout.LayoutParams)
                            lp?.dimensionRatio = "${img.width}:${img.height}"
                            ivImage.layoutParams = lp
                            ivImage.setImageBitmap(img)
                            model.imageWidth = img.width.toString()
                            model.imageHeight = img.height.toString()
                        }
                    } catch (e: Exception) {

                    }
                }.start()
            }
        }


    }
}

