package dev.phystech.mipt.ui.fragments.navigator.legends_page

import dev.phystech.mipt.adapters.LegendsAdapter
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.LegendModel

interface LegendsPageContract {
    interface View: BaseView {
        fun showLegendDetail(forView: android.view.View, withModel: LegendModel)
        fun setAdapter(adapter: LegendsAdapter)
    }

    abstract class Presenter: BasePresenter<View>() {

    }
}