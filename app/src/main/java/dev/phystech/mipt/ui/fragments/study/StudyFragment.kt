package dev.phystech.mipt.ui.fragments.study


import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.switchmaterial.SwitchMaterial
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ClassesAdapter
import dev.phystech.mipt.adapters.DaysPagerAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.helpers.EventsBottomSheetController
import dev.phystech.mipt.helpers.SchedulersBottomSheetController
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerListShortItem
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.ui.custom.CustomTabLayout
import dev.phystech.mipt.ui.fragments.study.scheduler_event_detail.SchedulerEventDetailFragment
import dev.phystech.mipt.utils.dpToPx
import dev.phystech.mipt.utils.visibility
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow

/** Экран с расписанием разбитый по дням недели, используя [ViewPager].
 *
 * Для отображение BottomSheet меню [SchedulersBottomSheetController] к фрагменту должен
 * быть присоединен [BottomSheetController]
 * Пример: bottomSheetController.showSchedulersOptions()
 *
 * За это отвечает навигация в [MainActivity]
 * @see MainActivity.showSchedulersOptions
 *
 * Для навигации этот фрагмент использует метод [MainActivity.add].
 * Благодаря этому, при открытии фрагмента пверх, текущий не останавливает выполнение и
 * приложение не вылетает при возвращении к данному фрагменту. {@see readme.md}
 */
class StudyFragment : BaseFragment(), ClassesAdapter.Delegate {

    lateinit var pager: ViewPager
    lateinit var tabs: CustomTabLayout
    lateinit var indicator: View
    lateinit var rlNoSchedulers: View
    lateinit var calendarView: CalendarView
    lateinit var ivFireIcon: ImageView
    lateinit var ivDateDateArrow: ImageView
    lateinit var ivSandwitch: ImageView
    lateinit var tvDateRange: TextView
    lateinit var calendarLayout: LinearLayout
    lateinit var constraintLayout: ConstraintLayout
    lateinit var rlAddSchedule: View
    lateinit var rlToSupport: View

    private var currentWeekStart: Long = 0
    private var currentWeekEnd: Long = 0

    private var filterWeekStart: Long? = null
    private var filterWeekEnd: Long? = null

    private var evenOddCurrDate = 0

    private var subscribtions: CompositeDisposable = CompositeDisposable()
    val schedulersEvents: ArrayList<SchedulerListShortItem> = arrayListOf()

    private var eventDisposable: Disposable? = null

    var adapter: DaysPagerAdapter? = null

    var page = TimeUtils.getCurrentDay() - 1

