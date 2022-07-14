package dev.phystech.mipt.repositories

import android.content.Context
import android.os.FileUtils
import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import edu.phystech.iag.kaiumov.shedule.utils.DataUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.realm.Realm
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TeachersRepository private constructor() {

    val preferences = Application.context.getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE)
    val realm = Realm.getDefaultInstance()
    val teachers: BehaviorSubject<MutableList<Teacher>> = BehaviorSubject.create()

    val dateFormat: DateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }



    init {
        teachers.onNext(
            DataUtils.loadTeachers(Application.context)
                .mapNotNull { v -> v.value }
                .toMutableList()
        )

        preferences.edit().clear().apply()
        val teachers = realm.where(Teacher::class.java).findAll()
        if (teachers.size > 0) {
            this.teachers.onNext(ArrayList(teachers))
        }
    }

    fun loadData() {
        val requestDateTime = Date()
        val lastUpdate = preferences.getString(KEY_LATS_UPDATE, null)
        ApiClient.shared.getTeachers(lastUpdateFrom = lastUpdate)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {

                    //  SAVE DATA IN DB
                    it.data?.paginator?.values?.let { responseTeachers ->
                        teachers.onNext(responseTeachers.toMutableList())
                        realm.executeTransactionAsync ({ realmTransaction ->
                            realmTransaction.insertOrUpdate(responseTeachers)
                        }, { _ ->
                            //  UPDATE LOCAL OBSERVABLE MODELS (TEACHERS)
                            realm.where(Teacher::class.java).findAll().let { realmTeachers ->
                                teachers.onNext(ArrayList(realmTeachers))
                            }
                        })
                    }


                    //  UPDATE "LAST UPDATE"
                    val requestDateTimeFormatted = dateFormat.format(requestDateTime)
                    preferences.edit().putString(KEY_LATS_UPDATE, requestDateTimeFormatted).apply()


                }
            }, {
                Log.w("ERROR", it)
                Log.e("ERROR", it.toString())
                print("")
            })
    }

    fun getByID(id: String, callback: ((Teacher?) -> Unit)? = null) {
        if (teachers.hasValue()) {
            teachers.value.firstOrNull{ v -> v.id == id }?.let {
                callback?.invoke(it)
                return
            }
        }

        ApiClient.shared.getTeacher(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.values?.firstOrNull().let {
                        callback?.invoke(it)
                    }
                } else {
                    callback?.invoke(null)
                }
            }, {
                callback?.invoke(null)
            })


    }

    fun addTeacher(teacher: Teacher) {
        realm.executeTransactionAsync {
            it.insertOrUpdate(teacher)
        }

        if (teachers.hasValue()) {
            teachers.value.add(teacher)
        }
    }



    companion object {
        const val KEY_PREF_NAME = "preferences.teachers"
        const val KEY_LATS_UPDATE = "teachers.last_update"
        val shared: TeachersRepository = TeachersRepository()
    }
}