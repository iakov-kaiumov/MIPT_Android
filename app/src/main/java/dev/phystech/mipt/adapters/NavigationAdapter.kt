package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.NavigationItemModel
import dev.phystech.mipt.models.ReferenceModel

class NavigationAdapter(var items: ArrayList<NavigationItemModel>): RecyclerView.Adapter<NavigationAdapter.BaseViewHolder>() {

    interface Delegate {
        fun onLegendSelected(legendModel: LegendModel)
        fun onReferenceSelected(referenceModel: ReferenceModel)
    }

    var delegate: Delegate? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            NavigationItemModel.Type.Legend.ordinal -> {
                val view = inflater.inflate(R.layout.item_search, parent, false)
                return LegendViewHolder(view)
            }
            NavigationItemModel.Type.Place.ordinal -> {
                val view = inflater.inflate(R.layout.item_search, parent, false)
                return LegendViewHolder(view)
            }
            NavigationItemModel.Type.Contact.ordinal -> {
                val view = inflater.inflate(R.layout.item_catalog, parent, false)
                return ReferenceViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_navigation_title, parent, false)
                return TitleViewHolder(view)
            }
        }


    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type?.ordinal ?: -1
    }


    //  VIEW HOLDERS
    abstract inner class BaseViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(model: NavigationItemModel)
    }

    inner class TitleViewHolder(view: View): BaseViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvPlacesTitle)
        override fun bind(model: NavigationItemModel) {
            tvTitle.text = model.title
        }
    }

    inner class LegendViewHolder(view: View): BaseViewHolder(view) {
        private lateinit var model: LegendModel

        private val ivImage: ImageView = view.findViewById(R.id.ivImage)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvPlaceType: TextView = view.findViewById(R.id.tvPlaceType)
        private val tvDescription: TextView = view.findViewById(R.id.tvDescription)

        init {
            view.setOnClickListener {
                delegate?.onLegendSelected(model)
            }
        }

        fun bind(model: LegendModel) {
            this.model = model
            tvTitle.text = model.title
            tvPlaceType.text = model.place
//            tvDescription.text = model.

            model.imageUrl?.let {
                Picasso.get()
                    .load(it)
                    .into(ivImage)
            }
        }

        override fun bind(model: NavigationItemModel) {
            model.legendModel?.let {
                bind(it)
            }

        }

    }

    inner class ReferenceViewHolder(view: View) : BaseViewHolder(view) {
        private lateinit var model: ReferenceModel

        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvOccupation: TextView = view.findViewById(R.id.tvOccupation)
        private val tvDepartment: TextView = view.findViewById(R.id.tvDepartment)
        private val tvInnerPhone: TextView = view.findViewById(R.id.tvInnerPhone)
        private val tvPersonalPhone: TextView = view.findViewById(R.id.tvPersonalPhone)
        private val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        private val tvRight: TextView = view.findViewById(R.id.tvRight)

        init {
            view.setOnClickListener {
                delegate?.onReferenceSelected(model)
            }
        }

        fun bind(model: ReferenceModel) {
            this.model = model

            tvTitle.text = model.name
            tvOccupation.text = model.occupation
            tvDepartment.text = model.place
            tvInnerPhone.text = model.innerPhoneNumber
            tvPersonalPhone.text = model.personalPhoneNumber
            tvEmail.text = model.email
            tvRight.text = model.location
        }

        override fun bind(model: NavigationItemModel) {
            model.reference?.let { referenceModel ->
                bind(referenceModel)
            }
        }
    }

}