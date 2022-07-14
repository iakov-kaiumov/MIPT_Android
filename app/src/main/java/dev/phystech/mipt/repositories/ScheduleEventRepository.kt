package dev.phystech.mipt.repositories

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.kotlin.executeTransactionAwait
import kotlinx.coroutines.Dispatchers
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ScheduleEventRepository private constructor() {

    val userEvents: BehaviorSubject<MutableList<EventModel>> = BehaviorSubject.create()
    val groupedUserEvents: Observable<SortedMap<Date?, List<EventModel>>> by lazy { getUserEventsSplitDateObservable() }
    val loading: BehaviorSubject<Boolean> = BehaviorSubject.create()

    fun loadData() {
        val realm = Realm.getDefaultInstance()
        loading.onNext(true)
        val models = realm.where(EventModel::class.java).findAll()
        val cleanModels = realm.copyFromRealm(models)
        userEvents.onNext(ArrayList(cleanModels))

        ApiClient.shared.getMyEvents()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loading.onNext(false)
                it.data?.timetable?.flatMap { v -> v.value }?.let {
                    userEvents.onNext(it.toMutableList())
                    saveInDB(it)
                }
            }, {
                loading.onNext(false)
                print(it)
            })
    }

    private fun getUserEventsSplitDateObservable(): Observable<SortedMap<Date?, List<EventModel>>> {
        val map = userEvents
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                it.filter {
                  it.isValid
                }.groupBy { v ->

                    var date = dateFormat.parse(v.startTime ?: "")

                    date?.let { it ->
                        date = Date(it.time - it.time % (1000 * 60 * 60 * 24))
                    }

                    return@groupBy date
                }.toSortedMap()
            }.doOnSubscribe {
                userEvents.onNext(userEvents.value.toMutableList())
            }

        return map
    }

    fun getUserEventsSplitDate(): SortedMap<Date, List<EventModel>> {
        if (userEvents.hasValue()) {
            return userEvents.value.groupBy { v ->
                var date = dateFormat.parse(v.startTime ?: "")

                date = date?.let { it ->
                    Date(it.time - it.time % (1000 * 60 * 60 * 24))
                }


                return@groupBy date
            }.toSortedMap()
        }

        return sortedMapOf()
    }

    fun getById(id: Int, callback: ((EventModel?) -> Unit)? = null) {
        if (userEvents.hasValue()) {
            userEvents.value.firstOrNull { v -> v.id == id }?.let {
                callback?.invoke(it)
                return
            }
        }

        ApiClient.shared.getSchedulerEvent(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.firstOrNull()?.let {
                        callback?.invoke(it)
                        saveInDB(it)
                    }
                } else {
                    callback?.invoke(null)
                }
            }, {
                callback?.invoke(null)
            })


    }

    fun updateModel(model: EventModel, forGroup: Boolean, callback: ((Boolean) -> Unit)?) {
        model.teachers = RealmList(*model.teachers.map { v -> v.copy() }.toTypedArray())
        val modelString = Gson().toJson(model)
        val groupValue = if (forGroup) 1 else null

        ApiClient.shared.updateSchedulerEvent(modelString, groupValue)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.data?.scheduleEvent?.let { eventModel ->
                    userEvents.value.firstOrNull { v -> v.id == eventModel.id }?.let {
                        userEvents.value.remove(it)
                        userEvents.value.add(eventModel)
                        userEvents.onNext(userEvents.value)
                        saveInDB(eventModel)
                    }
                }

//                loadData()
                callback?.invoke(it.status.isSuccess)
            }, {
                loadData()
                callback?.invoke(false)
            })
    }

    fun createModel(model: EventModel, forGroup: Boolean, callback: ((Boolean) -> Unit)?) {
        model.teachers = RealmList(*model.teachers.map { v -> v.copy() }.toTypedArray())
        val modelString = Gson().toJson(model)

        val groupValue = if (forGroup) 1 else null
        ApiClient.shared.createSchedulerEvent(modelString, groupValue)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.data?.scheduleEvent?.let { eventModel ->
                    userEvents.value.add(eventModel)
                    saveInDB(eventModel)
                    userEvents.onNext(userEvents.value)
                }

//                loadData()
                callback?.invoke(it.status.isSuccess)
            }, {
                loadData()
                callback?.invoke(false)
            })
    }

    fun resetChanges(modelId: Int, callback: ((Boolean) -> Unit)?) {
        ApiClient.shared.resetEventChanges(modelId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.scheduleEvent?.let { reseterModel ->
                        userEvents.value.firstOrNull { v -> v.id == reseterModel.id }?.let {
                            userEvents.value.remove(it)
                            userEvents.value.add(reseterModel)
                            saveInDB(reseterModel)
                        }
                    }
                    loadData()
                }

                callback?.invoke(it.status.isSuccess)

            }, {
                callback?.invoke(false)
            })
    }

    fun getForCourse(courseId: String, callback: ((List<EventModel>) -> Unit)? = null) {
        if (userEvents.hasValue()) {
            val result = userEvents.value.filter { v -> v.course == courseId && courseId.isNullOrEmpty().not() }
            callback?.invoke(result)
        }
    }

    fun delete(eventId: Int, callback: ((Boolean) -> Unit)? = null) {
        ApiClient.shared.eventUnsubscribe(eventId.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) {
                    if (userEvents.hasValue()) {
                        userEvents.value.removeIf { v -> v.id == eventId }
                        userEvents.onNext(userEvents.value.toMutableList())
                        deleteFromDB(eventId)
                        loadData()
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    callback?.invoke(it.status.isSuccess)
                }

            }, {
                Handler(Looper.getMainLooper()).post {
                    callback?.invoke(false)
                }
            })
    }

    fun subscribe(eventId: String, secret: String?, callback: ((Boolean) -> Unit)? = null) {
        ApiClient.shared.eventSubscribe(eventId, secret)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loadData()
                callback?.invoke(it.status.isSuccess)
            }, {
                callback?.invoke(false)
            })
    }

    fun containsInMyEvents(eventId: String): Boolean {
        eventId.toIntOrNull()?.let { id ->
            if (userEvents.hasValue()) {
                val model = userEvents.value.firstOrNull { v -> v.id == id }
                return model != null
            }
        }

        return false
    }

    private fun saveInDB(items: List<EventModel>) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransactionAsync {
            it.insertOrUpdate(items)
        }
        realm.close()
    }

    private fun saveInDB(item: EventModel) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransactionAsync {
            it.insertOrUpdate(item)
        }
        realm.close()
    }

    private fun deleteFromDB(itemId: Int) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransactionAsync {
            val itemToRemove = it.where(EventModel::class.java)
                .equalTo("id", itemId)
                .findFirst()

            itemToRemove?.deleteFromRealm()
        }

        realm.close()
    }

    companion object {
        private val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val shared: ScheduleEventRepository = ScheduleEventRepository()
    }
}