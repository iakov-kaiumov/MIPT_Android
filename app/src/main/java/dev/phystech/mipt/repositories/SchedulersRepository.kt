package dev.phystech.mipt.repositories

import android.content.Context
import com.google.gson.Gson
import edu.phystech.iag.kaiumov.shedule.utils.DataUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SportSectionModel
import dev.phystech.mipt.models.api.request.UpdateSchedulerRequestModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.ui.utils.SchedulerMapper
import dev.phystech.mipt.utils.isSuccess
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class SchedulersRepository private constructor() {

    private val prefName = "schedulers_pref_name"
    private val preferences = Application.context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    val messages: BehaviorSubject<String> = BehaviorSubject.create()
    val schedulers: BehaviorSubject<ArrayList<ScheduleItem>> = BehaviorSubject.create()
    val firstOddWeekDate: BehaviorSubject<Date> = BehaviorSubject.create()
    val categories: BehaviorSubject<List<SportSectionModel>> = BehaviorSubject.create()

    val loadingInProgress: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun loadData() {
        if (loadingInProgress.value) return
        loadingInProgress.onNext(true)

        if (preferences.contains(FIRST_ODD_WEEK_KEY)) {
            val dateTime = preferences.getLong(FIRST_ODD_WEEK_KEY, 0)
            val dt = Date(dateTime)
            firstOddWeekDate.onNext(dt)
        }

        DataUtils.loadSchedule(Application.context)
        DataUtils.loadScheduleData(Application.context)?.let { data ->
            data.timetable.values.firstOrNull()?.let { schedulers ->
                this.schedulers.onNext(schedulers)
                loadingInProgress.onNext(false)
            }
        }


        ApiClient.shared.getSchedulers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (!it.status.isSuccess) {
                    messages.onNext("Произошла ошибка загрузки расписания")
                    return@subscribe
                }

                it.data?.let {
                    DataUtils.saveSchedule(Application.context, it)
                }

                val schedulers = it.data?.timetable?.values?.firstOrNull()
                schedulers?.let {
                    this.schedulers.onNext(schedulers)
                }
        }, {
            messages.onNext("Произошла ошибка запроса расписания")
        }, {
            loadingInProgress.onNext(false)
        })

        ApiClient.shared.getFirstOddWeek()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    it.getValue()?.let {
                        val df = SimpleDateFormat("dd.MM.yyyy")
                        val date = df.parse(it) ?: return@let
                        firstOddWeekDate.onNext(date)
                        DataUtils.saveFirstOddWeekDate(Application.context, date.time)
                        preferences.edit().putLong(FIRST_ODD_WEEK_KEY, date.time).apply()
                    }
                }
            }, {
                print("")
            })

        ApiClient.shared.getScheduleCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.mapNotNull { v -> v.value }?.let {
                        categories.onNext(it)
                    }
                }
            }, { print(it) })

    }

    fun getScheduler(): ArrayList<ScheduleItem>? {
        return schedulers.value
    }

    fun getSchedulerItemById(id: Int): ScheduleItem? {
        if (schedulers.hasValue()) {
            schedulers.value.firstOrNull{ v -> v.id == id}?.let { model ->
                return model
            }
        }

        return null
    }

    fun resetChanges(schedulerId: Int, callback: ((Boolean) -> Unit)? = null) {
        ApiClient.shared.resetChanges(schedulerId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.schedule?.let { resetedModel ->
                        schedulers.value.firstOrNull { v -> v.id == resetedModel.id }?.let {
                            schedulers.value.remove(it)
                            schedulers.value.add(resetedModel)
                        }
                    }

                    loadData()
                } else {

                }
                callback?.invoke(it.status.isSuccess)
            }, {
                callback?.invoke(false)
            })
    }

    fun getSchedulerItemById(id: Int, callback: ((ScheduleItem?) -> Unit)? = null) {
        if (schedulers.hasValue()) {
            schedulers.value.firstOrNull{ v -> v.id == id}?.let { model ->
                callback?.invoke(model)
                return
            }
        }

        ApiClient.shared.getScheduleIndex(id = id.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val model = it.data?.paginator?.firstOrNull()

                if (model != null) {
                    val itemModel = SchedulerMapper.mapIndexToItem(model)
                    callback?.invoke(itemModel)
                } else {
                    callback?.invoke(null)
                }

            }, {
                callback?.invoke(null)
                print(it)
            })
    }

    fun getDateOdd(date: Date): Int? {
        if (firstOddWeekDate.hasValue()) {
            val calendar = GregorianCalendar()
            calendar.time = firstOddWeekDate.value
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)

            val date1 = calendar.time

            calendar.time = date
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)

            val date2 = calendar.time

            val between = date2.time - date1.time
            val betweenDays = TimeUnit.MILLISECONDS.toDays(between) / 7 % 2
            return betweenDays.toInt()
        }

        return null
    }

    fun updateSchedule(
        model: ScheduleItem,
        forGroup: Boolean,
        callback: ((Boolean, Int?, String?, List<ScheduleItem>?) -> Unit)?
    ) {
        val bodyString = Gson().toJson(model)
        val groupValue = if (forGroup) 0 else null

        ApiClient.shared.updateScheduler(bodyString, groupValue)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.schedule?.let { updatedModel ->
                        if (schedulers.hasValue()) {
                            schedulers.value?.let { currSchedulers ->
                                val index = currSchedulers.indexOfFirst { v -> v.id == updatedModel.id }
                                if (index >= 0) {
                                    currSchedulers[index] = updatedModel
                                    schedulers.onNext(currSchedulers)
                                } else {
                                    currSchedulers.add(updatedModel)
                                }
                            }
                        }
                    }
                }

                callback?.invoke (
                    it.status.isSuccess,
                    it.data?.schedule?.id,
                    it.data?.message,
                    it.data?.similar
                )


            }, {
                callback?.invoke(false, null, null, null)
                print(it)
            })

    }

    fun newSchedule(model: ScheduleItem, forGroup: Boolean, callback: ((Boolean) -> Unit)?) {
        val bodyString = Gson().toJson(model)

        val groupValue = if (forGroup) 1 else null

        ApiClient.shared.createScheduler(bodyString, groupValue)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.schedule?.let {  newSchedule ->
                        if (schedulers.hasValue()) {
                            schedulers.value.let { currSchedulers ->
                                currSchedulers.add(newSchedule)
                                schedulers.onNext(currSchedulers)
                            }
                        }
                    }
                }
                callback?.invoke(it.status.isSuccess)

            }, {
                print(it)
                callback?.invoke(false)
            })
    }

    fun delete(id: Int, callback: ((Boolean) -> Unit)?) {
        ApiClient.shared.schedulerUnsubscribe(id.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    schedulers.value.firstOrNull { v -> v.id == id }?.let {
                        schedulers.value.remove(it)
                        schedulers.onNext(schedulers.value)
                    }
                }

                callback?.invoke(it.status.isSuccess)
            }, {
                callback?.invoke(false)
            })
    }


    companion object {
        const val FIRST_ODD_WEEK_KEY = "first_odd_week"

        val shared: SchedulersRepository = SchedulersRepository()
    }
}