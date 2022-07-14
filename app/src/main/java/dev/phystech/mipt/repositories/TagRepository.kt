package dev.phystech.mipt.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.api.TagsDataResponseModel
import dev.phystech.mipt.network.ApiClient

class TagRepository private constructor() {

    private val pref: SharedPreferences = Application.context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getTag(byId: String, callback: ((model: TagsDataResponseModel.PaginatorItem) -> Unit)? = null) {
        Log.d("TAG REGO", "Get tag tag: id=$byId")

        val gson = Gson()

        if (pref.contains(byId)) {
            pref.getString(byId, null)?.let { jsonValue ->
                val model =
                    gson.fromJson(jsonValue, TagsDataResponseModel.PaginatorItem::class.java)
                callback?.invoke(model)
            }
        } else {
            Log.d("TAG REGO", "Load tag from 'getTag' method: id=$byId")
            ApiClient.shared.getTagsById(byId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.data?.paginator?.get(byId)?.let { tagModel ->
                        val jsonValue = gson.toJson(tagModel)
                        pref.edit().putString(byId, jsonValue).apply()
                        callback?.invoke(tagModel)
                    }
                }, {})
        }


    }

    fun containsTag(withId: String): Boolean {
        Log.d("TAG REGO", "Check contains tag: id=$withId")
        return pref.contains(withId)
    }

    fun loadTagInBackground(byId: String) {
        Log.d("TAG REGO", "Load tag: id=$byId")
        val gson = Gson()
        ApiClient.shared.getTagsById(byId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                it.data?.paginator?.get(byId)?.let { tagModel ->
                    val jsonValue = gson.toJson(tagModel)
                    pref.edit().putString(byId, jsonValue).apply()
                }
            }, {})
    }

    fun loadAll() {
        Log.d("TAG REGO", "Load all tags")
        ApiClient.shared.getTagsById()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.data?.paginator?.let {
                    val gson = Gson()
                    val editor = pref.edit()

                    for (item in it){
                        val k = item.key
                        val v = item.value

                        val jsonValue = gson.toJson(v)
                        editor.putString(k, jsonValue).apply()
                    }
                    editor.apply()
                }
            }, {})
    }


    companion object {
        const val prefName = "tags_preferences_name"

        val shared: TagRepository = TagRepository()
    }
}