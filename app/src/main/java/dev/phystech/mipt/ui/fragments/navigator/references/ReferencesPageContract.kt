package dev.phystech.mipt.ui.fragments.navigator.references

import dev.phystech.mipt.adapters.CatalogAdapter
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.ReferenceModel

interface ReferencesPageContract {
    interface View: BaseView {
        fun setAdapter(adapter: CatalogAdapter)
        fun showContactPopup(forModel: ReferenceModel)
    }

    abstract class Presenter: BasePresenter<View>() {

    }
}