package dev.phystech.mipt.ui.fragments.navigator.place_page

import dev.phystech.mipt.adapters.PlacesAdapter
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.PlacesRepository
import dev.phystech.mipt.repositories.TagRepository

class PlacePagePresenter: PlacePageContract.Presenter(), PlacesAdapter.Delegate {

    private lateinit var adapter: PlacesAdapter

    override fun attach(view: PlacePageContract.View?) {
        super.attach(view)

        adapter = PlacesAdapter(this)

        view?.setAdapter(adapter)

        PlacesRepository.shared.isLoading.subscribe {
            if (it) view?.showProgress()
            else view?.hideProgress()
        }

        PlacesRepository.shared.places.subscribe {
            adapter.items.clear()
            adapter.items.addAll(it)
//            adapter.items.addAll(it.map { v ->
//
//                v.tags.forEach { tag ->
//                    tag.id?.let { tagId ->
//                        if (!TagRepository.shared.containsTag(tagId)) {
//                            TagRepository.shared.loadTagInBackground(tagId)
//                        }
//                    }
//                }
//
//                val model = PlaceModel(
//                    v.name ?: "-",
//                    "",
//                    v.place?.address,
//                    ""
//                ).apply {
//                    tags = v.tags
//                    fromModel = v
//                }
//
//                v.image?.signatures?.firstOrNull()?.let { image ->
//                    model.imageUrl = NetworkUtils.getImageUrl(image.id, image.dir, image.path)
//                }
//
//                return@map model
//            })
            adapter.notifyDataSetChanged()
        }

    }

    override fun onSelect(model: PlaceModel) {
        view.showPlaceDetail(model)

    }

}