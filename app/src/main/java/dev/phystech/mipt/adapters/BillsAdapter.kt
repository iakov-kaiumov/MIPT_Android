package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R

interface OnBillClickListener {
    fun onClick()
}

class BillsAdapter(val onBillClickListener: OnBillClickListener) :
    RecyclerView.Adapter<BillsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_bill, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.setOnClickListener {
            onBillClickListener.onClick()
        }
    }

    override fun getItemCount() = 10
}

