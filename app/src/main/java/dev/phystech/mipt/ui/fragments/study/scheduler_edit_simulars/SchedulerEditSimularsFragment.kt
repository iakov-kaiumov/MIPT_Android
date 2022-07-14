package dev.phystech.mipt.ui.fragments.study.scheduler_edit_simulars

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.SchedulerFiltredListAdapter
import dev.phystech.mipt.adapters.SchedulersShortAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay

/** Список занятий, которые предлагаются как альтернатива созданному занятию
 *
 * Подробнее: При создании нового занятия, чтобы не создавать на сервере большое количество
 * одинаковых занятий - пользователю предлагается выбрать уже существующие из списка.
 * Этот список приходит с сервера в ответ на запрос создания.
 *
 * Если пользователь выберет какое-то занятие из списка - делается 2 запроса на сервер:
 * 1 - Подписка на выбранное занятие (добавление)
 * 2 - Отписка от того, которое пытались создать (удаление)
 */
class SchedulerEditSimularsFragment: BaseFragment(), SchedulersShortAdapter.Delegate {

    lateinit var rvSimulars: RecyclerView
    lateinit var rlSaveChanger: RelativeLayout
    lateinit var ivEdit: ImageView

    var schedulers: List<ScheduleItem> = listOf()
    var schedulerId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        schedulers = (arguments?.getSerializable(KEY_SIMULAR_SCHEDULERS) as? ArrayList<*>)
            ?.mapNotNull { v -> v as? ScheduleItem } ?: listOf()
        schedulerId = arguments?.getInt(KEY_SCHEDULER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scheduler_edit_simular, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        rvSimulars = view.findViewById(R.id.rvSimulars)
        rlSaveChanger = view.findViewById(R.id.rlSaveChanger)
        ivEdit = view.findViewById(R.id.ivEdit)

        rvSimulars.adapter = SchedulersShortAdapter().apply {
            delegate = this@SchedulerEditSimularsFragment
            items = schedulers
        }
        rlSaveChanger.setOnClickListener(this::saveClicked)
        ivEdit.setOnClickListener(this::saveClicked)

    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }



    companion object {
        const val KEY_SCHEDULER_ID = "scheduler_edit_simulars.scheduler_id"
        const val KEY_SIMULAR_SCHEDULERS = "scheduler_edit_simulars.siular_schedulers"

        fun newInstance(schedulerId: Int, schedulers: ArrayList<ScheduleItem>): SchedulerEditSimularsFragment {
            return SchedulerEditSimularsFragment().apply {
                arguments = bundleOf(
                    KEY_SCHEDULER_ID to schedulerId,
                    KEY_SIMULAR_SCHEDULERS to schedulers
                )
            }
        }
    }

    //  SchedulersShortAdapter Delegate
    override fun onSelect(item: ScheduleItem) {
        if (schedulerId == null) return
        showProgress()
        SchedulersRepository.shared.delete(requireNotNull(schedulerId)) {
            if (it.not()) {
                hideProgress()
                showMessage(R.string.message_some_error_try_late)
                return@delete
            }

            ApiClient.shared.schedulerSubscribe(item.id.toString(), item.urls.findSecret().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    hideProgress()
                    if (it.status.isSuccess) {
                        SchedulersRepository.shared.loadData()
                        navigationPresenter.showStudy()
                    }
                }, {
                    showMessage(R.string.message_some_error_try_late)
                    hideProgress()
                })
        }
    }


    //  EVENTS
    private fun saveClicked(view: View) {
        navigationPresenter.popFragment()
        navigationPresenter.popFragment()
    }
}