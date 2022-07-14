package dev.phystech.mipt.ui.fragments.services.psychologists.record_apply

import android.content.res.Resources
import dev.phystech.mipt.R
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.repositories.PsychologistRepository
import dev.phystech.mipt.repositories.ScheduleEventRepository
import io.realm.RealmList

class RecordApplyPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): RecordApplyContract.Presenter() {

    override fun recordApply(id: String, phone: String) {
        view?.showLoader(true)
        PsychologistRepository.shared.applyPsychologist(id, phone) { response ->
            if (response.isEmpty()) {
                view?.creatingEventInSchedule()
            } else {
                view?.showLoader(false)
                view?.showError(response)
            }
        }
    }

    override fun createSchedule(eventModel: EventModel) {
        ScheduleEventRepository.shared.createModel(eventModel, false) {
            view?.showLoader(false)
            view?.showError("")
        }
    }

    override fun attach(view: RecordApplyContract.View?) {
        super.attach(view)
    }
}