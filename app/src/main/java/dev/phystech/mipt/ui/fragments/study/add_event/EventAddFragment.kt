package dev.phystech.mipt.ui.fragments.study.add_event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.SchedulerType
import dev.phystech.mipt.models.add_event_view.AddEventFieldsViewModel
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType.*
import dev.phystech.mipt.ui.fragments.study.auditory_list.AuditoryListFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditFragment
import dev.phystech.mipt.ui.fragments.study.teachers_list.TeachersListFragment
import dev.phystech.mipt.ui.fragments.study.user_scheduler_list.UserSchedulerListFragment
import dev.phystech.mipt.ui.utils.CollapseAnimation
import dev.phystech.mipt.ui.utils.ExpandAnimation
import dev.phystech.mipt.utils.visibility

class EventAddFragment: BaseFragment(), EventAddContract.View {

    private lateinit var type: AddEventType
    private var modelId: Int? = null
    private var presenter: EventAddContract.Presenter? = null

    private lateinit var ivBack: ImageView
    private lateinit var ivSave: ImageView

    private lateinit var tvName: MaterialAutoCompleteTextView
    private lateinit var tvAuditory: MaterialAutoCompleteTextView
    private lateinit var tvLector: MaterialAutoCompleteTextView
    private lateinit var tvDate: MaterialAutoCompleteTextView
    private lateinit var tvSchedule: MaterialAutoCompleteTextView
    private lateinit var tvTimeBegin: MaterialAutoCompleteTextView
    private lateinit var tvTimeEnd: MaterialAutoCompleteTextView
    private lateinit var tvType: MaterialAutoCompleteTextView

    private lateinit var boxSchedule: View
    private lateinit var boxTimeBegin: View
    private lateinit var boxTimeEnd: View
    private lateinit var boxTeacher: View
    private lateinit var boxAuditory: View
    private lateinit var boxDate: View

    private lateinit var wrapName: TextInputLayout
    private lateinit var wrapAuditory: TextInputLayout
    private lateinit var wrapLector: TextInputLayout
    private lateinit var wrapSchedule: TextInputLayout
    private lateinit var wrapDate: TextInputLayout
    private lateinit var wrapTimeBegin: TextInputLayout
    private lateinit var wrapTimeEnd: TextInputLayout
    private lateinit var wrapType: TextInputLayout

    private lateinit var etPersonalNotes: EditText
    private lateinit var btnAddLink: MaterialButton
    private lateinit var switchAllDay: SwitchMaterial
    private lateinit var rvLink: RecyclerView
    private lateinit var llDeadlineFields: LinearLayout
    private lateinit var layoutTimeSlice: View


    //  ANDROID LIFE CIRCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typeInt = arguments?.getInt(KEY_TYPE) ?: return
        type = AddEventType.getByTypeValue(typeInt) ?: SimpleEvent
        if (arguments?.containsKey(KEY_SCHEDULE_ID) == true) {
            modelId = arguments?.getInt(KEY_SCHEDULE_ID)
        }

        when (type) {
            SimpleEvent -> presenter = EventAddPresenter(resources, bottomSheetController);
            Deadline -> presenter = DeadlineAddPresenter(modelId, resources, bottomSheetController);
            DeadlineEdit -> presenter = DeadlineEditPresenter(modelId, resources, bottomSheetController)
        }


