package dev.phystech.mipt.repositories

import android.content.Context
import dev.phystech.mipt.Application
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.base.utils.nullIfEmpty
import dev.phystech.mipt.models.SchedulePlace
import dev.phystech.mipt.models.SchedulePlaceCreateModel
import dev.phystech.mipt.models.api.SchedulerAddResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class SchedulePlaceRepository private constructor(){

    val preferences = Application.context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)
    val places: BehaviorSubject<List<SchedulePlace>> = BehaviorSubject.create()


    fun loadData() {
        ApiClient.shared.getSchedulePlace()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.values?.let {
                        places.onNext(it.toList())
                    }
                }
            }, {
                print(it)
            })
    }

    fun getById(id: String, callback: ((SchedulePlace?) -> Unit)? = null) {
        if (places.hasValue()) {
            val model = places.value.firstOrNull{ v -> v.id == id}
            if (model != null) {
                callback?.invoke(model)
                return
            }
        }

        ApiClient.shared.getSchedulePlace(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(it.data?.paginator?.get(id))
            }, {
                callback?.invoke(null)
            })
    }

    fun createAuditory(model: SchedulePlaceCreateModel, callback: ((Boolean, SchedulePlace?) -> Unit)?) {
        String.empty()
        ApiClient.shared.createSchedulerPlace(
            model.name,
            model.type?.nullIfEmpty(),
            model.building?.nullIfEmpty(),
            model.floor?.nullIfEmpty()
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    loadData()
                }

                callback?.invoke(it.status.isSuccess, it.data?.schedulerPlace)
            }, {
                callback?.invoke(false, null)
            })
    }


    companion object {
        const val NAME_SHARED_PREFERENCES = "pref.schedule_places"

        val shared: SchedulePlaceRepository = SchedulePlaceRepository()
    }
}