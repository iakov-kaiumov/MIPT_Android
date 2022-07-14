package dev.phystech.mipt.ui.fragments.navigator.legends_page

import android.view.View
import dev.phystech.mipt.adapters.LegendsAdapter
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.HistoryRepository
import dev.phystech.mipt.repositories.PlacesRepository

class LegendsPagePresenter: LegendsPageContract.Presenter(), LegendsAdapter.Delegate {

    private lateinit var legendAdapter: LegendsAdapter

    override fun attach(view: LegendsPageContract.View?) {
        super.attach(view)

        legendAdapter = LegendsAdapter()
        legendAdapter.delegate = this
        view?.setAdapter(legendAdapter)

        HistoryRepository.shared.histories.subscribe {
            legendAdapter.items.removeAll { v -> v.type == LegendModel.Type.History }
            legendAdapter.items.addAll(it
//                it.values.map { v ->
//                val model = LegendModel()
//                model.content = v.content
//                model.title = v.title
//                model.type = LegendModel.Type.History
//                v.summaryImage?.signatures?.firstOrNull()?.let { image ->
//                    model.imageUrl = NetworkUtils.getImageUrl(image.id, image.dir, image.path)
//                }
//                model.legendSource = v
//
//                return@map model
//            }
            )

            legendAdapter.notifyDataSetChanged()
        }

    }



    //  LEGEND ADAPTER DELEGATE
    override fun onSelect(view: View, withModel: LegendModel) {
        this.view.showLegendDetail(view, withModel)

    }
}


/*        NewsRepository.shared.news.subscribe {
            legendAdapter.items.removeAll { v -> v.type == LegendModel.Type.News }
            legendAdapter.items.addAll(it.values.map { v ->
                val model = LegendModel()
                model.content = v.content
                model.title = v.title
                model.type = LegendModel.Type.News

                return@map model
            })

            legendAdapter.notifyDataSetChanged()
        }

        EventRepository.shared.events.subscribe {
            legendAdapter.items.removeAll { v -> v.type == LegendModel.Type.Event }
            legendAdapter.items.addAll(it.values.map { v ->
                val model = LegendModel()
                model.content = v.content
                model.title = v.title
                model.type = LegendModel.Type.Event

                return@map model
            })

            legendAdapter.notifyDataSetChanged()
        }
*/