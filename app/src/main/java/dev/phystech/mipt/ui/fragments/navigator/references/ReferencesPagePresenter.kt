package dev.phystech.mipt.ui.fragments.navigator.references

import dev.phystech.mipt.adapters.CatalogAdapter
import dev.phystech.mipt.models.ReferenceModel
import dev.phystech.mipt.repositories.ContactsRepository

class ReferencesPagePresenter: ReferencesPageContract.Presenter(), CatalogAdapter.Delegate {

    val adapter: CatalogAdapter = CatalogAdapter()

    override fun attach(view: ReferencesPageContract.View?) {
        super.attach(view)
        view?.setAdapter(adapter)

        adapter.delegate = this


        ContactsRepository.shared.contacts.subscribe {
            adapter.items.clear()
            adapter.items.addAll(it.sortedBy { v -> v.order })
            adapter.notifyDataSetChanged()
        }
    }

    override fun onItemClick(model: ReferenceModel) {
        view.showContactPopup(model)
    }

}