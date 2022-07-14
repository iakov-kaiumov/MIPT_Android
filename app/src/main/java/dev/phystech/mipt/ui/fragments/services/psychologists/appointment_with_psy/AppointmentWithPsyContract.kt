package dev.phystech.mipt.ui.fragments.services.psychologists.appointment_with_psy

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.api.PsyTimeResponseModel
import dev.phystech.mipt.models.api.UsersResponseModel

interface AppointmentWithPsyContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
        fun setCalendarEventPoints(psychologists: ArrayList<PsyTimeResponseModel.PsyInfoModel>)
        fun <VH : RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun loadPsychologistsTime(allPsychologists: ArrayList<UsersResponseModel.UserInfoModel>, psyId: String?, location: String?, type: String)
        abstract fun getPsychologistsTimes(): ArrayList<PsyTimeResponseModel.PsyInfoModel>
    }
}