    var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter("notification")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, filter)

        Log.i("LIFECIRCLE", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("LIFECIRCLE", "onCreateView")
        return inflater.inflate(R.layout.fragment_study, container, false)
    }

    override fun onStart() {
        super.onStart()
        Log.i("LIFECIRCLE", "onStart")
        SchedulersRepository.shared.loadData()
        SchedulersRepository.shared.loadingInProgress
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
//                if (it) showProgress() else hideProgress()
            }, {
                print(it)
            })

        SchedulersRepository.shared.schedulers
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                createTimeTable()
            }, {
                print(it)
            })

        val cal = GregorianCalendar(Locale.getDefault()).apply { time = Date() }
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 + 1
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)

        val startWeekDay: Long = cal.timeInMillis - (dayOfWeek - 1) * (24 * 60 * 60 * 1000)
        val endWeekDay: Long = startWeekDay + 24 * 60 * 60 * 1000 * 7 - (60 * 1000)

        Log.i("Date check", "${cal.time}")

        currentWeekStart = startWeekDay
        currentWeekEnd = endWeekDay

        Log.i("Date check", "Expected monday: ${Date(startWeekDay)}")
        Log.i("Date check", "Expected Sunday: ${Date(endWeekDay)}")


        eventDisposable = ScheduleEventRepository.shared.groupedUserEvents
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.filter { v -> v.key != null && v.key!!.time >= startWeekDay && v.key!!.time < endWeekDay }
                    .values
                    .flatMap { v -> v }
                    .filter { v -> v.isValid }
                    .let {
                        if (schedulersEvents.isEmpty()) {
                            schedulersEvents.clear()
                            schedulersEvents.addAll(it)
                            adapter?.notifyDataSetChanged()
                            createTimeTable()
                        }
                    }
            }, {
                print("Fail")
            })

        subscribtions.add(ScheduleEventRepository.shared.userEvents
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                createTimeTable()
            }, {})
        )

        val even = SchedulersRepository.shared.getDateOdd(Date())
        evenOddCurrDate = (even ?: 0) + 1
        val oddValue = resources.getString(if (even == 0) R.string.odd else R.string.even)
        val curr = resources.getString(R.string.curr_week)
        tvDateRange.text = "${displayDF.format(Date(startWeekDay))} - ${displayDF.format(Date(endWeekDay))} ($curr $oddValue)"
        Log.i("LIFECIRCLE", "onStart")
    }

    override fun onResume() {
        super.onResume()
        configureTabLayout()

        Log.i("LIFECIRCLE", "onResume")

    }

    override fun onPause() {
        super.onPause()
        page = pager.currentItem
        pager.clearOnPageChangeListeners()

        subscribtions.dispose()
        subscribtions = CompositeDisposable()

        Log.i("LIFECIRCLE", "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        Log.i("LIFECIRCLE", "onDestroy")
        isCreated = false
    }


    override fun getBackgroundColor(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.white, null)
        } else {
            resources.getColor(R.color.white)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        pager = view.findViewById(R.id.pager)
        tabs = view.findViewById(R.id.tabs)
        indicator = view.findViewById(R.id.indicator)
        rlNoSchedulers = view.findViewById(R.id.rlNoSchedulers)
        calendarView = view.findViewById(R.id.calendar)
        ivFireIcon = view.findViewById(R.id.ivFireIcon)
        ivDateDateArrow = view.findViewById(R.id.ivDateDateArrow)
        ivSandwitch = view.findViewById(R.id.ivSandwitch)
        tvDateRange = view.findViewById(R.id.tvDateRange)
        calendarLayout = view.findViewById(R.id.calendarLayout)
        constraintLayout = view.findViewById(R.id.constraintLayout)
        rlAddSchedule = view.findViewById(R.id.rlAddSchedule)
        rlToSupport = view.findViewById(R.id.rlToSupport)

        pager.clearOnPageChangeListeners()
        pager.offscreenPageLimit = 6
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                page = position
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })

        tvDateRange.setOnClickListener(this::textViewDateRangeClick)
        ivDateDateArrow.setOnClickListener(this::textViewDateRangeClick)
        ConstraintLayout.LayoutParams.WRAP_CONTENT

        rlNoSchedulers.visibility = false.visibility()
        calendarView.setOnDateChangeListener(this::dateSelected)
        ivSandwitch.setOnClickListener(this::sandwitchSelected)
        ivFireIcon.setOnClickListener(this::toggleClick)
        rlAddSchedule.setOnClickListener(this::addSchedule)
        rlToSupport.setOnClickListener(this::toSupport)

        Log.i("LIFECIRCLE", "onBind")
    }

    var isCreated = false
    private fun createTimeTable() {
        Log.i("LIFECIRCLE", "createTimeTable start")

        val appContext = activity?.applicationContext ?: return
        var schedulers: ArrayList<SchedulerListShortItem> = arrayListOf()

        val cal = GregorianCalendar(Locale.getDefault()).apply { time = Date() }
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 + 1
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)

        val startWeekDay: Long = filterWeekStart ?: (cal.timeInMillis - (dayOfWeek - 2) * (24 * 60 * 60 * 1000)).let { it - it % TimeUnit.DAYS.toMillis(1) }
        val endWeekDay: Long = filterWeekEnd ?: (startWeekDay + TimeUnit.DAYS.toMillis(8) - 1)

        Log.i("Date check", "${cal.time}")
        Log.i("Date check", "Expected monday: ${Date(startWeekDay)}")
        Log.i("Date check", "Expected Sunday: ${Date(endWeekDay)}")

        schedulers.addAll(SchedulersRepository.shared.getScheduler() ?: emptyList())
        schedulers = ArrayList(schedulers.filter { v -> v is ScheduleItem && (v.evenodd <= 0 || v.evenodd == evenOddCurrDate) })

        val eventsEnabledKey = resources.getString(R.string.pref_show_events)
        val eventEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(eventsEnabledKey, true)

        if (eventEnabled) {
            ScheduleEventRepository.shared.getUserEventsSplitDate().let {
                it.filter { v -> v.key != null && v.key!!.time >= startWeekDay && v.key!!.time < endWeekDay }
                    .values.flatMap { v -> v }
                    .let {
                        schedulersEvents.clear()
                        schedulersEvents.addAll(it)
                        adapter?.notifyDataSetChanged()
                    }
            }
        } else {
            schedulersEvents.clear()
        }


        if (schedulers.isNotEmpty()) {
            isCreated = true
            // Creating schedule view
            adapter = DaysPagerAdapter (
                schedulers,
                schedulersEvents,
                appContext,
                childFragmentManager,
                evenOddCurrDate,
                startWeekDay,
                endWeekDay
            ).apply { delegate = this@StudyFragment }


            pager.adapter = adapter

//            adapter?.notifyDataSetChanged()

            tabs.setupWithViewPager(pager)
//                pager.addOnPageChangeListener(CircularViewPagerHandler(pager))
//            rubberTabLayout.setupWithViewPager(pager)
            pager.currentItem = page
            rlNoSchedulers.visibility = false.visibility()




        } else {
            showNoSchedulers()
        }

        Log.i("LIFECIRCLE", "createTimeTable finish")
    }


    //  OTHERS
    private fun configureTabLayout() {
        var width = 0
        tabs.setupWithViewPager(pager, true)
        tabs.post {
            width = tabs.width / 7
            val params = indicator.layoutParams as? FrameLayout.LayoutParams ?: return@post

            params.width = width
            indicator.layoutParams = params
        }

        pager.addOnPageChangeListener(pageChangeListener)
    }

    private fun showNoSchedulers() {
        rlNoSchedulers.visibility = true.visibility()
    }

    private fun updateEvents() {
        eventDisposable?.dispose()

        eventDisposable = ScheduleEventRepository.shared.groupedUserEvents
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.filter { v -> v.key != null && v.key!!.time >= filterWeekStart!! && v.key!!.time < filterWeekStart!! }
                    .values
                    .flatMap { v -> v }
                    .filter { v -> v.isValid }
                    .let {
                        schedulersEvents.clear()
                        schedulersEvents.addAll(it)
                        adapter?.notifyDataSetChanged()
                        createTimeTable()
                    }
            }, {
                print("Fail")
            })
    }

    private fun showBaseDayAlert(item: ScheduleItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.base_day_alert_title)
            .setMessage(R.string.base_day_alert_subtitle)
            .setPositiveButton(R.string.ok) {_, _ ->
            }.show()
    }


    //  EVENTS
    private var pageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageSelected(position: Int) {}

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            val params = indicator.layoutParams as? FrameLayout.LayoutParams ?: return
            val finishWidth = tabs.width / 7
            val finishHeight = tabs.height - (5.dpToPx())
            val translation = (positionOffset + position) * finishWidth

            val widthDelta = abs((positionOffset * 2 - 1).pow(2) - 1) / 3 + 1
            val heightDelta = (positionOffset * 2 - 1).absoluteValue.minus(1).absoluteValue * (finishHeight / 5 )

            params.leftMargin = translation.toInt()
            params.width = (finishWidth * widthDelta).toInt()
            params.height = (finishHeight - heightDelta).toInt()

            indicator.layoutParams = params



            Log.i("TAB_LAYOUT_SCROLL", "position: $position;\t positionOffset: $positionOffset;\t positionOffsetPixels: $positionOffsetPixels;\t width: $widthDelta\t heightDelta: $heightDelta \theight: ${finishHeight - heightDelta}")
        }

    }

    private fun textViewDateRangeClick(view: View) {
        val animDuration = 300L
        val transition: Transition = AutoTransition()
        transition.setDuration(600)
        transition.addTarget(R.id.image)

        if (calendarLayout.visibility == View.VISIBLE) {
            calendarLayout.visibility = false.visibility()
            ivDateDateArrow.rotation = 0f
        } else {
            calendarLayout.visibility = true.visibility()
            ivDateDateArrow.rotation = 90f
        }


//        calendarLayout.visibility = (calendarLayout.visibility != View.VISIBLE).visibility()
//        if (calendarLayout.layoutParams.height <= 1 && calendarLayout.layoutParams.height >= 0) {
//            val anim = ExpandAnimation(calendarLayout)
//            anim.duration = animDuration
//            calendarLayout.startAnimation(anim)
//
//            val rotateAnim = RotateAnimation(0f, 90f,
//                ivDateDateArrow.width.toFloat() / 2,
//                ivDateDateArrow.height.toFloat() / 2)
//            rotateAnim.duration = animDuration
//            rotateAnim.fillAfter = true
//            calendarLayout.isFocusable = true
//            ivDateDateArrow.startAnimation(rotateAnim)
//        } else {
//            val anim = CollapseAnimation(calendarLayout)
//            anim.duration = animDuration
//            calendarLayout.startAnimation(anim)
//
//            val rotateAnim = RotateAnimation(90f, 0f,
//                ivDateDateArrow.width.toFloat() / 2,
//                ivDateDateArrow.height.toFloat() / 2
//            )
//            rotateAnim.fillAfter = true
//            rotateAnim.duration = animDuration
//            ivDateDateArrow.startAnimation(rotateAnim)
//        }
    }

    private fun dateSelected(calendar: CalendarView?, y: Int, m: Int, d: Int) {
        val gCalendar = GregorianCalendar(Locale.GERMANY)
        gCalendar.set(y, m, d, 0, 0, 1)
        val dayWeek = (gCalendar.get(GregorianCalendar.DAY_OF_WEEK) - 2 + 7) % 7
        val date = gCalendar.time
        val dateTime = gCalendar.timeInMillis

        pager.setCurrentItem(dayWeek, true)

        print(date)
        val even = SchedulersRepository.shared.getDateOdd(date)
        evenOddCurrDate = (even ?: 0) + 1

        gCalendar.set(Calendar.DAY_OF_WEEK, 2)
        val day1 = gCalendar.get(Calendar.DAY_OF_MONTH)
        val month1 = gCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        filterWeekStart = gCalendar.timeInMillis

        gCalendar.add(Calendar.DAY_OF_YEAR, 7)
        val day2 = gCalendar.get(Calendar.DAY_OF_MONTH)
        val month2 = gCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        filterWeekEnd = gCalendar.timeInMillis

        val oddValue = resources.getString(if (even != 0) R.string.even else R.string.odd)
        var curr = ""
        if (currentWeekStart <= dateTime && dateTime <= currentWeekEnd) {
            curr = resources.getString(R.string.curr_week)
        }
        tvDateRange.text = "$day1 $month1 - $day2 $month2 ($curr$oddValue)"
        updateEvents()
    }

    private fun createDateRange() {

    }

    private fun sandwitchSelected(view: View) {
        bottomSheetController.showSchedulersOptions()
    }

    private fun toggleClick(view: View) {
        bottomSheetController.toEvents()
    }

    private fun addSchedule(view: View) {
        bottomSheetController.clickAddLesson()
    }

    private fun toSupport(view: View) {
        navigationPresenter.showSupport()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            createTimeTable()
        }
    }

    companion object {
        val displayDF = SimpleDateFormat("dd MMMM")
    }

    //  ClassesAdapter Delegate
    override fun onClick(item: ScheduleItem) {
        if (item.name == "Базовый день") {
            showBaseDayAlert(item)
        }

        (activity as? MainActivity)?.let { activity ->
            activity.add(ScheduleItemDetailFragment(item))
        }
    }

    override fun onClick(item: EventModel) {
        (activity as? MainActivity)?.let { activity ->
            val eventFragment = SchedulerEventDetailFragment.newInstance(item.id)
            activity.add(eventFragment)
        }
    }
}