        presenter?.receiver?.let {
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(it, IntentFilter(TeachersListFragment.ACTION))

            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(it, IntentFilter(AuditoryListFragment.ACTION))

            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(it, IntentFilter(UserSchedulerListFragment.ACTION))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.receiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scheduler_event_add, container, false)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }


    //  MVP BASE VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        ivSave = view.findViewById(R.id.ivSave)
        tvName = view.findViewById(R.id.tvName)
        tvAuditory = view.findViewById(R.id.tvAuditory)
        tvLector = view.findViewById(R.id.tvLector)
        tvDate = view.findViewById(R.id.tvDate)
        tvSchedule = view.findViewById(R.id.tvSchedule)
        tvTimeBegin = view.findViewById(R.id.tvTimeBegin)
        tvTimeEnd = view.findViewById(R.id.tvTimeEnd)
        tvType = view.findViewById(R.id.tvType)
        boxTeacher = view.findViewById(R.id.boxTeacher)
        boxAuditory = view.findViewById(R.id.boxAuditory)
        boxSchedule = view.findViewById(R.id.boxSchedule)
        boxDate = view.findViewById(R.id.boxDate)
        boxTimeBegin = view.findViewById(R.id.boxTimeBegin)
        boxTimeEnd = view.findViewById(R.id.boxTimeEnd)
        etPersonalNotes = view.findViewById(R.id.etPersonalNotes)
        wrapName = view.findViewById(R.id.wrapName)
        wrapAuditory = view.findViewById(R.id.wrapAuditory)
        wrapLector = view.findViewById(R.id.wrapLector)
        wrapSchedule = view.findViewById(R.id.wrapSchedule)
        wrapDate = view.findViewById(R.id.wrapDate)
        wrapTimeBegin = view.findViewById(R.id.wrapTimeBegin)
        wrapTimeEnd = view.findViewById(R.id.wrapTimeEnd)
        wrapType = view.findViewById(R.id.wrapType)
        switchAllDay = view.findViewById(R.id.switchAllDay)
        btnAddLink = view.findViewById(R.id.btnAddLink)
        rvLink = view.findViewById(R.id.rvLink)
        layoutTimeSlice = view.findViewById(R.id.layoutTimeSlice)
        llDeadlineFields = view.findViewById(R.id.llDeadlineFields)


        boxSchedule.setOnClickListener(this::clickSchedule)
        boxTimeBegin.setOnClickListener(this::clickTimeBegin)
        boxTimeEnd.setOnClickListener(this::clickTimeEnd)
        boxTeacher.setOnClickListener(this::clickTeacher)
        boxAuditory.setOnClickListener(this::clickAuditory)
        boxDate.setOnClickListener(this::clickDate)
        switchAllDay.setOnCheckedChangeListener(this::switchAllDayChanged)
        btnAddLink.setOnClickListener(this::btnAddLinkClick)
        ivBack.setOnClickListener(this::back)
        ivSave.setOnClickListener(this::saveClick)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }



    /***    MVP VIEW
     * @see EventAddContract.View
     */
    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    override fun setContent(model: AddEventFieldsViewModel) {
        tvName.setText(model.name)
        tvAuditory.setText(model.auditory)
        tvLector.setText(model.teacher)
        tvSchedule.setText(model.schedule)
        etPersonalNotes.setText(model.notes)
        tvDate.setText(model.date)
        tvTimeBegin.setText(model.timeBegin)
        tvTimeEnd.setText(model.timeEnd)
        switchAllDay.isChecked = model.allDay ?: false
        tvType.setText(model.type)
    }

    override fun setNameError(valueId: Int) {
        wrapName.error = resources.getString(valueId)
    }

    override fun setAuditoryError(valueId: Int) {
        wrapAuditory.error = resources.getString(valueId)
    }

    override fun setTeacherError(valueId: Int) {
        wrapLector.error = resources.getString(valueId)
    }

    override fun setScheduleError(valueId: Int) {
        wrapSchedule.error = resources.getString(valueId)
    }

    override fun setDateError(valueId: Int) {
        wrapDate.error = resources.getString(valueId)
    }

    override fun setTimeBeginError(valueId: Int) {
        wrapTimeBegin.error = resources.getString(valueId)
    }

    override fun setTimeEndError(valueId: Int) {
        wrapTimeEnd.error = resources.getString(valueId)
    }

    override fun setTypesError(valueId: Int) {
        wrapType.error = resources.getString(valueId)
    }

    override fun setDeadlinesVisibilist(isVisible: Boolean) {
        llDeadlineFields.visibility = isVisible.visibility()
    }

    override fun updateTimeSliceVisibility(isChecked: Boolean) {
        val anim: Animation = if (isChecked) {
            ExpandAnimation(layoutTimeSlice).apply { duration = 100 }
        } else {
            CollapseAnimation(layoutTimeSlice).apply { duration = 100 }
        }

        layoutTimeSlice.startAnimation(anim)
    }

    override fun setTypes(types: List<SchedulerType>) {
        val adapter = SchedulerEditFragment.SchedulerTypeAdapter(requireContext(), types)
        tvType.setAdapter(adapter)
    }

    override fun <VH : RecyclerView.ViewHolder> setLinksAdapter(adapter: RecyclerView.Adapter<VH>) {
        rvLink.adapter = adapter
    }

    override fun showTeacherList() {
        val teacherFragment = TeachersListFragment()
        navigationPresenter.pushFragment(teacherFragment, true)
    }

    override fun showAuditoryList() {
        val auditoryFragment = AuditoryListFragment()
        navigationPresenter.pushFragment(auditoryFragment, true)
    }

    override fun showScheduleList() {
        val schedulersFragment = UserSchedulerListFragment().apply {
            arguments = Bundle().apply {
                putString(UserSchedulerListFragment.KEY_TYPE, UserSchedulerListFragment.TYPE_BROADCASE)
            }
        }
        navigationPresenter.pushFragment(schedulersFragment, true)
    }

    override fun back() {
        navigationPresenter.popFragment()
    }

    override fun buildModel(): AddEventFieldsViewModel {
        return AddEventFieldsViewModel().apply {
            name = tvName.text.toString()
            auditory = tvAuditory.text.toString()
            teacher = tvLector.text.toString()
            schedule = tvSchedule.text.toString()
            notes = etPersonalNotes.text.toString()
            date = tvDate.text.toString()
            timeBegin = tvTimeBegin.text.toString()
            timeEnd = tvTimeEnd.text.toString()
            allDay = switchAllDay.isChecked
            type = tvType.text.toString()
        }
    }


    //  EVENTS
    private fun clickSchedule(view: View) {
        presenter?.clickSchedule()
    }

    private fun clickTimeBegin(view: View) {
        TimePickerDialog(requireContext(), { v: TimePicker, h: Int, m: Int ->
            tvTimeBegin.requestFocus()
            presenter?.selectTimeBegin(h, m)
        }, 0, 0, true).show()
    }

    private fun clickTimeEnd(view: View) {
        TimePickerDialog(requireContext(), { v: TimePicker, h: Int, m: Int ->
            tvTimeEnd.requestFocus()
            presenter?.selectTimeEnd(h, m)
        }, 0, 0, true).show()
    }

    private fun clickTeacher(view: View) {
        presenter?.clickTeacher()
    }

    private fun clickAuditory(view: View) {
        presenter?.clickAuditory()
    }

    private fun clickDate(view: View) {
        val picker = DatePickerDialog(requireContext(), R.style.PickerTheme)
        picker.setOnDateSetListener({ _, y, m, d ->
            tvDate.requestFocus()
            presenter?.selectDate(y, m, d)
        })
        picker.show()
    }

    private fun switchAllDayChanged(btn: CompoundButton, isAllDayChecked: Boolean) {
        presenter?.allDayChange(isAllDayChecked)
    }

    private fun btnAddLinkClick(view: View) {
        presenter?.addLinkClick()
    }

    private fun saveClick(view: View) {
        presenter?.saveClicked()
    }

    private fun back(view: View) {
        back()
    }



    companion object {
        const val KEY_TYPE = "add_event.key.type"
        const val KEY_SCHEDULE_ID = "add_event.key.schedule_id"

        fun newInstance(type: AddEventType, scheduleId: Int? = null): BaseFragment {
            val fragment = EventAddFragment()
            val arguments = Bundle().apply {
                putInt(KEY_TYPE, type.typeValue)
                scheduleId?.let { putInt(KEY_SCHEDULE_ID, it) }
            }

            return fragment.apply { this.arguments = arguments }
        }
    }


}