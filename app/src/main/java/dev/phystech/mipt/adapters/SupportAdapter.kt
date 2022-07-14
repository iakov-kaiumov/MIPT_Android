package dev.phystech.mipt.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R

class SupportAdapter(
    private val context: Context
) :
    RecyclerView.Adapter<SupportAdapter.BaseViewHolder<*>>() {
    private var adapterDataList: List<Any> = emptyList()

    companion object {
        private const val TYPE_INCOME = 0
        private const val TYPE_INCOME_TEMPLATE = 1
        private const val TYPE_OUTCOME = 2
    }


    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    inner class IncomeViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        override fun bind(item: String) {
            TODO("Not yet implemented")
        }
    }

    inner class IncomeTemplateViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        override fun bind(item: String) {
            TODO("Not yet implemented")
        }
    }

    inner class OutcomeViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {
        override fun bind(item: String) {
            TODO("Not yet implemented")
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            TYPE_INCOME -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_income, parent, false)
                IncomeViewHolder(view)
            }
            TYPE_INCOME_TEMPLATE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_income_template, parent, false)
                IncomeTemplateViewHolder(view)
            }
            TYPE_OUTCOME -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_outcome, parent, false)
                OutcomeViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        //TODO:
        return 0
    }

}
