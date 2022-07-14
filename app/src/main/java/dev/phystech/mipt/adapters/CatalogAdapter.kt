package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.models.ReferenceModel

class CatalogAdapter() :
        RecyclerView.Adapter<CatalogAdapter.ViewHolder>() {

    interface Delegate {
        fun onItemClick(model: ReferenceModel)
    }

    var delegate: Delegate? = null

    var items: ArrayList<ReferenceModel> = ArrayList()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_catalog, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(items[position])
    }

    override fun getItemCount() = items.size




    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
                delegate?.onItemClick(model)
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
    }
}

