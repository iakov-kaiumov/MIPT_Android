package dev.phystech.mipt.ui.fragments.navigator.images_fullscreen

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView

interface ImagesPlaceFullscreenContract {
    interface View: BaseView {
        fun setTitle(title: String)
        fun setPaginationTitle(text: String)
        fun <VH: RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun backPressed()
        abstract fun onPageUpdate(page: Int)
    }
}