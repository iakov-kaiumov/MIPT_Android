package dev.phystech.mipt.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.SportSectionModel
import dev.phystech.mipt.network.NetworkUtils

class SportSectionsAdapter(private val resources: Resources): RecyclerView.Adapter<SportSectionsAdapter.ViewHolder>() {

    var delegate: Delegate? = null
    private val shownItems: ArrayList<SportSectionModel> = arrayListOf()
    private val allItems: ArrayList<SportSectionModel> = arrayListOf()
    private val weeks = resources.getStringArray(R.array.week_short)
    private var lastFilter: String? = null

    interface Delegate {
        fun onSelect(model: SportSectionModel)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(shownItems[position])
    }

    override fun getItemCount(): Int = shownItems.size

    fun setFilter(filterValue: String?) {
        lastFilter = filterValue
        val oldSize = shownItems.size
        shownItems.clear()

        if (filterValue == null) {
            shownItems.addAll(allItems)
        } else {
            val filteredItems = allItems.filter { v ->
                v.name?.toLowerCase()?.contains(filterValue.toLowerCase()) == true
            }
            shownItems.addAll(filteredItems)
        }


        notifyItemRangeRemoved(0, oldSize)
        notifyItemRangeInserted(0, shownItems.size)
    }

    fun setItems(newItems: List<SportSectionModel>) {
        allItems.clear()
        allItems.addAll(newItems)
        setFilter(lastFilter)
    }


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvDays: TextView = view.findViewById(R.id.tvDays)
        private val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        private val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)

        lateinit var model: SportSectionModel

        init {
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bind(model: SportSectionModel) {
            this.model = model
            tvName.text = model.name

            tvAuthor.text = model.schedules.firstOrNull { v -> v.teachers.any { vv -> vv.name != null } }
                ?.teachers?.firstOrNull { v -> v.name != null }?.name

            tvDays.text = model.schedules
                .mapNotNull { v -> v.day }
                .distinct()
                .sorted()
                .map { weeks[it - 1] }
                .joinToString(separator = ", ") { it.toString() }

            model.image?.signatures?.firstOrNull()?.let {
                val imageUrl = NetworkUtils.getImageUrl(it.id, it.dir, it.path)
                Picasso.get()
                    .load(imageUrl)
                    .error(R.drawable.bg_rounded_view_gray_10)
                    .into(ivAvatar)
            }
        }

    }


}