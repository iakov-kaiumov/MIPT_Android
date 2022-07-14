package dev.phystech.mipt.ui.fragments.services.psychologists.appointment_with_psy

import android.content.res.Resources
import android.util.Log
import dev.phystech.mipt.adapters.PsychologistsTimesListAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.models.api.PsyTimeResponseModel
import dev.phystech.mipt.models.api.UsersResponseModel
import dev.phystech.mipt.repositories.PsychologistRepository
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AppointmentWithPsyPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): AppointmentWithPsyContract.Presenter() {

    private val adapter: PsychologistsTimesListAdapter = PsychologistsTimesListAdapter()
    private val psychologistsTimes: ArrayList<PsyTimeResponseModel.PsyInfoModel> = ArrayList()

    override fun loadPsychologistsTime(allPsychologists: ArrayList<UsersResponseModel.UserInfoModel>, psyId: String?, location: String?, type: String) {
        view?.showLoader(true)
        PsychologistRepository.shared.loadPsychologistsTime(psyId, location, type) { psychologistsTimes ->
            if (psychologistsTimes.isNotEmpty() && allPsychologists.isNotEmpty()) {
                val dateFormat = SimpleDateFormat("HH:mm")
                try {
                    psychologistsTimes.sortBy { list -> dateFormat.parse(dateFormat.format(list.startTime?.getFormatDate())).time }
                } catch (e: Exception) {
                    Log.d("DATE_PARSE", "Not parse")
                }
                for(item in psychologistsTimes) {
                    val findPsychologist = allPsychologists.find { list -> list.id == item.psy?.id }
                    if (findPsychologist != null) {
                        item.user_psy = findPsychologist
                    }
                }
            }

            view?.showLoader(false)

            this.psychologistsTimes.clear()
            this.psychologistsTimes.addAll(psychologistsTimes)

            view?.setAdapter(adapter)
            view?.setCalendarEventPoints(psychologistsTimes)
        }
    }

    override fun getPsychologistsTimes(): ArrayList<PsyTimeResponseModel.PsyInfoModel> {
        return psychologistsTimes
    }

    override fun attach(view: AppointmentWithPsyContract.View?) {
        super.attach(view)
    }
}