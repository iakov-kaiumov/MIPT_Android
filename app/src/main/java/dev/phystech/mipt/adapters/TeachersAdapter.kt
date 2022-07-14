package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.shuhart.stickyheader.StickyAdapter
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import dev.phystech.mipt.R
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.ui.utils.AvatarColor

class TeachersAdapter: StickyAdapter<TeachersAdapter.HeaderViewHolder, TeachersAdapter.CommonViewHolder>() {

    interface Delegate {
        fun onTeacherSelect(model: Teacher)
        fun updateDisplayedCount(count: Int)
    }

    var delegate: Delegate? = null

    private val items: ArrayList<Teacher> = arrayListOf()
    private val headersItems: ArrayList<Teacher> = arrayListOf()
    private val shownItems: ArrayList<Teacher> = arrayListOf()

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

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        val item = shownItems[position]
        if (item.id == ID_HEADER) {
            holder as HeaderViewHolder
            holder.tvValue.text = item.name
        } else {
            holder as ViewHolder
            holder.bind(item)

        }
    }

    override fun getItemCount(): Int = this.shownItems.size

    override fun getItemViewType(position: Int): Int {
        return if (shownItems[position].id == ID_HEADER) TYPE_HEADER else TYPE_ITEM

    }



    //  Header
    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        shownItems[itemPosition].name?.first()?.let { firstLetter ->
            headersItems.firstOrNull { v -> v.name?.first() == firstLetter}?.let { firstItemSection ->
                return headersItems.indexOf(firstItemSection)
            }
        }

        return 0
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, headerPosition: Int) {
        val name = headersItems[headerPosition].name
        val char = name?.firstOrNull()?.toString()
        holder?.tvValue?.text = char
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_list_header, parent, false)

        view.findViewById<TextView>(R.id.tvValue).setText("_")

        return HeaderViewHolder(view)
    }



    //  OTHER
    fun setItems(items: List<Teacher>) {
        this.items.clear()
        this.items.addAll(items.sortedBy { v -> v.name })

        shownItems.clear()
        shownItems.addAll(items)

        var i = 0
        while (i < items.size) {
            val letter = this.items[i].name?.first()
            if (i == 0 || this.items[i].name?.first() != this.items[i - 1].name?.first()) {
                val headerModel = Teacher().apply {
                    name = letter.toString()
                    id = ID_HEADER
                }

                headersItems.add(headerModel)
                this.shownItems.add(i++, headerModel)
            }

            ++i
        }

        shownItems.sortBy { v -> v.name }

        delegate?.updateDisplayedCount(shownItems.size)
    }

    fun setFilter(filterValue: String) {
        val mFiltered = items.filter { v ->
            v.name?.toLowerCase()?.contains(filterValue.toLowerCase()) == true ||
                    v.post?.toLowerCase()?.contains(filterValue.toLowerCase()) == true
        }

        shownItems.clear()
        shownItems.addAll(mFiltered)
        shownItems.sortBy { v -> v.name }

        var i = 0
        while (i < shownItems.size) {
            val letter = shownItems[i].name?.first()
            if (i == 0 || shownItems[i].name?.first() != shownItems[i - 1].name?.first()) {
                val headerModel = Teacher().apply {
                    name = letter.toString()
                    id = ID_HEADER
                }

                headersItems.add(headerModel)
                shownItems.add(i++, headerModel)
            }

            ++i
        }

        shownItems.sortBy { v -> v.name }
        delegate?.updateDisplayedCount(shownItems.size)

        notifyDataSetChanged()
    }



    //  VH
    abstract class CommonViewHolder(view: View): RecyclerView.ViewHolder(view)

    inner class ViewHolder(view: View): CommonViewHolder(view) {
        val image: RoundedImageView = view.findViewById(R.id.image)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvChair: TextView = view.findViewById(R.id.tvChair)
        val tvAvatarDescription: TextView = view.findViewById(R.id.tvAvatarDescription)

        lateinit var model: Teacher

        init {
            view.setOnClickListener {
                delegate?.onTeacherSelect(model)
            }
        }

        fun bind(model: Teacher) {
            this.model = model

            tvName.text = model.name
            tvChair.text = ""

            model.image?.signatures?.firstOrNull().let { img ->
                if (img == null) {
                    val colorRandomPosition = model.name.hashCode() % 6
                    val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
                    image.setImageResource(avatarColor.colorResId)
                } else {
                    val url = NetworkUtils.getImageUrl(img.id, img.dir, img.path)
                    Picasso.get()
                        .load(url)
                        .into(image)
                }
            }

            tvAvatarDescription.text = model.name?.split(" ")?.mapNotNull { v -> v.firstOrNull() }?.take(2)?.joinToString(separator = "") { v -> v.toString() }
        }
    }

    inner class HeaderViewHolder(view: View): CommonViewHolder(view) {
        val tvValue: TextView = view.findViewById(R.id.tvValue)
    }



    companion object {
        const val ID_HEADER = "{header}"

        const val TYPE_HEADER = 1
        const val TYPE_ITEM = 2
    }
}