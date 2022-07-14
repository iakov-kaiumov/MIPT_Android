package dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_help

import android.content.res.Resources
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.repositories.PsychologistRepository
import dev.phystech.mipt.models.api.UsersResponseModel

class PsychologistsHelpPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): PsychologistsHelpContract.Presenter() {

    private val allPsychologists: ArrayList<UsersResponseModel.UserInfoModel> = arrayListOf()

    override fun loadPsychologists() {
        view?.showLoader(true)
        PsychologistRepository.shared.loadPsychologists() { psychologists ->
            view?.showLoader(false)
            allPsychologists.clear()
            allPsychologists.addAll(psychologists)
        }
    }

    override fun getPsychologists(): ArrayList<UsersResponseModel.UserInfoModel> {
        return allPsychologists;
    }

    override fun attach(view: PsychologistsHelpContract.View?) {
        super.attach(view)
    }

}