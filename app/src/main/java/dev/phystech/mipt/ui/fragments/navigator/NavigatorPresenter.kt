package dev.phystech.mipt.ui.fragments.navigator

import android.os.Looper
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.adapters.CatalogAdapter
import dev.phystech.mipt.adapters.NavigationAdapter
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.NavigationItemModel
import dev.phystech.mipt.models.ReferenceModel
import dev.phystech.mipt.repositories.ContactsRepository
import dev.phystech.mipt.repositories.HistoryRepository
import dev.phystech.mipt.repositories.PlacesRepository
import dev.phystech.mipt.ui.fragments.navigator.legend_detail.LegendDetailFragment
import dev.phystech.mipt.ui.fragments.navigator.place_detail.PlaceDetailFragment
import java.util.*
import kotlin.collections.ArrayList

class NavigatorPresenter: NavigatorContract.Presenter(), CatalogAdapter.Delegate,
    NavigationAdapter.Delegate {

    private var searchValue: String = ""

    private val places: ArrayList<LegendModel> = ArrayList()
    private val histories: ArrayList<LegendModel> = ArrayList()
    private val contacts: ArrayList<ReferenceModel> = ArrayList()

//    private lateinit var placeAdapter: SearchAdapter
//    private lateinit var historiesAdapter: SearchAdapter
//    private lateinit var contactsAdapter: CatalogAdapter

    private lateinit var navAdapter: NavigationAdapter

    var items: ArrayList<NavigationItemModel> = ArrayList()

    private var searchField: BehaviorSubject<String> = BehaviorSubject.create()

    override fun attach(view: NavigatorContract.View?) {
        super.attach(view)

//        placeAdapter = SearchAdapter()
//        historiesAdapter = SearchAdapter()
//        contactsAdapter = CatalogAdapter()

        navAdapter = NavigationAdapter(items)
        navAdapter.delegate = this
        navAdapter.items = items
        view?.setAdapter(navAdapter)

//        placeAdapter.items = places
//        historiesAdapter.items = histories
//        contactsAdapter.items = contacts

//        placeAdapter.delegate = this
//        historiesAdapter.delegate = this
//        contactsAdapter.delegate = this

//        view?.setSearchPlaceAdapter(placeAdapter)
//        view?.setSearchLegendAdapter(historiesAdapter)
//        view?.setSearchReferenceAdapter(contactsAdapter)


        HistoryRepository.shared.histories.subscribe {
//            val values = it.values.map { v ->
//                val model = LegendModel()
//                model.title = v.title
//                model.place = v.place
//                model.content = v.content
//                v.summaryImage?.signatures?.firstOrNull()?.let {
//                    model.imageUrl = NetworkUtils.getImageUrl(it.id, it.dir, it.path)
//                }
//
//                return@map model
//            }

            histories.clear()
            histories.addAll(it)
//            historiesAdapter.notifyDataSetChanged()
        }

        PlacesRepository.shared.places.subscribe {
            val values = it.map { v ->
                val model = LegendModel()
                model.title = v.name
                model.place = v.address
                model.content = v.description
                model.id = v.id
                model.placeSource = v
//                model.placeSource = v
                model.imageUrl = v.avatarImageUrl
//                v.image?.signatures?.firstOrNull()?.let {
//                    model.imageUrl = NetworkUtils.getImageUrl(it.id, it.dir, it.path)
//                }
                return@map model
            }

            places.clear()
            places.addAll(values)
//            placeAdapter.notifyDataSetChanged()
        }

        ContactsRepository.shared.contacts.subscribe {
            contacts.clear()
            contacts.addAll(it)
//            contactsAdapter.notifyDataSetChanged()
        }

    }



    var computationThread: Thread? = null
    override fun updateSearch(value: String) {
        searchValue = value

        if (value.isNotEmpty()) {
            view.showSearch()

            Schedulers.computation().run {
                val filteredPlaces = places.filter { v ->
                    v.content?.toLowerCase(Locale.getDefault())?.contains(value.toLowerCase(Locale.getDefault())) == true ||
                            v.place?.toLowerCase(Locale.getDefault())?.contains(value.toLowerCase(Locale.getDefault())) == true ||
                            v.title?.toLowerCase(Locale.getDefault())?.contains(value.toLowerCase(Locale.getDefault())) == true
                }.map { v -> NavigationItemModel().apply {
                    legendModel = v
                    type = NavigationItemModel.Type.Place
                } }.take(2)

                val filteredHistories = histories.filter { v ->
                    v.content?.toLowerCase(Locale.getDefault())?.contains(value.toLowerCase(Locale.getDefault())) == true ||
                            v.place?.toLowerCase(Locale.getDefault())?.contains(value.toLowerCase(Locale.getDefault())) == true ||
                            v.title?.toLowerCase(Locale.getDefault())?.contains(value.toLowerCase(Locale.getDefault())) == true
                }.map { v -> NavigationItemModel().apply {
                    legendModel = v
                    type = NavigationItemModel.Type.Legend
                } }.take(2)

                val filteredContacts = contacts.filter { v ->
                    v.location.contains(value) ||
                            v.name.toLowerCase(Locale.getDefault()).contains(value.toLowerCase(Locale.getDefault())) ||
                            v.place.toLowerCase(Locale.getDefault()).contains(value.toLowerCase(Locale.getDefault())) ||
                            v.occupation.toLowerCase(Locale.getDefault()).contains(value.toLowerCase(Locale.getDefault()))
                }.map { v -> NavigationItemModel().apply {
                    reference = v
                    type = NavigationItemModel.Type.Contact
                } }.sortedBy { v -> v.reference?.order }


                items.clear()
                if (filteredPlaces.isNotEmpty()) {
                    items.add(NavigationItemModel.withTitle("Места"))
                    items.addAll(filteredPlaces)
                }
                if (filteredHistories.isNotEmpty()) {
                    items.add(NavigationItemModel.withTitle("Легенды"))
                    items.addAll(filteredHistories)
                }
                if (filteredContacts.isNotEmpty()) {
                    items.add(NavigationItemModel.withTitle("Справочник"))
                    items.addAll(filteredContacts)
                }
                navAdapter.notifyDataSetChanged()
                view.hideProgress()

//                placeAdapter.items = ArrayList(filteredPlaces.distinctBy { v -> v.id })
//                historiesAdapter.items = ArrayList(filteredHistories)
//                contactsAdapter.items = ArrayList(filteredContacts)


//                if (computationThread == Thread.currentThread()) {
//                    view.runOnUI {
//
//                        placeAdapter.notifyDataSetChanged()
//                        historiesAdapter.notifyDataSetChanged()
//                        contactsAdapter.notifyDataSetChanged()
//
//                        view?.setHistoriesVisibility(filteredHistories.isNotEmpty())
//                        view?.setPlacesVisibility(filteredPlaces.isNotEmpty())
//                        view?.setContactsVisibility(filteredContacts.isNotEmpty())
//                    }
//                }
            }

            computationThread?.start()


        } else {
            view.showPager()
        }
    }

    override fun clearClicked() {
        searchValue = String.empty()
        view.setSearch(String.empty())
    }

    override fun qrCodeClicked() {
        view.showQRScanner()
    }

    override fun ivSearchIconClicked() {
        if (searchValue.isNotEmpty()) {
            clearClicked()
        } else {
            //  TODO: Show microphone
        }
    }


    override fun onItemClick(model: ReferenceModel) {

    }


    //  ADAPTER DELEGATE
    override fun onLegendSelected(legendModel: LegendModel) {
        val fragment = if (legendModel.placeSource != null) {
            PlaceDetailFragment(legendModel.placeSource!!)
        } else {
            LegendDetailFragment(legendModel)
        }

        view.showFragment(fragment)
    }

    override fun onReferenceSelected(referenceModel: ReferenceModel) {
        view.showContactPopup(referenceModel)
    }
}