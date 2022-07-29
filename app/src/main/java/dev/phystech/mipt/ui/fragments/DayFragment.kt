package dev.phystech.mipt.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.*
import androidx.preference.PreferenceManager
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ClassesAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerListShortItem
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.ui.fragments.study.ScheduleItemDetailFragment
import dev.phystech.mipt.ui.fragments.study.StudyFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_event_detail.SchedulerEventDetailFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_event_edit.SchedulerEventEditFragment
import dev.phystech.mipt.ui.utils.ContextMenuRegistrator
import dev.phystech.mipt.ui.utils.Notifyable
import dev.phystech.mipt.utils.isSuccess
import dev.phystech.mipt.utils.visibility
import dev.phystech.mipt.view.LinearLayoutTyped
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/** Экран со списком занятий. Используется во [ViewPager].
 * Параметром получает временной диапазон (начало - Long, и конец - Long) чтобы отображать
 * занятия только из этого диапазона
 * И evenOddCurrDate - отвечает за отображение только четных недель, только нечетных или всех
 */
class DayFragment : BaseFragment(), ClassesAdapter.Delegate, ContextMenuRegistrator, Notifyable {

    var delegate: ClassesAdapter.Delegate? = null
    lateinit var recycler: RecyclerView

    private val timeInterval = 30 * 1000L
    private var empty = false
    private var day: Int = 0
    private var adapter: ClassesAdapter? = null
    private var timetable: ArrayList<SchedulerListShortItem> = arrayListOf()
    private var evenOddCurrDate: Int = 0
    private var startWeekDay: Long = 0
    private var endWeekDay: Long = Long.MAX_VALUE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val lessons = timetable
        if (lessons.none { it.day == day || it.day + 7 == day}) {
            empty = true
            return inflater.inflate(R.layout.empty_day, container, false)
        }

        val view = inflater.inflate(R.layout.fragment_day, container, false)
        recycler = view.findViewById(R.id.recycler)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val day = arguments?.getInt(ARG_DAY)
        if (day == null || empty) {
            if ((day ?: 7) < 7) {
                view.findViewById<View>(R.id.image).visibility = false.visibility()
                view.findViewById<TextView>(R.id.tvDescription)
                    .setText(R.string.empty_day_description_friends)
            } else {

            }

            return
        }

        // TODO: 1. Убрать сортировку, перенести в модель/контроллер
        // TODO: 2. Сделать нормальную сортировку (без trim, split и т.д.)
        val tt = timetable as List<SchedulerListShortItem>
        tt.sortedWith(kotlin.Comparator { v1, v2 ->
            if (v1.allDay) return@Comparator -1
            else if (v2.allDay) return@Comparator 1

            val sDate1 = (if (v1 is EventModel)
                    v1.startTime.trim().split(" ").lastOrNull()
                else v1.startTime) ?: ""
            val sDate2 = (if (v2 is EventModel)
                    v2.startTime.trim().split(" ").lastOrNull()
                else
                    v2.startTime) ?: ""

            val eDate1 = (if (v1 is EventModel)
                    v1.endTime.trim().split(" ").lastOrNull()
                else v1.endTime) ?: ""
            val eDate2 = (if (v2 is EventModel)
                    v2.endTime.trim().split(" ").lastOrNull()
                else v2.endTime) ?: ""

            if (sDate1.compareTo(sDate2) == 0) {
                return@Comparator eDate1.compareTo(eDate2)
            } else {
                return@Comparator sDate1.compareTo(sDate2)
            }
        }).also {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val showSpaces = preferences.getBoolean(getString(R.string.pref_show_spaces), true)
            recycler.layoutManager = LinearLayoutManager(context)
            adapter = ClassesAdapter(day, showSpaces, it, this)
            adapter?.delegate = this
            recycler.adapter = adapter
            recycler.adapter?.notifyDataSetChanged()
        }



