package dev.phystech.mipt.repositories

import android.content.Context
import android.util.Log
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
import io.realm.Realm

class NewsRepository private constructor() {

    enum class EventType(val type: String) {
        Chair("chair"),
        Outer("outer"),
        MIPT("inner")
    }

    private var countInLoad = 0
    private val realm = Realm.getDefaultInstance()
    private val pref = Application.context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()

    val newsNews: BehaviorSubject<ArrayList<News>> = BehaviorSubject.create()
    val newsChair: BehaviorSubject<ArrayList<News>> = BehaviorSubject.create()
    val newsEvents: BehaviorSubject<ArrayList<News>> = BehaviorSubject.create()

    var newsEventPage = 1
    var newsChairPage = 1
    var newsOuterNewsPage = 1


    init {
        if (pref.getBoolean(IS_FIRST_LOUNCH_KEY, true)) {
            executeFirstConfig()
        }

        val realmEvents = realm.where(News::class.java).findAll()
            .filter { v -> v.typeId == Events.index }
        val realmChair = realm.where(News::class.java).findAll()
            .filter { v -> v.typeId == Chair.index }
        val realmOuter = realm.where(News::class.java).findAll()
            .filter { v -> v.typeId == News.Type.News.index }

        newsNews.onNext(ArrayList(realmOuter))  //realmNews
        newsChair.onNext(ArrayList(realmChair))
        newsEvents.onNext(ArrayList(realmEvents))

        Log.d("APP_REPOSITORY", "Load from Realm ${realmEvents.size} Event items; [${this}]")
        Log.d("APP_REPOSITORY", "Load from Realm ${realmChair.size} Chair items; [${this}]")
        Log.d("APP_REPOSITORY", "Load from Realm ${realmOuter.size} Outer items; [${this}]")
    }

    fun loadData(callback: ((Boolean) -> Unit)? = null) {
        Log.d("APP_REPOSITORY", "loadData; [${this}]")
        isLoading.onNext(true)

        ApiClient.shared.getNewsForCategory(EventType.Outer.type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleSuccess, this::handleError)

        ApiClient.shared.getEvents()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleSuccess, this::handleError)


