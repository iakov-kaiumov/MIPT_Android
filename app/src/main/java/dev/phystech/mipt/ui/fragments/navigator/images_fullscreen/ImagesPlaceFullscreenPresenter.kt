package dev.phystech.mipt.ui.fragments.navigator.images_fullscreen

import dev.phystech.mipt.adapters.ImagesFullScreenAdapter
import dev.phystech.mipt.adapters.PlaceImagesAdapter
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.repositories.PlacesRepository
import java.text.FieldPosition

class ImagesPlaceFullscreenPresenter(
    private val modelID: String,
    private val fromPosition: Int
    ): ImagesPlaceFullscreenContract.Presenter() {

    val adapter: ImagesFullScreenAdapter = ImagesFullScreenAdapter()

    override fun attach(view: ImagesPlaceFullscreenContract.View?) {
        super.attach(view)

        PlacesRepository.shared.getById(modelID) {
            it?.let { updateModel(it) }
        }
    }


    override fun backPressed() {

    }

    override fun onPageUpdate(page: Int) {
        view?.setPaginationTitle("${page + 1} из ${adapter.items.size}")
    }


    private fun updateModel(placeModel: PlaceModel) {
        val images = ArrayList(placeModel.allImagesUrls)
        adapter.items = images
        adapter.notifyDataSetChanged()
        view.setAdapter(adapter)

        view?.setTitle(placeModel.name ?: "")
    }
}