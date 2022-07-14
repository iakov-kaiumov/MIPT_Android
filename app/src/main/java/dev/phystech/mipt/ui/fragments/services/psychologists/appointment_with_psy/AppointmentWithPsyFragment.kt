package dev.phystech.mipt.ui.fragments.services.psychologists.appointment_with_psy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.api.UsersResponseModel
import android.widget.ArrayAdapter
import dev.phystech.mipt.adapters.PsychologistsTimesListAdapter
import dev.phystech.mipt.ui.fragments.services.psychologists.record_apply.RecordApplyFragment
import android.widget.AdapterView
import android.widget.Toast
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.ui.DayBinder
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder

import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import dev.phystech.mipt.base.utils.CommonUtils.Companion.dpToPx
import dev.phystech.mipt.models.api.PsyTimeResponseModel
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import okhttp3.internal.notify
import okhttp3.internal.notifyAll
import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.ArrayList

class AppointmentWithPsyFragment : BaseFragment(), AppointmentWithPsyContract.View {

    private var presenter: AppointmentWithPsyContract.Presenter? = null

    private lateinit var ivBack: ImageView

    private lateinit var llPsychologistTitle: LinearLayout
    private lateinit var llLocationTitle: LinearLayout
    private lateinit var psychologistsRecycler: RecyclerView
    private lateinit var endLine: View

    private lateinit var ibMonthBack: ImageButton
    private lateinit var ibMonthNext: ImageButton

    private lateinit var spinnerPsychologistTitle: Spinner
    private lateinit var spinnerPsychologistLocation: Spinner

    private var ignoreFirstPsychologistSpinnerChange = true;
    private var ignoreFirstLocationSpinnerChange = true;

    private val allPsychologists: ArrayList<UsersResponseModel.UserInfoModel> = arrayListOf()
    private var online: Boolean = false



    private lateinit var calendarView: com.kizitonwose.calendarview.CalendarView
    private lateinit var tvSelectedDateText: TextView

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val selectionFormatter = DateTimeFormatter.ofPattern("yyyy")
    private val events = mutableMapOf<LocalDate, Boolean>()