        if (day == TimeUtils.getCurrentDay()) {
            recycler.adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("LIFECIRCLE", "day: onCreate")
        day = arguments?.getInt(ARG_DAY) ?: 0
        evenOddCurrDate = arguments?.getInt(EVEN_ODD) ?: evenOddCurrDate
        startWeekDay = arguments?.getLong(START_WEEK_DAY) ?: startWeekDay
        endWeekDay = arguments?.getLong(END_WEEK_DAY) ?: endWeekDay

        timetable.addAll(SchedulersRepository.shared.getScheduler() ?: emptyList())
        timetable = ArrayList(timetable.filter { v ->
            (v is ScheduleItem && (v.evenodd <= 0 || v.evenodd == evenOddCurrDate)) || (v is EventModel)
        })

        val eventsEnabledKey = resources.getString(R.string.pref_show_events)
        val eventEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(eventsEnabledKey, true)

        if (eventEnabled) {
            ScheduleEventRepository.shared.getUserEventsSplitDate().let {
                it.filter { v ->
                    v.key != null && v.key!!.time >= startWeekDay && v.key!!.time <= endWeekDay
                }.values.flatMap { v -> v }.let {
                    timetable.addAll(it)
                    adapter?.notifyDataSetChanged()
                }
            }
        }

    }

