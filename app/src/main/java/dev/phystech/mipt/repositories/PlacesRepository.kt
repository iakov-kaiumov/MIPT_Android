package dev.phystech.mipt.repositories

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.models.api.HistoryDataResponseModel
import dev.phystech.mipt.models.api.HistoryPaginatorItem
import dev.phystech.mipt.models.api.PlacePaginatorItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.models.api.PlacesDataResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.realm.Realm
import java.io.File
import java.nio.charset.Charset

class PlacesRepository private constructor() {

    private val pref = Application.context.getSharedPreferences(GuestFilesRepository.PREF_NAME, Context.MODE_PRIVATE)
    private val realm = Realm.getDefaultInstance()

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val places: BehaviorSubject<ArrayList<PlaceModel>> = BehaviorSubject.create()

    init {
        //  Config on first lounch
        try {
            if (pref.getBoolean(IS_FIRST_LOUNCH_KEY, true)) {
                executeFirstConfig()
            }
        } catch (e: Exception) {
            pref.edit().remove(IS_FIRST_LOUNCH_KEY).apply()
            executeFirstConfig()
        }


        val models = realm.where(PlaceModel::class.java).findAll()
        places.onNext(ArrayList(models))
        Log.d("APP_REPOSITORY", "Load from Realm ${models.size} items; [${this}]")

//        if (pref.contains(ITEMS_KEY)) {
//            pref.getString(ITEMS_KEY, null)?.let { jsonString ->
//                val model = Gson().fromJson(jsonString, PlacesDataResponseModel.Data::class.java)
//                places.onNext(model.paginator)
//            }
//        } else {
//            AssetsRepository.shared?.getPlaces()?.data?.paginator?.let { data ->
//                places.onNext(data)
//            }
//        }
    }

    //  PUBLIC
    fun loadData(callback: ((Boolean) -> Unit)? = null) {
        Log.d("APP_REPOSITORY", "loadData; [${this}]")
        isLoading.onNext(true)
        ApiClient.shared.getPlaces()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSuccess, this::handleError)
    }

    fun getById(id: String, callback: ((PlaceModel?) -> Unit)? = null) {
        Log.d("APP_REPOSITORY", "getById($id: String); [${this}]")
        ApiClient.shared.getPlaceById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val model = it.data?.paginator?.values?.firstOrNull()
                if (model != null) {
                    callback?.invoke(transformModelFromResponse(model))
                } else {
                    callback?.invoke(null)
                }
            }, {
                callback?.invoke(null)
            })
    }

    fun getBySchedulerId(id: String, callback: ((PlaceModel?) -> Unit)? = null) {
        ApiClient.shared.getSchedulePlace(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

            }, {

            })
    }


    //  LOAD DATA HANDLERS
    private fun handleSuccess(it: PlacesDataResponseModel) {
        Log.d("APP_REPOSITORY", "handleSuccess(it); [${this}]")
        isLoading.onNext(false)
        if (it.status.isSuccess) {
            val models = transformModelsFromResponse(it)
            saveModelsInRealm(models)
            places.onNext(models)
        }
    }

    private fun handleError(error: Throwable) {
        Log.d("APP_REPOSITORY", "handleError(); [${this}]")
        isLoading.onNext(false)
    }


    //  OTHERS
    private fun executeFirstConfig() {
        Log.d("APP_REPOSITORY", "executeFirstConfig() [${this}]")
        pref.edit().putBoolean(IS_FIRST_LOUNCH_KEY, false).apply()
        val contactsFromAssets = AssetsRepository.shared?.getPlaces() ?: return
        val places = transformModelsFromResponse(contactsFromAssets)
        saveModelsInRealm(places)
    }

    private fun saveModelsInRealm(models: ArrayList<PlaceModel>) {
        Log.d("APP_REPOSITORY", "saveModelsInRealm(), models count:${models.size} [${this}]")
        realm.executeTransactionAsync { realmTransaction ->
            realmTransaction.insertOrUpdate(models)
        }
    }

    private fun transformModelsFromResponse(response: PlacesDataResponseModel): ArrayList<PlaceModel> {
        return ArrayList(response.data?.paginator?.values?.map { v -> PlaceModel.fromPaginatorItem(v) } ?: ArrayList())
    }

    private fun transformModelFromResponse(response: PlacePaginatorItem): PlaceModel {
        return PlaceModel.fromPaginatorItem(response)
    }



    companion object {
        val shared: PlacesRepository = PlacesRepository()

        private const val ITEMS_KEY = "places_array"
        private const val IS_FIRST_LOUNCH_KEY = "app.is_first_lounch"
    }
}