    private var minLocalDate: LocalDate? = null
    private var maxLocalDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_psychologist_appointment_with, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = AppointmentWithPsyPresenter(resources, bottomSheetController)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        if (online) {
            presenter?.loadPsychologistsTime(allPsychologists, null, null, "online")
        } else {
            presenter?.loadPsychologistsTime(allPsychologists, null, "dolg", "offline")
        }
    }

    override fun onStop() {
        presenter?.detach()
        spinnerPsychologistTitle.setOnItemSelectedListener(null)
        spinnerPsychologistLocation.setOnItemSelectedListener(null)
        super.onStop()
    }

    //  MVP VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)

        llPsychologistTitle = view.findViewById(R.id.llPsychologistTitle)
        llLocationTitle = view.findViewById(R.id.llLocationTitle)
        psychologistsRecycler = view.findViewById(R.id.psychologistsRecycler)
        endLine = view.findViewById(R.id.endLine)

        ibMonthBack = view.findViewById(R.id.ibMonthBack)
        ibMonthNext = view.findViewById(R.id.ibMonthNext)

        calendarView = view.findViewById(R.id.calendarView)
        tvSelectedDateText = view.findViewById(R.id.tvSelectedDateText)

        spinnerPsychologistTitle = view.findViewById(R.id.spinnerPsychologistTitle)
        spinnerPsychologistLocation = view.findViewById(R.id.spinnerPsychologistLocation)

        setContent()

        //  EVENTS

        ivBack.setOnClickListener(this::onBack)

        spinnerPsychologistTitle.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, itemSelected: View?, selectedItemPosition: Int, selectedId: Long) {
                if (ignoreFirstPsychologistSpinnerChange) {
                    ignoreFirstPsychologistSpinnerChange = !ignoreFirstPsychologistSpinnerChange
                } else {
                    selectSpinnerItem()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        spinnerPsychologistLocation.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, itemSelected: View?, selectedItemPosition: Int, selectedId: Long) {
                if (ignoreFirstLocationSpinnerChange) {
                    ignoreFirstLocationSpinnerChange = !ignoreFirstLocationSpinnerChange
                } else {
                    selectSpinnerItem()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        setupCalendar()

        ibMonthBack.setOnClickListener(this::onMonthBack)
        ibMonthNext.setOnClickListener(this::onMonthNext)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    //  OTHERS

    fun onBack(view: View) {
        navigationPresenter.popFragment()
    }

    fun onMonthBack(view: View) {
        calendarView.findFirstVisibleMonth()?.let {
            if (minLocalDate != null) {
                if (it.yearMonth.previous.year >= minLocalDate!!.year) {
                    if (it.yearMonth.previous.monthValue >= minLocalDate!!.monthValue) {
                        calendarView.smoothScrollToMonth(it.yearMonth.previous)
                    }
                }
            } else {
//                calendarView.smoothScrollToMonth(it.yearMonth.previous)
            }
        }
    }

    fun onMonthNext(view: View) {
        calendarView.findFirstVisibleMonth()?.let {
            if (maxLocalDate != null) {
                if (it.yearMonth.next.year <= maxLocalDate!!.year) {
                    if (it.yearMonth.next.monthValue <= maxLocalDate!!.monthValue) {
                        calendarView.smoothScrollToMonth(it.yearMonth.next)
                    }
                }
            } else {
//                calendarView.smoothScrollToMonth(it.yearMonth.next)
            }
        }
    }

    fun setContent() {

        if (online) {
            llLocationTitle.visibility = View.GONE
        } else {
            llLocationTitle.visibility = View.VISIBLE
            val adapterLocation: ArrayAdapter<String> = ArrayAdapter<String>(context!!, R.layout.item_spenner_title, arrayListOf(getString(R.string.psy_location_dolg), getString(R.string.psy_location_zhuck)))
            adapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPsychologistLocation.setAdapter(adapterLocation);
        }

        val data = arrayListOf(getString(R.string.psy_date_picker_psychologist_any))
        for(psy in allPsychologists) {
            psy.name?.let {
                data.add(it)
            }
        }

        val adapterTitles: ArrayAdapter<String> = ArrayAdapter<String>(context!!, R.layout.item_spenner_title, data)
        adapterTitles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPsychologistTitle.setAdapter(adapterTitles);

    }

    fun selectSpinnerItem() {
        if (online) {
            if (spinnerPsychologistTitle.selectedItemPosition == 0) {
                presenter?.loadPsychologistsTime(allPsychologists, null, null, "online")
            } else {
                presenter?.loadPsychologistsTime(allPsychologists, allPsychologists[spinnerPsychologistTitle.selectedItemPosition - 1].id, null, "online")
            }
        } else {
            if (spinnerPsychologistTitle.selectedItemPosition == 0) {
                if (spinnerPsychologistLocation.selectedItemPosition == 0) {
                    presenter?.loadPsychologistsTime(allPsychologists, null, "dolg", "offline")
                } else {
                    presenter?.loadPsychologistsTime(allPsychologists, null, "zhuck", "offline")
                }
            } else {
                if (spinnerPsychologistLocation.selectedItemPosition == 0) {
                    presenter?.loadPsychologistsTime(allPsychologists, allPsychologists[spinnerPsychologistTitle.selectedItemPosition - 1].id, "dolg", "offline")
                } else {
                    presenter?.loadPsychologistsTime(allPsychologists, allPsychologists[spinnerPsychologistTitle.selectedItemPosition - 1].id, "zhuck", "offline")
                }
            }
        }
    }

    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    override fun showLoader(visible: Boolean) {
        if (visible) {
            showProgress()
        } else {
            hideProgress()
        }
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {

        (adapter as PsychologistsTimesListAdapter).onClickListener = PsychologistsTimesListAdapter.OnClickListener {
            navigate(RecordApplyFragment.newInstance(psychologist = it))
        }

        psychologistsRecycler.adapter = adapter
    }

    private fun setupCalendar() {

        val daysOfWeek = TimeUtils.daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()

        calendarView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
            }
        })

        calendarView.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

//        if (savedInstanceState == null) {
            calendarView.post {
                selectDate(today)
            }
//        }

        setDayBinder()

        calendarView.monthScrollListener = {
            val monthsList = resources.getStringArray(R.array.month)
            tvSelectedDateText.text = "${monthsList.get(it.yearMonth.atDay(1).getMonthValue() - 1)} ${selectionFormatter.format(it.yearMonth.atDay(1))}"
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = LayoutInflater.from(view.context).inflate(R.layout.calendar_day_legend, view as ViewGroup, false)
        }

        calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.findViewById<FrameLayout>(R.id.legendLayout).children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.first().toString()
                    }
                }
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { calendarView.notifyDateChanged(it) }
            calendarView.notifyDateChanged(date)
            val monthsList = resources.getStringArray(R.array.month)
            tvSelectedDateText.text = "${monthsList.get(date.getMonthValue() - 1)} ${selectionFormatter.format(date)}"
            filterPsy(date)
        }
    }

    override fun setCalendarEventPoints(psychologists: ArrayList<PsyTimeResponseModel.PsyInfoModel>) {
        events.clear()
        minLocalDate = null
        maxLocalDate = null

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
        for (psychologist in psychologists) {
            psychologist.startTime?.date?.let { date ->
                try {

                    val localDate = LocalDate.parse(date, dateTimeFormatter)
                    events[localDate] = true

                    if (minLocalDate != null) {
                        if (localDate.isBefore(minLocalDate)) {
                            minLocalDate = localDate
                        }
                    } else {
                        minLocalDate = localDate
                    }

                    if (maxLocalDate != null) {
                        if (localDate.isAfter(maxLocalDate)) {
                            maxLocalDate = localDate
                        }
                    } else {
                        maxLocalDate = localDate
                    }

                } catch (e:Exception) { }
            }
        }
        setDayBinder()

        if (selectedDate != null) {
            filterPsy(selectedDate!!)
        } else {
            filterPsy(LocalDate.now())
        }

        calendarView.post {
            calendarView.findFirstVisibleMonth()?.let {
                if (minLocalDate != null) {
                    if (it.yearMonth.year < minLocalDate!!.year || (it.yearMonth.year >= minLocalDate!!.year && it.yearMonth.monthValue < minLocalDate!!.monthValue)) {
                        val yearMonth: YearMonth = YearMonth.of(minLocalDate!!.year, minLocalDate!!.monthValue)
                        calendarView.smoothScrollToMonth(yearMonth)
                    }
                }

                if (maxLocalDate != null) {
                    if (it.yearMonth.next.year > maxLocalDate!!.year || (it.yearMonth.year <= maxLocalDate!!.year && it.yearMonth.monthValue > maxLocalDate!!.monthValue)) {
                        val yearMonth: YearMonth = YearMonth.of(maxLocalDate!!.year, maxLocalDate!!.monthValue)
                        calendarView.smoothScrollToMonth(yearMonth)
                    }
                }

                if (minLocalDate == null && maxLocalDate == null) {
                    val yearMonth: YearMonth = YearMonth.of(LocalDate.now().year, LocalDate.now().monthValue)
                    calendarView.smoothScrollToMonth(yearMonth)
                }
            }
        }
    }

    private fun filterPsy(date: LocalDate) {

        val filtredItems: ArrayList<PsyTimeResponseModel.PsyInfoModel> = arrayListOf()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())

        presenter?.getPsychologistsTimes()?.let { psychologistsTimes ->
            for (item in psychologistsTimes) {
                item.startTime?.date?.let { dateApply ->
                    val itemLocalDate = LocalDate.parse(dateApply, dateTimeFormatter)
                    if (date.isEqual(itemLocalDate))
                        filtredItems.add(item)
                }

            }
        }

        psychologistsRecycler.adapter?.let { adapter ->
            (adapter as PsychologistsTimesListAdapter).items.clear()
            (adapter as PsychologistsTimesListAdapter).items.addAll(filtredItems)
            adapter.notifyDataSetChanged()
        }

        if (filtredItems.isNotEmpty()) {
            endLine.visibility = View.VISIBLE
        } else {
            endLine.visibility = View.GONE
        }
    }

    private fun setDayBinder() {

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val dayText = view.findViewById<TextView>(R.id.dayText)
            val dotView = view.findViewById<View>(R.id.dotView)
            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.dayText
                val dotView = container.dotView

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    when (day.date) {
                        today -> {
                            textView.setTextColor(ContextCompat.getColor(context!!, R.color.black))
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            dotView.visibility = View.INVISIBLE
                        }
                        selectedDate -> {
                            textView.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                            dotView.visibility = View.INVISIBLE
                        }
                        else -> {
                            textView.setTextColor(ContextCompat.getColor(context!!, R.color.black))
                            textView.background = null
                            dotView.visibility = if (events[day.date] != null && events[day.date] == true) View.VISIBLE else View.GONE
                        }
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                    dotView.visibility = View.INVISIBLE
                }
            }
        }
    }

    companion object {
        fun newInstance(psychologists: ArrayList<UsersResponseModel.UserInfoModel>, online: Boolean): BaseFragment {
            val fragment = AppointmentWithPsyFragment()
            fragment.allPsychologists.clear()
            fragment.allPsychologists.addAll(psychologists)
            fragment.online = online
            return fragment
        }
    }
}