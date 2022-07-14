package dev.phystech.mipt.repositories

import android.content.Context
import android.os.Looper
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.MeetItem
import dev.phystech.mipt.models.api.MeetFilesResponseModel
import dev.phystech.mipt.network.ApiClient
import io.reactivex.rxjava3.core.Single

class GuestFilesRepository private constructor() {

    val items: BehaviorSubject<ArrayList<MeetItem>> = BehaviorSubject.create()

    private val pref = Application.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        Thread {
            pref.getString(ITEMS_KEY, null)?.let {
                Gson().fromJson(it, MeetFilesResponseModel.Data::class.java)?.let {
                    val prefItems = ArrayList(it.paginator.values)
                    items.onNext(prefItems)
                }
            }
        }.start()

    }

    fun load() {
        ApiClient.shared.getMeetFiles()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val newItemsData = it.data ?: return@subscribe
                val files: ArrayList<MeetItem> = ArrayList(newItemsData.paginator.values)

                items.onNext(files)

                val jsonValue = Gson().toJson(newItemsData)
                pref.edit().putString(ITEMS_KEY, jsonValue).apply()


            }, {
                if (items.hasValue()) {
                    items.onNext(items.value)
                } else {
                    items.onNext(ArrayList())
                }
                print("Error")
            })
    }

    companion object {
        const val PREF_NAME = "guest_files_pref_name"
        const val ITEMS_KEY = "guest_files_key"
        val shared = GuestFilesRepository()
    }
}