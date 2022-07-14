package dev.phystech.mipt.adapters.scheduler_filtred

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.adapters.SchedulerFiltredListAdapter
import dev.phystech.mipt.models.SchedulerFilterModel
import dev.phystech.mipt.models.SchedulerIndex
import dev.phystech.mipt.models.SchedulerIndexModel

sealed class SchedulerFiltredListAdapterModel {
    abstract fun getTypeIndex(): Int
    abstract fun getType(): Type
}

class FilterAdapterModelHelper(val model: SchedulerFilterModel = SchedulerFilterModel()): SchedulerFiltredListAdapterModel() {
    var isShown: Boolean = false

    override fun getTypeIndex(): Int = getType().typeIndex
    override fun getType(): Type = Type.Filter
}

class SchedulerAdapterModelHelper(val model: SchedulerIndex): SchedulerFiltredListAdapterModel() {
    override fun getTypeIndex(): Int = getType().typeIndex
    override fun getType(): Type = Type.Item
}

class SchedulerAdapterEmptyHelper(): SchedulerFiltredListAdapterModel() {
    override fun getTypeIndex(): Int = getType().typeIndex
    override fun getType(): Type = Type.Empty
}