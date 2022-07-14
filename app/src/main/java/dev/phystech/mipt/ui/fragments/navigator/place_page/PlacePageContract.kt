package dev.phystech.mipt.ui.fragments.navigator.place_page

import dev.phystech.mipt.adapters.PlacesAdapter
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.models.api.PlacePaginatorItem
import dev.phystech.mipt.models.api.PlacesDataResponseModel

interface PlacePageContract {
    interface View: BaseView {
        fun setAdapter(adapter: PlacesAdapter)
        fun showPlaceDetail(forModel: PlaceModel)

    }

    abstract class Presenter: BasePresenter<View>() {

    }
}