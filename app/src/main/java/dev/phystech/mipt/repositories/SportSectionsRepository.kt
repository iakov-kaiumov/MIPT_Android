package dev.phystech.mipt.repositories

import dev.phystech.mipt.models.SportSectionModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class SportSectionsRepository private constructor() {

    val sections: BehaviorSubject<ArrayList<SportSectionModel>> = BehaviorSubject.create()

    fun loadData() {
        ApiClient.shared.getSportSections()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.values?.let {
                        sections.onNext(ArrayList(it))
                    }
                }
            }, {

            })
    }


    fun getByID(id: String, callback: ((SportSectionModel?) -> Unit)) {
        if (sections.hasValue()) {
            sections.value.firstOrNull { v -> v.id == id }?.let { model ->
                callback.invoke(model)
            }
        } else {
            callback.invoke(null)
        }

    }


    companion object {
        val shared: SportSectionsRepository = SportSectionsRepository()
    }
}