    override fun onResume() {
        Log.i("LIFECIRCLE", "day: onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.i("LIFECIRCLE", "day: onDestroy")
        super.onDestroy()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val view = v as? LinearLayoutTyped ?: return


        val id = v.tag as? Int ?: return
        if (id <= 0) return

        (activity?.getSystemService(Activity.VIBRATOR_SERVICE) as? Vibrator)?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));

            } else {
                //deprecated in API 26
//                v.vibrate(100);
            }
        }

        var extras: Bundle
        var editIntent: Intent? = null
        var deleteIntent: Intent? = null

        when (view.model) {
            is EventModel -> {
                extras = Bundle().apply { putInt(KEY_SCHEDULER_ID, id) }
                editIntent = Intent().apply { action = ACTION_EVENT_EDIT; putExtras(extras) }
                deleteIntent = Intent().apply { action = ACTION_EVENT_DELETE; putExtras(extras) }
            }
            is ScheduleItem -> {
                extras = Bundle().apply { putInt(KEY_SCHEDULER_ID, id) }
                editIntent = Intent().apply { action = ACTION_SCHEDULER_EDIT; putExtras(extras) }
                deleteIntent =
                    Intent().apply { action = ACTION_SCHEDULER_DELETE; putExtras(extras) }
            }
        }


        val deleteTitle = resources.getString(R.string.context_delete)
        menu.add(R.string.context_edit)
            .setIntent(editIntent)
        menu.add(Html.fromHtml("<span style='color:#FF785C';>$deleteTitle</span>"))
            .setIntent(deleteIntent)

        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuIntent = item.intent
        if (menuIntent.extras?.containsKey(KEY_SCHEDULER_ID) == true) {
            val id = menuIntent.getIntExtra(KEY_SCHEDULER_ID, -1)
            var scheduler: SchedulerListShortItem? = null

            fun executeCommand() {
                when (menuIntent.action) {
                    ACTION_SCHEDULER_DELETE -> showDeleteAlert(scheduler as ScheduleItem)
                    ACTION_SCHEDULER_EDIT -> showEditAlert(scheduler as ScheduleItem)
                    ACTION_EVENT_DELETE -> showDeleteAlert(scheduler as EventModel)
                    ACTION_EVENT_EDIT -> showEditAlert(scheduler as EventModel)
                }
            }

            if (menuIntent.action?.contains("scheduler") == true) {
                scheduler = SchedulersRepository.shared.getSchedulerItemById(id)
                executeCommand()
            } else {
                ScheduleEventRepository.shared.getById(id) {
                    scheduler = it
                    executeCommand()
                }
            }
        }

        return true
    }


    //  OTHERS
    private fun showEditAlert(scheduleItem: ScheduleItem) {
        val alertView = layoutInflater.inflate(R.layout.alert_edit_confirm, null)
        val btnEditForSelf = alertView.findViewById<View>(R.id.btnEditForSelf)
        val btnEditForGroup = alertView.findViewById<View>(R.id.btnEditForGroup)
        val btnChage = alertView.findViewById<View>(R.id.btnChage)
        val btnDelete = alertView.findViewById<View>(R.id.btnDelete)

        val alert = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(alertView)
            .create()

        btnDelete.setOnClickListener { alert.cancel() }
        btnChage.setOnClickListener { alert.cancel() }
        btnEditForSelf.setOnClickListener {
            val fragment = SchedulerEditFragment.newInstance(
                scheduleItem.id,
                false,
                SchedulerEditFragment.Type.Edit
            )
            (activity as? MainActivity)?.let { activity ->
                activity.pushFragment(fragment, true)
            }
            alert.cancel()
        }
        if (scheduleItem.canEdit || true) {
            btnEditForGroup.setOnClickListener {
                val fragment = SchedulerEditFragment.newInstance(
                    scheduleItem.id,
                    true,
                    SchedulerEditFragment.Type.Edit
                )
                (activity as? MainActivity)?.let { activity ->
                    activity.pushFragment(fragment, true)
                }
                alert.cancel()
            }
        } else {
            btnEditForGroup.alpha = 0.5f
        }

        alert.show()
    }

    private fun showDeleteAlert(scheduleItem: ScheduleItem) {
        val id = scheduleItem.id

        val deleteTitle = resources.getString(R.string.context_delete)
        val deleteTitleRed = Html.fromHtml("<span style='color:#FF785C';>$deleteTitle</span>")

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_message)
            .setPositiveButton(deleteTitleRed) { _, _ ->
                showProgress()
                ApiClient.shared.schedulerUnsubscribe(id.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        hideProgress()
                        if (it.status.isSuccess) {
                            SchedulersRepository.shared.loadData()
                        } else showMessage(R.string.message_some_error_try_late)


                    }, {
                        hideProgress()
                        showMessage(R.string.message_some_error_try_late)
                    })
            }.setNeutralButton(R.string.cancel, null)
            .create()
            .show()

    }

    private fun showEditAlert(event: EventModel) {
        Log.i("ContextItem", "select event edit [eventId: ${event.id}}]")

        val fragment = SchedulerEventEditFragment.newInstance(event.id, false)
        (activity as? MainActivity)?.pushFragment(fragment, true)
    }

    private fun showDeleteAlert(event: EventModel) {
        Log.i("ContextItem", "select event delete [eventId: ${event.id}}]")
        val id = event.id

        val deleteTitle = resources.getString(R.string.context_delete)
        val deleteTitleRed = Html.fromHtml("<span style='color:#FF785C';>$deleteTitle</span>")

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_message)
            .setPositiveButton(deleteTitleRed) { _, _ ->
                showProgress()
                ScheduleEventRepository.shared.delete(id) {
                    hideProgress()
                    if (it) {
                        showMessage(R.string.success)
                    } else {
                        showMessage(R.string.message_some_error_try_late)
                    }
                }

            }.setNeutralButton(R.string.cancel, null)
            .create()
            .show()
    }


    override fun bindView(view: View?) {

    }

    private fun shouldShowTips(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val key = resources.getString(R.string.pref_tip_key)
        return !preferences.getBoolean(key, false)
    }

    companion object {
        private const val ARG_DAY = "day"
        private const val EVEN_ODD = "even_odd"
        private const val START_WEEK_DAY = "startWeekDay"
        private const val END_WEEK_DAY = "endWeekDay"
        const val ACTION_SCHEDULER_DELETE = "action.scheduler.delete"
        const val ACTION_SCHEDULER_EDIT = "action.scheduler.edit"
        const val ACTION_EVENT_DELETE = "action.event.delete"
        const val ACTION_EVENT_EDIT = "action.event.edit"
        const val KEY_SCHEDULER_ID = "scheduler.id"

        fun new(day: Int,
                timetable: ArrayList<SchedulerListShortItem>,
                evenOddCurrDate: Int,
                startWeekDay: Long,
                endWeekDay: Long
        ): DayFragment {
            return DayFragment().apply {
                arguments = bundleOf(
                    ARG_DAY to day,
                    EVEN_ODD to evenOddCurrDate,
                    START_WEEK_DAY to startWeekDay,
                    END_WEEK_DAY to endWeekDay
                )
            }
        }
    }


    //  ADAPTER DELEGATE
    override fun onClick(item: ScheduleItem) {
        delegate?.onClick(item)
    }

    override fun onClick(item: EventModel) {
        delegate?.onClick(item)
    }

    override fun notifySomeUpdated() {
        adapter?.notifyDataSetChanged()
    }
}