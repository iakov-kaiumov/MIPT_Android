package dev.phystech.mipt.ui.fragments.navigator

import dev.phystech.mipt.adapters.CatalogAdapter
import dev.phystech.mipt.adapters.NavigationAdapter
import dev.phystech.mipt.adapters.SearchAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.ReferenceModel

interface NavigatorContract {
    interface View: BaseView {
        fun showSearch()
        fun showPager()
        fun showQRScanner()
        fun setSearchPlaceAdapter(adapter: SearchAdapter)
        fun setSearchLegendAdapter(adapter: SearchAdapter)
        fun setSearchReferenceAdapter(adapter: CatalogAdapter)
        fun setSearch(withValue: String)

        fun setHistoriesVisibility(isVisible: Boolean)
        fun setPlacesVisibility(isVisible: Boolean)
        fun setContactsVisibility(isVisible: Boolean)

        fun showContactPopup(forModel: ReferenceModel)
        fun showFragment(fragment: BaseFragment)

        fun setAdapter(adapter: NavigationAdapter)

        fun runOnUI(method: () -> Unit)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun updateSearch(value: String)
        abstract fun clearClicked()
        abstract fun qrCodeClicked()
        abstract fun ivSearchIconClicked()
    }
}