package dev.phystech.mipt.ui.fragments.navigator.place_detail

import dev.phystech.mipt.adapters.ConvenienceAdapter
import dev.phystech.mipt.adapters.LegendsAdapter
import dev.phystech.mipt.adapters.PlaceImagesAdapter
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.LegendModel

interface PlaceDetailContract {
    interface View: BaseView {
        fun setImagesAdapter(adapter: PlaceImagesAdapter)
        fun setTagsAdapter(adapter: ConvenienceAdapter)
        fun setLegendAdapter(adapter: LegendsAdapter)
        fun showLegend(view: android.view.View, withModel: LegendModel)
        fun setPlaceType(type: String)
        fun showImageFullscreen(withStartPosition: Int)
    }

    abstract class Presenter: BasePresenter<View>() {

    }
}