package dev.phystech.mipt.base.interfaces

import io.reactivex.rxjava3.core.Single

interface BottomSheetController {
    fun showEventOptions()
    fun showSchedulersOptions()
    fun toSchedulers()
    fun toEvents()
    fun clickAddLesson()

    fun <T>showSelector(items: List<T>, onSelect: ((T) -> Unit)? = null)
}