package dev.phystech.mipt.repositories

import dev.phystech.mipt.models.TimeSlotModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class TimeSlotsRepository private constructor() {

    val slots: BehaviorSubject<List<TimeSlotModel>> = BehaviorSubject.create()

    fun load() {
        ApiClient.shared.getTimeSlots()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) { slots.onNext(it.data) }
            }, {})
    }

    companion object {
        val shared: TimeSlotsRepository = TimeSlotsRepository()
    }
}