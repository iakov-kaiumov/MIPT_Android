package dev.phystech.mipt.repositories

import android.content.Context
import com.google.gson.Gson
import dev.phystech.mipt.Application
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import dev.phystech.mipt.models.ReferenceModel
import dev.phystech.mipt.models.api.ContactsDataResponseModel
import dev.phystech.mipt.models.api.ContactsPaginatorItem
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.realm.Realm
import io.realm.RealmResults

class ContactsRepository {

    private val pref = Application.context.getSharedPreferences(GuestFilesRepository.PREF_NAME, Context.MODE_PRIVATE)
    private val realm = Realm.getDefaultInstance()

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val contacts: BehaviorSubject<ArrayList<ReferenceModel>> = BehaviorSubject.create()


    init {
        //  First lounch - get models from assets and save in realm
        if (pref.getBoolean(IS_FIRST_LOUNCH_KEY, true)) {
            executeFirstConfig()
        }

        //  Load models
        val items = realm.where(ReferenceModel::class.java).findAll()
        items.addChangeListener { models, c ->
            contacts.onNext(ArrayList(models))
        }

        contacts.onNext(ArrayList(items))
    }


    //  PUBLIC
    fun loadData(callback: ((Boolean) -> Unit)? = null) {
        isLoading.onNext(true)
        ApiClient.shared.getContacts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSuccess, this::handleError)

    }

    fun loadDeletedData(callback: ((Boolean) -> Unit)? = null) {
        ApiClient.shared.getDeletedContacts()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({

                if (it.status.isSuccess) {

                    val realm = Realm.getDefaultInstance()

                    val modelsDeleted = transformModelsFromResponse(it)
                    realm.beginTransaction()
                    val modelsInDB = realm.where(ReferenceModel::class.java).findAll();
                    if(!modelsInDB.isEmpty() && !modelsDeleted.isNullOrEmpty()) {

                        val listNeedDelete = modelsInDB.filter { m -> modelsDeleted.any { it.id == m.id } }
                        if (!listNeedDelete.isNullOrEmpty()) {

                            for (del in listNeedDelete.size - 1 downTo 0) {
                                contacts.value?.let { contacts ->
                                    if (contacts.isNotEmpty()) {
                                        val eletemt = contacts.find { it.id == listNeedDelete.get(del).id }
//                                        val eletemt = contacts.filter { list -> list.id == listNeedDelete.get(del).id }
                                        if (eletemt != null)
                                            contacts.remove(eletemt)
                                    }
                                }

                                listNeedDelete.get(del)?.deleteFromRealm()
                            }
                        }
                    }
                    realm.commitTransaction()
                }
            }, {
                print(it.toString())
            })

    }

    //  LOAD DATA HANDLERS
    private fun handleSuccess(it: ContactsDataResponseModel) {
        isLoading.onNext(false)
        if (it.status.isSuccess) {
            val models = transformModelsFromResponse(it)
            if (models.isNotEmpty()) {
                val items = realm.where(ReferenceModel::class.java).findAll()
                if (ArrayList(items).isNotEmpty()) {
                    realm.beginTransaction()
                    items.deleteAllFromRealm()
                    realm.commitTransaction()
                }
                saveModelsInRealm(models)
                contacts.onNext(models)
            }
        }
    }

    private fun handleError(error: Throwable) {
        isLoading.onNext(false)
    }


    //  OTHERS
    private fun executeFirstConfig() {
        pref.edit().putBoolean(IS_FIRST_LOUNCH_KEY, false).apply()
        val contactsFromAssets = AssetsRepository.shared?.getContacts() ?: return
        val contacts = transformModelsFromResponse(contactsFromAssets)
        saveModelsInRealm(contacts)
    }

    private fun saveModelsInRealm(models: ArrayList<ReferenceModel>) {
        realm.executeTransactionAsync { realmTransaction ->
            realmTransaction.insertOrUpdate(models)
        }
    }

    private fun transformModelsFromResponse(response: ContactsDataResponseModel): ArrayList<ReferenceModel> {
        return ArrayList(response.data?.paginator?.map { v -> ReferenceModel.map(v.value) } ?: ArrayList())
    }




    companion object {
        val shared: ContactsRepository = ContactsRepository()

        private const val ITEMS_KEY = "contacts_items"
        private const val IS_FIRST_LOUNCH_KEY = "contacts_is_first_lounch"
    }
}