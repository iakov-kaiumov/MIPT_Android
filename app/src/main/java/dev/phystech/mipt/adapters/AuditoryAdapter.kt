package dev.phystech.mipt.adapters

import android.content.Context
import android.location.LocationListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shuhart.stickyheader.StickyAdapter
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.SchedulePlace
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.network.NetworkUtils

class AuditoryAdapter(val context: Context): StickyAdapter<AuditoryAdapter.HeaderViewHolder, AuditoryAdapter.CommonViewHolder>() {

    interface Delegate {
        fun onTeacherSelect(model: SchedulePlace)
        fun newAuditory()
    }

    var delegate: Delegate? = null

    private val items: ArrayList<SchedulePlace> = arrayListOf()
    private val headersItems: ArrayList<SchedulePlace> = arrayListOf()
    private val shownItems: ArrayList<SchedulePlace> = arrayListOf()

    init {

    }



    //  Item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        if (viewType == TYPE_HEADER) {
            return HeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_list_header, parent, false)
            )
        }

        if (viewType == TYPE_ADD) {
            return AddButtonViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_auditory, parent, false)
            )
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auditory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        val item = shownItems[position]
        if (item.id == ID_HEADER) {
            holder as HeaderViewHolder
            holder.tvValue.text = item.name?.toUpperCase()
        } else if (item.id != ID_ADD_BUTTON) {
            holder as ViewHolder
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = this.shownItems.size

    override fun getItemViewType(position: Int): Int {
        return if (shownItems[position].id == ID_HEADER) TYPE_HEADER
        else if (shownItems[position].id == ID_ADD_BUTTON) TYPE_ADD else TYPE_ITEM

    }



    //  Header
    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        if (shownItems[itemPosition].id == ID_HEADER) {
            headersItems.firstOrNull { v -> v.name == shownItems[itemPosition].name }?.let { headerItem ->
                return headersItems.indexOf(headerItem)
            }
        }

        if (shownItems[itemPosition].id == ID_ADD_BUTTON) {
            return headersItems.lastIndex
        }

        shownItems[itemPosition].building?.name?.let { building ->
            headersItems.firstOrNull { v -> v.name == building }?.let { firstItemSection ->
                return headersItems.indexOf(firstItemSection)
            }
        }

        return 0
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, headerPosition: Int) {
        val name = headersItems[headerPosition].name
        holder?.tvValue?.text = name?.toUpperCase()
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_list_header, parent, false)

        view.findViewById<TextView>(R.id.tvValue).setText("123123")

        return HeaderViewHolder(view)
    }


    //  OTHER
    fun setItems(items: List<SchedulePlace>) {
        this.items.clear()
        this.items.addAll(items.sortedBy { v -> v.building?.name })

        shownItems.clear()
        shownItems.addAll(this.items)

        val buildings = items.map { v -> v.building }
            .distinctBy { v -> v?.id }
            .map { v -> SchedulePlace().apply {
                id = ID_HEADER
                name = v?.name
            } }.sortedBy { v -> v.name }

//        headersItems.clear()
//        headersItems.addAll(buildings)

        var i = 0
        while (i < shownItems.size) {
            if (i == 0 || this.shownItems[i].building?.name != this.shownItems[i - 1].building?.name) {
                val headerModel = SchedulePlace().apply {
                    name = shownItems[i].building?.name
                    id = ID_HEADER
                }

                headersItems.add(headerModel)
                this.shownItems.add(i++, headerModel)
            }

            ++i
        }

        shownItems.add(SchedulePlace().apply {
            id = ID_ADD_BUTTON
        })

        notifyDataSetChanged()
    }

    fun setFilter(filterValue: String) {
        val mFiltered = this.items.filter { v ->
            v.name?.toLowerCase()?.contains(filterValue.toLowerCase()) == true
        }.sortedBy { v -> v.building?.name }

        shownItems.clear()
        shownItems.addAll(mFiltered)

        headersItems.clear()

        var i = 0
        while (i < shownItems.size) {
            if (i == 0 || this.shownItems[i].building?.name != this.shownItems[i - 1].building?.name) {
                val headerModel = SchedulePlace().apply {
                    name = shownItems[i].building?.name
                    id = ID_HEADER
                }

                headersItems.add(headerModel)
                this.shownItems.add(i++, headerModel)
            }

            ++i
        }

        shownItems.add(SchedulePlace().apply {
            id = ID_ADD_BUTTON
        })

        notifyDataSetChanged()

    }



    //  VH
    abstract class CommonViewHolder(view: View): RecyclerView.ViewHolder(view)

    inner class ViewHolder(view: View): CommonViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)

        lateinit var model: SchedulePlace

        init {
            view.setOnClickListener {
                delegate?.onTeacherSelect(model)
            }
        }

        fun bind(model: SchedulePlace) {
            this.model = model

            tvName.text = model.name
        }
    }

    inner class HeaderViewHolder(view: View): CommonViewHolder(view) {
        val tvValue: TextView = view.findViewById(R.id.tvValue)
    }

    inner class AddButtonViewHolder(view: View): CommonViewHolder(view) {
        init {
            view.setOnClickListener {
                delegate?.newAuditory()
            }
        }
    }



    companion object {
        const val ID_HEADER = "{header}"
        const val ID_ADD_BUTTON = "{add_auditory}"

        const val TYPE_HEADER = 1
        const val TYPE_ITEM = 2
        const val TYPE_ADD = 3
    }
}