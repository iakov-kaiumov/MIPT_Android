package dev.phystech.mipt.repositories

import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.models.api.ChairDataResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess

class ChairRepository private constructor() {

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val chairs: BehaviorSubject<HashMap<String, ChairDataResponseModel.PaginatorItem>> = BehaviorSubject.create()

    val allNewsChairs: BehaviorSubject<List<ChairDataResponseModel.PaginatorItem>> = BehaviorSubject.create()
    val allEventChairs: BehaviorSubject<List<ChairDataResponseModel.PaginatorItem>> = BehaviorSubject.create()

    fun loadData(callback: ((Boolean) -> Unit)? = null) {
        isLoading.onNext(true)
        ApiClient.shared.getChair()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    isLoading.onNext(false)
                    if (it.status.isSuccess) {
                        chairs.onNext(it.data?.paginator)
                    }
                }, {
                    isLoading.onNext(false)
                })

        ApiClient.shared.getChairsList()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.values?.let {
                        allNewsChairs.onNext(it.toList())
                    }
                }
            }, {

            })

        ApiClient.shared.getEventChairsList()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.values?.let {
                        allEventChairs.onNext(it.toList())
                    }
                }
            }, {

            })

    }


    companion object {
        val shared: ChairRepository = ChairRepository()
    }
}