        ApiClient.shared.getNewsForCategory(EventType.Chair.type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleSuccess, this::handleError)
    }

    fun loadNextPage(forType: News.Type): Boolean {
        Log.d("APP_REPOSITORY", "loadNextPage(forType[${forType}]); [${this}]")
        when (forType) {
            Events -> {
//                newsOuterNews
                if (newsEventPage == -1) return false
                ApiClient.shared.getNewsForCategory(EventType.Outer.type, newsEventPage + 1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handlePageLoadSuccess, {})
            }

            Chair -> {
                if (newsChairPage == -1) return false
                ApiClient.shared.getNewsForCategory(EventType.Chair.type, newsChairPage + 1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handlePageLoadSuccess, {})
            }

            News.Type.News -> {
                if (newsOuterNewsPage == -1) return false
                ApiClient.shared.getNewsForCategory(EventType.Outer.type, newsOuterNewsPage + 1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handlePageLoadSuccess, {})

            }
        }

        return false
    }

    fun getNewsObserbavleByType(type: News.Type): Observable<ArrayList<News>> {
        return when (type) {
            News.Type.News -> newsNews
            Chair -> newsChair
            Events -> newsEvents
        }
    }

    //  LOAD DATA HANDLERS
    fun handleSuccess(it: EventDataResponseModel) {
        Log.d("APP_REPOSITORY", "handleSuccess(it); [${this}]")
        countInLoad -= 1
        isLoading.onNext(countInLoad > 0)

        if (it.status.isSuccess) {
            val apiItems = it.data?.paginator?.values ?: return
            val fromChair = it.data?.filter?.eventCategory == EventType.Chair.type
            val items = transformModelsFromResponse(ArrayList(apiItems), fromChair)

            when (it.data?.filter?.eventCategory) {
                EventType.Chair.type -> {
                    items.forEach { v -> v.typeId = Chair.index }
                    newsChair.onNext(items)
                    newsNews.onNext(newsNews.value)
                }
                EventType.Outer.type -> {
                    items.forEach { v -> v.typeId = Events.index }
                    newsNews.onNext(items)
                }
                else -> {
                    items.forEach { v -> v.typeId = News.Type.News.index }
                    newsEvents.onNext(items)
                }
            }

            saveModelsInRealm(items)
        }
    }

    fun handlePageLoadSuccess(it: EventDataResponseModel) {
        Log.d("APP_REPOSITORY", "handlePageLoadSuccess(it); [${this}]")
        if (it.status.isSuccess) {
            val category = it.data?.filter?.eventCategory
            val items = transformModelsFromResponse(it.data?.paginator?.values ?: ArrayList(), category == EventType.Chair.type)
            val isLastPage = items.size == 0

            when (category) {
                EventType.Chair.type -> {
                    newsChairPage = if (isLastPage) -1  else newsChairPage + 1
                    items.forEach { v -> v.typeId = Chair.index }
                    newsChair.value?.let {
                        it.addAll(items)
                        newsChair.onNext(it)
                    }
                }
                EventType.Outer.type -> {
                    newsOuterNewsPage = if (isLastPage) -1  else newsOuterNewsPage + 1
                    items.forEach { v -> v.typeId = Events.index }
                    newsNews.value?.let {
                        it.addAll(items)
                        newsNews.onNext(it)
                    }
                }
                else -> {
                    newsEventPage = if (isLastPage) -1  else newsEventPage + 1
                    items.forEach { v -> v.typeId = News.Type.News.index }
                    newsEvents.value?.let {
                        it.addAll(items)
                        newsEvents.onNext(it)
                    }
                }
            }

            saveModelsInRealm(items)
        }
    }

    fun handleError(error: Throwable) {
        Log.d("APP_REPOSITORY", "handleError(); [${this}]")
        countInLoad -= 1
        isLoading.onNext(countInLoad > 0)
    }


    //  OTHERS
    private fun executeFirstConfig() {
        Log.d("APP_REPOSITORY", "executeFirstConfig() [${this}]")
        pref.edit().putBoolean(IS_FIRST_LOUNCH_KEY, false).apply()

        AssetsRepository.shared?.getEvents()?.data?.paginator?.let { data ->
            val models = transformModelsFromResponse(data.values)
            models.forEach { v -> v.typeId = Events.index }
            saveModelsInRealm(models)
        }
        AssetsRepository.shared?.getNewsChair()?.data?.paginator?.let { data ->
            val models = transformModelsFromResponse(data.values)
            models.forEach { v -> v.typeId = Chair.index }
            saveModelsInRealm(models)
        }
        AssetsRepository.shared?.getNewsOuter()?.data?.paginator?.let { data ->
            val models = transformModelsFromResponse(data.values)
            models.forEach { v -> v.typeId = News.Type.News.index }
            saveModelsInRealm(models)
        }
    }

    private fun saveModelsInRealm(models: ArrayList<News>) {
        Log.d("APP_REPOSITORY", "saveModelsInRealm(), models count:${models.size} [${this}]")
        realm.executeTransactionAsync { realmTransaction ->
            realmTransaction.insertOrUpdate(models)
        }
    }

    private fun transformModelsFromResponse(response: Collection<EventDataResponseModel.PaginatorItem>, withChair: Boolean = false): ArrayList<News> {
        return ArrayList(response.map { v -> News.mapFromPaginatorItem(v, withChair) })
    }

    private fun transformModelFromResponse(response: EventDataResponseModel.PaginatorItem, withChair: Boolean = false): News {
        return News.mapFromPaginatorItem(response, withChair)
    }


    companion object {
        val shared: NewsRepository = NewsRepository()

        private const val PREF_KEY = "news.pref_name"
        private const val IS_FIRST_LOUNCH_KEY = "news.first_config"
    }
}
