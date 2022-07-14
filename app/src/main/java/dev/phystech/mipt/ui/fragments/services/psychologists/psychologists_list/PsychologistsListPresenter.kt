package dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_list

import android.content.res.Resources
import dev.phystech.mipt.adapters.PsychologistsListAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.repositories.PsychologistRepository
import dev.phystech.mipt.models.api.UsersResponseModel

class PsychologistsListPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): PsychologistsListContract.Presenter() {

    private val adapter: PsychologistsListAdapter = PsychologistsListAdapter()

    override fun load() {
        view?.showLoader(true)
        PsychologistRepository.shared.loadPsychologists() { psychologists ->
            view?.showLoader(false)
            adapter.items.clear()
            adapter.items.addAll(psychologists)
            adapter.notifyDataSetChanged()
            view?.setAdapter(adapter)
        }
    }

    override fun setPsychologists(psychologists: ArrayList<UsersResponseModel.UserInfoModel>) {
        adapter.items.clear()
        adapter.items.addAll(psychologists)
        adapter.notifyDataSetChanged()
        view?.setAdapter(adapter)
    }

    override fun attach(view: PsychologistsListContract.View?) {
        super.attach(view)
    }

}