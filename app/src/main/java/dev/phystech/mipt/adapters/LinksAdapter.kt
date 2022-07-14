package dev.phystech.mipt.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import dev.phystech.mipt.R

class LinksAdapter: RecyclerView.Adapter<LinksAdapter.ViewHolder>() {

    val items: ArrayList<LinkModel> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_link_text_field, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun getLinkById(id: Int): String? {
        items.firstOrNull { v -> v.id == id }?.let {
            return it.value
        }

        return null
    }


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val layout: TextInputLayout = view.findViewById(R.id.wrap)
        val editText: EditText = view.findViewById(R.id.etValue)

        lateinit var model: LinkModel

        init {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    model.value = s.toString()
                }
            })
        }

        fun bind(model: LinkModel) {
            this.model = model
            layout.hint = model.title
            editText.setText(model.value)
        }
    }

    data class LinkModel(
        val id: Int,
        val title: String,
        var value: String? = null
    )
}