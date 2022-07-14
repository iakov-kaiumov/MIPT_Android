package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.BaseApiEntity
import dev.phystech.mipt.repositories.ChairTopicRepository

class ChairSettingsAdapter(val items: ArrayList<BaseApiEntity.Chair>): RecyclerView.Adapter<ChairSettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_settings_toggle, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private var model: BaseApiEntity.Chair? = null

        private val tvValue: TextView = view.findViewById(R.id.tvValue)
        private val toggle: SwitchCompat = view.findViewById(R.id.mSwitch)

        init {
            toggle.setOnCheckedChangeListener { _, isChecked ->
                model?.let {
                    ChairTopicRepository.shared.setTopicEnabled(it.id ?: "", isChecked)
                }
            }
        }


        fun bind(model: BaseApiEntity.Chair) {
            this.model = model

            tvValue.text = model.name
            model.id?.let { modelID ->
                toggle.isChecked = ChairTopicRepository.shared.getTopciEnabled(modelID)
            }
        }
    }

}