package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.MeetItem
import java.util.*
import kotlin.collections.ArrayList

class SchedulersFilesAdapter(val items: ArrayList<MeetItem>)
    : RecyclerView.Adapter<SchedulersFilesAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(item: MeetItem)
    }

    var delegate: Delegate? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scheduler_file, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])

    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val ivFile: ImageView = view.findViewById(R.id.ivFile)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)

        private lateinit var model: MeetItem

        init {
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bind(model: MeetItem) {
            this.model = model

            tvTitle.text = model.name
            tvSubtitle.text = model.meetName
            tvDate.text = model.dates

            val fileType = model.fileType?.toLowerCase(Locale.ROOT) ?: ""

            @DrawableRes
            val res = when {
                fileType.contains("jpg") -> R.drawable.ic_jpg
                fileType.contains("pdf") -> R.drawable.ic_pdf
                fileType.contains("xls") -> R.drawable.ic_xls
                fileType.contains("doc") -> R.drawable.ic_doc
                fileType.contains("url") -> R.drawable.ic_url
                else -> R.drawable.example_file_text
            }

            ivFile.setImageResource(res)
//            Picasso.get().load(R.drawable.ic_jpg)
//                .into(ivFile)
        }
    }

}