package dev.phystech.mipt.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.api.BaseApiEntity
import dev.phystech.mipt.models.api.ChairTopicDataResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ChairTopicRepository private constructor() {

    private val pref: SharedPreferences
    val chairs: BehaviorSubject<ArrayList<BaseApiEntity.Chair>> = BehaviorSubject.create()

    init {
        pref = Application.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.getString(KEY_CHAIRS, null)?.let { jsonValue ->
            val data = Gson().fromJson(jsonValue, ChairTopicDataResponseModel.Data::class.java)
            data.paginator.values.let { items ->
                val cChairs = items.map { v -> BaseApiEntity.Chair().apply { id = v.id; name = v.name; } }
                chairs.onNext(ArrayList(cChairs))
            }
        }
    }

    fun loadData() {
        ApiClient.shared.getChairTopic()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess) {
                    it.data?.paginator?.values?.let { items ->
                        val cChairs = items.map { v -> BaseApiEntity.Chair().apply { id = v.id; name = v.name; } }
                        chairs.onNext(ArrayList(cChairs))

                        val jsonValue = Gson().toJson(it.data)
                        pref.edit().putString(KEY_CHAIRS, jsonValue).apply()
                    }
                }
            }, {

            })
    }

    fun setTopicEnabled(id: String, isEnabled: Boolean) {
        pref.edit().putBoolean("topic_$id", isEnabled).apply()
    }

    fun getTopciEnabled(id: String): Boolean {
        return pref.getBoolean("topic_$id", true)
    }



    companion object {
        val shared: ChairTopicRepository = ChairTopicRepository()

        const val PREF_NAME = "chair.topics"
        const val KEY_CHAIRS = "chair.list.key"
    }
}