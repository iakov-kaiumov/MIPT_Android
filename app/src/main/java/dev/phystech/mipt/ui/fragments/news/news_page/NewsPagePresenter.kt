package dev.phystech.mipt.ui.fragments.news.news_page

import dev.phystech.mipt.adapters.NewsAdapter
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.News
import dev.phystech.mipt.models.api.TagsDataResponseModel
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat

class NewsPagePresenter(private val contentType: News.Type): NewsPageContract.Presenter(),
    NewsAdapter.Delegate {

    private lateinit var adapter: NewsAdapter


    override fun attach(view: NewsPageContract.View?) {
        super.attach(view)
        adapter = NewsAdapter(contentType)
        adapter.delegate = this
        view?.setAdapter(adapter)

        val df = SimpleDateFormat("dd.MM.yyyy HH:mm")
        NewsRepository2.shared.getNewsObserbavleByType(contentType)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe {
                it.sortByDescending { v -> df.parse(v.serverDate) }
                adapter.items.clear()
                adapter.items.addAll(it)
                adapter.notifyDataSetChanged()
            }
    }

    //  ADAPTER DELEGATE
    override fun onLastItemLoad() {
        NewsRepository2.shared.loadNextPage(contentType)
        when (contentType) {


//            LegendModel.Type.Event -> {
//                EventRepository.shared.loadNextPage()
//            }

            else -> {
//                NewsRepository.shared.loadNextPage(contentType)
            }
        }
    }

    override fun onNewsClick(model: News) {
        view.showNewsDetail(model)
    }


}