package dev.phystech.mipt.ui.fragments.study.events

import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.scheduler_event.SchedulerEventTitleModel
import dev.phystech.mipt.adapters.scheduler_event.SchedulerEventsAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.repositories.EventRepository
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.ui.fragments.study.scheduler_event_detail.SchedulerEventDetailFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import kotlin.Comparator

class EventsPresenter(private val bottomSheetController: BottomSheetController): EventsContract.Presenter(),
    SchedulerEventsAdapter.Delegate {

    private val adapter: SchedulerEventsAdapter = SchedulerEventsAdapter()
    private var subscribtions: CompositeDisposable = CompositeDisposable()


    override fun attach(view: EventsContract.View) {
        super.attach(view)
        view.setAdapter(adapter)
        adapter.delegate = this

        subscribtions.add(ScheduleEventRepository.shared.groupedUserEvents
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val calendar = GregorianCalendar().apply { time = Date() }
                calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
                calendar.set(GregorianCalendar.MINUTE, 0)
                val startDay = calendar.timeInMillis

                adapter.setItems(it)
                val scrollToDate = it.keys.sortedBy { v -> v }.firstOrNull { v -> v != null && v.time >= startDay}
                val item = adapter.items.firstOrNull{ v -> v is SchedulerEventTitleModel && v.date == scrollToDate }
                val itemPosition = adapter.items.indexOf(item)
                view.scrollToPosition(itemPosition)
            }, {}))

        subscribtions.add(ScheduleEventRepository.shared.loading
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it) {
                    view.showProgress()
                } else {
                    view.hideProgress()
                }
            }, {})
        )
    }

    override fun detach() {
        super.detach()
        subscribtions.dispose()
        subscribtions = CompositeDisposable()
    }

    override fun sandwitchSelected() {
        bottomSheetController.showEventOptions()
    }


    /** EVENT ADAPTER DLEGATE
     * @see SchedulerEventsAdapter.Delegate
     */
    override fun onSelectEvent(id: Int) {
        val detailFragment = SchedulerEventDetailFragment.newInstance(id)
        view.navigate(detailFragment)
    }

}