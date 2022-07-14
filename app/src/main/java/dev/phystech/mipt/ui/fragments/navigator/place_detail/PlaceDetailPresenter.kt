package dev.phystech.mipt.ui.fragments.navigator.place_detail

import android.view.View
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import dev.phystech.mipt.adapters.ConvenienceAdapter
import dev.phystech.mipt.adapters.LegendsAdapter
import dev.phystech.mipt.adapters.PlaceImagesAdapter
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.models.api.*
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.TagRepository

class PlaceDetailPresenter(private val model: PlaceModel): PlaceDetailContract.Presenter(),
    LegendsAdapter.Delegate, PlaceImagesAdapter.Delegate {

    private val imagesAdapter: PlaceImagesAdapter = PlaceImagesAdapter()
    private val tagsAdapter: ConvenienceAdapter = ConvenienceAdapter()
    private val legendsAdapter: LegendsAdapter = LegendsAdapter()

    private val legends: ArrayList<LegendModel> = ArrayList()
    private val tags: ArrayList<Tag> = ArrayList()

    override fun attach(view: PlaceDetailContract.View?) {
        super.attach(view)

        imagesAdapter.items = ArrayList(model.allImagesUrls)   //?.signatures ?: ArrayList()
        tagsAdapter.items = tags
        legendsAdapter.items = legends

        view?.setImagesAdapter(imagesAdapter)
        view?.setTagsAdapter(tagsAdapter)
        view?.setLegendAdapter(legendsAdapter)

        legendsAdapter.delegate = this
        imagesAdapter.delegate = this


        model.postsID.forEach { id ->
            id?.let {
                ApiClient.shared.getHistoryById(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val items = it.data?.paginator?.values?.map {
                            val model = LegendModel()
                            model.date = it.created?.date

                            it.summaryImage?.signatures?.firstOrNull()?.let {
                                model.imageUrl = NetworkUtils.getImageUrl(it.id, it.dir, it.path)
                            }

                            model.title = it.title
                            model.place = it.place
                            model.content = it.content
                            model.id = it.id

                            return@map model
                        }

                        items?.let {
                            it.forEach {
                                if (legends.firstOrNull { v -> v.id == it.id } == null) {
                                    legends.add(it)
                                }
                            }
                        }

                        legendsAdapter.notifyDataSetChanged()
                    }, {})
            }
        }

        tags.clear()
        model.tags.forEach { tagItem ->
            tagItem.id?.let { tagId ->

                TagRepository.shared.getTag(tagId) { tag ->
                    if (tag.tagType == TagsDataResponseModel.TagType.Use) {
                        view?.setPlaceType(tag.name ?: "")
                    } else if (tag.tagType == TagsDataResponseModel.TagType.Info) {
                        tag.image?.signatures?.firstOrNull()?.let {
                            tagItem.imageUrl = NetworkUtils.getImageUrl(it.id, it.dir, it.path)
                        }

                        tags.add(tagItem)

                        tagsAdapter.notifyDataSetChanged()
                    }
                }

            }
        }
    }


    //  LEGEND ADAPTER
    override fun onSelect(view: View, withModel: LegendModel) {
        this.view.showLegend(view, withModel)
    }

    //  IMAGES ADAPTER
    override fun onSelect(model: String) {
        val selectedImagePosition = imagesAdapter.items.indexOf(model)
        view.showImageFullscreen(selectedImagePosition)


    }

}
