package dev.phystech.mipt.repositories

import android.content.Context
import dev.phystech.mipt.Application
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.models.News
import dev.phystech.mipt.models.News.Type.*
import dev.phystech.mipt.models.api.EventDataResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.realm.Realm
import kotlinx.coroutines.sync.Mutex

class NewsRepository2 private constructor() {

    private var countInLoad = 0
    private val realm = Realm.getDefaultInstance()
    private val pref = Application.context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()

    val newsNews: BehaviorSubject<ArrayList<News>> = BehaviorSubject.create()
    val newsEvents: BehaviorSubject<ArrayList<News>> = BehaviorSubject.create()

    var newsEventPage = 1
    var newsNewsPage = 1
    var itemPerPage = 20

    var filterDisposable: Disposable? = null
    var newsFilterMap: Map<String, String>? = null
    var eventFilterMap: Map<String, String>? = null

    init {
        filterDisposable = ChairFilterStorage.shared.disabledChairs
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loadData()
            }, {})

    }

    fun loadData() {

        ChairRepository.shared.allNewsChairs
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val idList = ChairRepository.shared.allNewsChairs.value.mapNotNull { v -> v.id }.toMutableList()
                val disabled = ChairFilterStorage.shared.disabledChairs.value
                idList.removeAll(disabled)
                val map = idList.mapIndexed { index, model -> "filter[chairs.id][$index]" to model}.toMap()
                newsFilterMap = map

                ApiClient.shared.getNewsFiltered(chairs = map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.data?.paginator?.values?.let {
                            val news = transformModelsFromResponse(it)
                            newsNews.onNext(news)
                        }
                    }, {
                        print(it)
                    })

                if (ChairRepository.shared.allEventChairs.hasValue().not()) return@subscribe

                val eventChairFilterIdList = ChairRepository.shared.allEventChairs.value.mapNotNull { v -> v.id }.toMutableList()
                eventChairFilterIdList.removeAll(disabled)
                val eventMap = eventChairFilterIdList.mapIndexed { index, model -> "filter[chairs.id][$index]" to model}.toMap()

                eventFilterMap = eventMap
                ApiClient.shared.getEvents(filterChairMap = map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({
                        it.data?.paginator?.values?.let {
                            val events = transformModelsFromResponse(it)
                            newsEvents.onNext(events)
                        }
                    }, {
                        print(it)
                    })
            })


    }

    fun loadNextPage(forType: News.Type): Boolean {
        when (forType) {
            News.Type.News -> {
                ApiClient.shared.getNewsFiltered(
                    chairs = newsFilterMap ?: emptyMap(),
                    perPage = itemPerPage,
                    page = newsNewsPage + 1
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.status.isSuccess) {
                            newsNewsPage += 1
                            it.data?.paginator?.values?.let {
                                val news = transformModelsFromResponse(it)
                                newsNews.value.addAll(news)
                                newsNews.onNext(newsNews.value)
                                if (news.size == 0) {
                                    newsNewsPage = -1
                                }
                            }
                        }
                    }, {})


                return newsNewsPage != -1
            }
            Events -> {
                ApiClient.shared.getEvents(
                    filterChairMap = eventFilterMap ?: emptyMap(),
                    page = newsEventPage + 1
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it.status.isSuccess) {
                            newsEventPage += 1
                            it.data?.paginator?.values?.let {
                                val news = transformModelsFromResponse(it)
                                newsEvents.value.addAll(news)
                                newsEvents.onNext(newsEvents.value)
                                if (news.size == 0) {
                                    newsEventPage = -1
                                }
                            }
                        }
                    }, {})


                return newsEventPage != -1
            }
        }

        return false
    }


    fun getNewsObserbavleByType(type: News.Type): Observable<ArrayList<News>>? {
        return when (type) {
            News.Type.News -> newsNews
            Events -> newsEvents
            else -> null
        }
    }

    private fun transformModelsFromResponse(response: Collection<EventDataResponseModel.PaginatorItem>, withChair: Boolean = false): ArrayList<News> {
        return ArrayList(response.map { v -> News.mapFromPaginatorItem(v, withChair) })
    }




    companion object {
        val shared: NewsRepository2 = NewsRepository2()

        private const val PREF_KEY = "news.pref_name"
        private const val IS_FIRST_LOUNCH_KEY = "news.first_config"
    }
}
