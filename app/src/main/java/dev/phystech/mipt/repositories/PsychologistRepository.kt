package dev.phystech.mipt.repositories

import dev.phystech.mipt.models.api.*
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class PsychologistRepository private constructor() {

    // SERVER OPERATIONS

    fun loadPsychologists(callback: ((ArrayList<UsersResponseModel.UserInfoModel>) -> Unit)? = null) {
        ApiClient.shared.getPsychologists()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val data: ArrayList<UsersResponseModel.UserInfoModel> = ArrayList()
                it.data?.paginator?.map { v -> v.value }?.let {
                    data.addAll(it)
                }
                callback?.invoke(data)
            }, {
                callback?.invoke(ArrayList<UsersResponseModel.UserInfoModel>())
            })
    }

    fun loadPsychologistsTime(psyId: String?, location: String?, type: String, callback: ((ArrayList<PsyTimeResponseModel.PsyInfoModel>) -> Unit)? = null) {
        ApiClient.shared.getPsychologistsTime(psyId = psyId, location = location, type = type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val data: ArrayList<PsyTimeResponseModel.PsyInfoModel> = ArrayList()
                it.data?.paginator?.map { v -> v.value }?.let {
                    data.addAll(it)
                }
                callback?.invoke(data)
            }, {
                callback?.invoke(ArrayList<PsyTimeResponseModel.PsyInfoModel>())
            })
    }

    fun applyPsychologist(id: String, phone:String, callback: ((String) -> Unit)? = null) {
        ApiClient.shared.applyPsychologist(id, phone)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                var result: String = "Error";
                if (response.status.isSuccess)
                    response.data?.result?.let {
                        if(it) result = "";
                    }
                callback?.invoke(result)
            }, {
                callback?.invoke(it.toString())
            })
    }

    companion object {
        val shared: PsychologistRepository = PsychologistRepository()
    }
}
