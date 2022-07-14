package dev.phystech.mipt.repositories

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.ReferenceModel
import dev.phystech.mipt.models.api.ContactsDataResponseModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.models.api.HistoryDataResponseModel
import dev.phystech.mipt.models.api.HistoryPaginatorItem
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.realm.Realm

class HistoryRepository private constructor() {

    private val realm = Realm.getDefaultInstance()
    private val pref = Application.context.getSharedPreferences(GuestFilesRepository.PREF_NAME, Context.MODE_PRIVATE)

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val histories: BehaviorSubject<ArrayList<LegendModel>> = BehaviorSubject.create()


    init {
        if (pref.getBoolean(IS_FIRST_LOUNCH_KEY, true)) {
            executeFirstConfig()
        }

        val items = realm.where(LegendModel::class.java).findAll()
        histories.onNext(ArrayList(items))
        Log.d("APP_REPOSITORY", "Load from Realm ${items.size} items; [${this}]")

    }


    //  PUBLIC
    fun loadData(callback: ((Boolean) -> Unit)? = null) {
        Log.d("APP_REPOSITORY", "loadData; [${this}]")
        isLoading.onNext(true)
        ApiClient.shared.getHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSuccess, this::handleError)
    }

    fun getById(id: String, callback: ((LegendModel?) -> Unit)?) {
        Log.d("APP_REPOSITORY", "getById($id: String); [${this}]")

        ApiClient.shared.getHistoryById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.data?.paginator?.values?.firstOrNull()?.let { apiModel ->
                    val model = transformModelFromResponse(apiModel)
                    callback?.invoke(model)
                    return@subscribe
                }

                callback?.invoke(null)
            }, {
                callback?.invoke(null)
            })
    }



    //  LOAD DATA HANDLERS
    private fun handleSuccess(it: HistoryDataResponseModel) {
        Log.d("APP_REPOSITORY", "handleSuccess(it); [${this}]")
        isLoading.onNext(false)
        if (it.status.isSuccess) {
            val models = transformModelsFromResponse(it)
            saveModelsInRealm(models)
            histories.onNext(models)
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
        val contactsFromAssets = AssetsRepository.shared?.getLegends() ?: return
        val contacts = transformModelsFromResponse(contactsFromAssets)
        saveModelsInRealm(contacts)
    }

    private fun saveModelsInRealm(models: ArrayList<LegendModel>) {
        Log.d("APP_REPOSITORY", "saveModelsInRealm(), models count:${models.size} [${this}]")
        realm.executeTransactionAsync { realmTransaction ->
            realmTransaction.insertOrUpdate(models)
        }
    }

    private fun transformModelsFromResponse(response: HistoryDataResponseModel): ArrayList<LegendModel> {
        return ArrayList(response.data?.paginator?.values?.map { v -> LegendModel.fromPaginatorItem(v) } ?: ArrayList())
    }

    private fun transformModelFromResponse(response: HistoryPaginatorItem): LegendModel {
        return LegendModel.fromPaginatorItem(response)
    }


    companion object {
        val shared: HistoryRepository = HistoryRepository()

        private const val ITEMS_KEY = "legends.items"
        private const val IS_FIRST_LOUNCH_KEY = "legends.first_config"
    }
}
