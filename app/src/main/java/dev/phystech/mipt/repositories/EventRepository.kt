package dev.phystech.mipt.repositories

import com.google.gson.Gson
import dev.phystech.mipt.models.EventModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.models.api.EventDataResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.realm.RealmList

class EventRepository private constructor() {

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val events: BehaviorSubject<HashMap<String, EventDataResponseModel.PaginatorItem>> = BehaviorSubject.create()

    var page = 1

    init {
        AssetsRepository.shared?.getEvents()?.data?.paginator?.let { data ->
            events.onNext(data)
        }
    }

    fun loadData(callback: ((Boolean) -> Unit)? = null) {

        isLoading.onNext(true)
        ApiClient.shared.getEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isLoading.onNext(false)
                    if (it.status.isSuccess) {
                        events.onNext(it.data?.paginator)
                    }
                }, {
                    isLoading.onNext(false)
                })
    }

    fun loadNextPage() {
        ApiClient.shared.getEvents(page + 1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val newItems = it.data?.paginator ?: return@subscribe
                if (newItems.size > 0) {
                    page += 1
                    events.value?.let {
                        it.putAll(newItems)
                        events.onNext(it)
                    }
                }
            }, {})
    }

    companion object {
        val shared: EventRepository = EventRepository()
    }
}
