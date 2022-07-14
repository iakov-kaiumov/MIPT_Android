package dev.phystech.mipt.repositories

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import dev.phystech.mipt.models.api.ContactsDataResponseModel
import dev.phystech.mipt.models.api.EventDataResponseModel
import dev.phystech.mipt.models.api.HistoryDataResponseModel
import dev.phystech.mipt.models.api.PlacesDataResponseModel
import java.util.*
import kotlin.collections.HashMap

class AssetsRepository private constructor(val context: Context) {

    fun getContacts(): ContactsDataResponseModel {
        val bytes = context.assets.open("contacts.json").readBytes()
        val jsonString = String(bytes)
        return Gson().fromJson(jsonString, ContactsDataResponseModel::class.java)
    }

    fun getLegends(): HistoryDataResponseModel {
        val bytes = context.assets.open("legends.json").readBytes()
        val jsonString = String(bytes)

        val data = Gson().fromJson(jsonString, HistoryDataResponseModel::class.java)
        data.data?.paginator?.filter { v -> v.value.language?.id == getLanguageID().toString() }?.let {
            data.data?.paginator = HashMap(it)
        }

        return data
    }

    fun getPlaces(): PlacesDataResponseModel {
        val bytes = context.assets.open("places.json").readBytes()
        val jsonString = String(bytes)

        val data = Gson().fromJson(jsonString, PlacesDataResponseModel::class.java)
        data.data?.paginator?.filter { v -> v.value.language?.id == getLanguageID().toString() }?.let {
            data.data?.paginator = HashMap(it)
        }

        return data
    }

    fun getNewsMIPT(): EventDataResponseModel {
        val bytes = context.assets.open("news_inner.json").readBytes()
        val jsonString = String(bytes)
        val data = Gson().fromJson(jsonString, EventDataResponseModel::class.java)
        data.data?.paginator?.filter { v -> v.value.language?.id == getLanguageID().toString() }?.let {
            data.data?.paginator = HashMap(it)
        }

        return data
    }

    fun getNewsChair(): EventDataResponseModel {
        val bytes = context.assets.open("news_chair.json").readBytes()
        val jsonString = String(bytes)
        val data = Gson().fromJson(jsonString, EventDataResponseModel::class.java)
        data.data?.paginator?.filter { v -> v.value.language?.id == getLanguageID().toString() }?.let {
            data.data?.paginator = HashMap(it)
        }

        return data
    }

    fun getNewsOuter(): EventDataResponseModel {
        val bytes = context.assets.open("news_outer_news.json").readBytes()
        val jsonString = String(bytes)
        val data = Gson().fromJson(jsonString, EventDataResponseModel::class.java)
        data.data?.paginator?.filter { v -> v.value.language?.id == getLanguageID().toString() }?.let {
            data.data?.paginator = HashMap(it)
        }

        return data
    }

    fun getEvents(): EventDataResponseModel {
        val bytes = context.assets.open("events.json").readBytes()
        val jsonString = String(bytes)
        val data = Gson().fromJson(jsonString, EventDataResponseModel::class.java)
        data.data?.paginator?.filter { v -> v.value.language?.id == getLanguageID().toString() }?.let {
            data.data?.paginator = HashMap(it)
        }

        return data
    }

    private fun getLanguageID(): Int {
        if (Locale.getDefault().language == "ru") return 1
        else return 2
    }

    companion object {
        var shared: AssetsRepository? = null

        fun create(context: Context) {
            if (shared == null) {
                shared = AssetsRepository(context)
            }
        }
    }
}