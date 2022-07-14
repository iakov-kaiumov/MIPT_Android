package dev.phystech.mipt.ui.fragments.study.scheduler_event_edit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.SchedulerType
import dev.phystech.mipt.ui.fragments.study.auditory_list.AuditoryListFragment
import dev.phystech.mipt.ui.fragments.study.events.SchedulerEventType
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditFragment
import dev.phystech.mipt.ui.fragments.study.teachers_list.TeachersListFragment
import dev.phystech.mipt.ui.utils.CollapseAnimation
import dev.phystech.mipt.ui.utils.ExpandAnimation
import dev.phystech.mipt.utils.visibility
import java.util.ArrayList

/** Изменение существующего события
 */
class SchedulerEventEditFragment: BaseFragment(), SchedulerEventEditContract.View {

    lateinit var tvName: EditText
    lateinit var tvAuditory: EditText
    lateinit var tvLector: EditText
    lateinit var tvDate: EditText
    lateinit var tvTimeBegin: EditText
    lateinit var tvTimeEnd: EditText
    lateinit var tvType: MaterialAutoCompleteTextView

    lateinit var wrapName: TextInputLayout
    lateinit var wrapAuditory: TextInputLayout
    lateinit var wrapLector: TextInputLayout
    lateinit var wrapDate: TextInputLayout
    lateinit var wrapTimeBegin: TextInputLayout
    lateinit var wrapTimeEnd: TextInputLayout
    lateinit var wrapType: TextInputLayout

    lateinit var switchAllDay: SwitchMaterial
    lateinit var rvLink: RecyclerView
    lateinit var layoutTimeSlice: LinearLayout

    lateinit var rlClearChanger: RelativeLayout
    lateinit var rlDelete: RelativeLayout
    lateinit var btnAddLink: Button
    lateinit var ivEdit: ImageView
    lateinit var ivBack: ImageView

    lateinit var boxDate: View
    lateinit var boxTimeBegin: View
    lateinit var boxTimeEnd: View
    lateinit var boxAuditory: View
    lateinit var boxTeacher: View

    private lateinit var presenter: SchedulerEventEditContract.Presenter
    private var editForGroup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey(KEY_ID) == true) {
            val id = arguments!!.getInt(KEY_ID)
            editForGroup = arguments?.getBoolean(KEY_FOR_GROUP) ?: false
            presenter = SchedulerEventEditPresenter(id, resources, bottomSheetController)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scheduler_event_edit, container, false)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(boradcastReceiver, IntentFilter(AuditoryListFragment.ACTION))
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(boradcastReceiver, IntentFilter(TeachersListFragment.ACTION))
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(boradcastReceiver)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        tvName = view.findViewById(R.id.tvName)
        tvAuditory = view.findViewById(R.id.tvAuditory)
        tvLector = view.findViewById(R.id.tvLector)
        tvDate = view.findViewById(R.id.tvDate)
        tvTimeBegin = view.findViewById(R.id.tvTimeBegin)
        tvTimeEnd = view.findViewById(R.id.tvTimeEnd)
        tvType = view.findViewById(R.id.tvType)
        wrapName = view.findViewById(R.id.wrapName)
        wrapAuditory = view.findViewById(R.id.wrapAuditory)
        wrapLector = view.findViewById(R.id.wrapLector)
        wrapDate = view.findViewById(R.id.wrapDate)
        wrapTimeBegin = view.findViewById(R.id.wrapTimeBegin)
        wrapTimeEnd = view.findViewById(R.id.wrapTimeEnd)
        wrapType = view.findViewById(R.id.wrapType)
        switchAllDay = view.findViewById(R.id.switchAllDay)
        rvLink = view.findViewById(R.id.rvLink)
        layoutTimeSlice = view.findViewById(R.id.layoutTimeSlice)
        rlClearChanger = view.findViewById(R.id.rlClearChanger)
        rlDelete = view.findViewById(R.id.rlDelete)
        btnAddLink = view.findViewById(R.id.btnAddLink)
        ivEdit = view.findViewById(R.id.ivEdit)
        ivBack = view.findViewById(R.id.ivBack)
        boxDate = view.findViewById(R.id.boxDate)
        boxTimeBegin = view.findViewById(R.id.boxTimeBegin)
        boxTimeEnd = view.findViewById(R.id.boxTimeEnd)
        boxAuditory = view.findViewById(R.id.boxAuditory)
        boxTeacher = view.findViewById(R.id.boxTeacher)


        btnAddLink.setOnClickListener(this::addLinkClicked)
        boxAuditory.setOnClickListener(this::auditoryClicked)
        boxTeacher.setOnClickListener(this::lectorClicked)
        boxDate.setOnClickListener(this::dateClicked)
        boxTimeBegin.setOnClickListener(this::timeBeginClicked)
        boxTimeEnd.setOnClickListener(this::timeEndClicked)
        switchAllDay.setOnCheckedChangeListener(this::switchAllDayChanged)
        ivEdit.setOnClickListener(this::saveClick)
        ivBack.setOnClickListener(this::back)
        rlClearChanger.setOnClickListener(this::clearChangesClick)


        tvName.doOnTextChanged {_, _, _, _ -> wrapName.error = null }
        tvAuditory.doOnTextChanged {_, _, _, _ -> wrapAuditory.error = null }
        tvLector.doOnTextChanged {_, _, _, _ -> wrapLector.error = null }
        tvDate.doOnTextChanged {_, _, _, _ -> wrapDate.error = null }
        tvTimeBegin.doOnTextChanged {_, _, _, _ -> wrapTimeBegin.error = null }
        tvTimeEnd.doOnTextChanged {_, _, _, _ -> wrapTimeEnd.error = null }
        tvType.doOnTextChanged {_, _, _, _ -> wrapType.error = null }

    }


    //  OTHERS
    override fun setContent(model: EventModel) {
        tvName.setText(model.name)
        tvDate.setText(model.startTime)
        tvTimeBegin.setText(model.startTime)
        tvTimeEnd.setText(model.endTime)
        switchAllDay.isChecked = model.allDay
        tvAuditory.setText(model.auditorium.firstOrNull()?.name)
        tvLector.setText(model.teachers.firstOrNull()?.name)

        SchedulerEventType.getByType(model.type)?.getDysplayedResId()?.let { typeId ->
            tvType.setText(typeId)
        }


    }

    override fun updateTimeSliceVisibility(isChecked: Boolean) {
        val anim: Animation = if (isChecked) {
            CollapseAnimation(layoutTimeSlice).apply { duration = 100 }
        } else {
            ExpandAnimation(layoutTimeSlice).apply { duration = 100 }
        }

        layoutTimeSlice.startAnimation(anim)
    }

    override fun buildModel(): EventModel {
        val mType = SchedulerEventType.values()
            .firstOrNull { v -> resources.getString(v.getDysplayedResId()) == tvType.text.toString() }

        val model = EventModel().apply {
            allDay = switchAllDay.isChecked
            name = tvName.text.toString()
            prof = tvLector.text.toString()
            place = tvAuditory.text.toString()
            type = mType?.typeValue ?: ""
            startTime = tvTimeBegin.text.toString()
            endTime = tvTimeEnd.text.toString()
            teachers
            auditorium
            urls
        }

        return model
    }

    override fun setAuditory(value: String) {
        tvAuditory.setText(value)
    }

    override fun setTeacher(value: String) {
        tvLector.setText(value)
    }

    override fun setDate(value: String) {
        tvDate.setText(value)
    }

    override fun setTimeBegin(value: String) {
        tvTimeBegin.setText(value)
    }

    override fun setTimeEnd(value: String) {
        tvTimeEnd.setText(value)
    }

    override fun setTypes(types: ArrayList<SchedulerType>) {
        val adapter = SchedulerEditFragment.SchedulerTypeAdapter(requireContext(), types)
        tvType.setAdapter(adapter)
    }

    override fun <VH : RecyclerView.ViewHolder> setLinksAdapter(adapter: RecyclerView.Adapter<VH>) {
        rvLink.adapter = adapter
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

    override fun setResetChangesVisibility(isVisible: Boolean) {
        rlClearChanger.visibility = isVisible.visibility()
    }

    override fun showTeacherList() {
        val teachersFragment = TeachersListFragment()
        navigationPresenter.pushFragment(teachersFragment, true)
    }

    override fun showAuditoryList() {
        val auditoriesFragment = AuditoryListFragment()
        navigationPresenter.pushFragment(auditoriesFragment, true)
    }

    override fun back() {
        navigationPresenter.popFragment()
    }




    //  EVENTS
    private val boradcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return
                val extras = intent.extras ?: return

                when (intent.action) {
                    AuditoryListFragment.ACTION -> {
                        if (extras.containsKey(AuditoryListFragment.DATA_ID)) {
                            val id = extras.getString(AuditoryListFragment.DATA_ID) ?: return
                            presenter.selectAuditory(id)

//                            SchedulePlaceRepository.shared.getById(id) {
//                                selectedAuditory = it
//                                activity?.runOnUiThread {
//                                    setAuditory()
//                                }
//                            }
                        }
                    }

                    TeachersListFragment.ACTION -> {
                        if (intent.hasExtra(TeachersListFragment.DATA_ID)) {
                            val id = intent.getStringExtra(TeachersListFragment.DATA_ID) ?: return
                            presenter.selectTeacher(id)
                        } else if (intent.hasExtra(TeachersListFragment.DATA_FIELD)) {
                            val name = intent.getStringExtra(TeachersListFragment.DATA_FIELD) ?: return
                            presenter.selectTeacherByName(name)
                        }
                    }
                }
            }
        }
    }

    private fun addLinkClicked(view: View) {
        presenter.addLinkClick()
    }

    private fun auditoryClicked(view: View) {
        presenter.clickAuditory()
    }

    private fun lectorClicked(view: View) {
        presenter.clickTeacher()
    }

    private fun dateClicked(view: View) {
        val picker = DatePickerDialog(requireContext(), R.style.PickerTheme)
        picker.setOnDateSetListener({ _, y, m, d ->
            presenter.selectDate(y, m, d)
        })
        picker.show()
    }

    private fun timeBeginClicked(view: View) {
        TimePickerDialog(requireContext(), { v: TimePicker, h: Int, m: Int ->
            presenter.selectTimeBegin(h, m)
        }, 0, 0, true).show()
    }

    private fun timeEndClicked(view: View) {
        TimePickerDialog(requireContext(), { v: TimePicker, h: Int, m: Int ->
            presenter.selectTimeEnd(h, m)
        }, 0, 0, true).show()

    }

    private fun switchAllDayChanged(button: CompoundButton, isChecked: Boolean) {
        updateTimeSliceVisibility(isChecked)
    }

    private fun saveClick(view: View) {
        presenter.saveClicked(editForGroup)
    }

    private fun clearChangesClick(view: View) {
        presenter.resetChanges()
    }

    fun back(view: View) {
        back()
    }


    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    companion object {

        const val KEY_ID = "event_fragment.id"
        const val KEY_FOR_GROUP = "event_fragment.edit_for_group"

        fun newInstance(id: Int, forGroup: Boolean): BaseFragment {
            return SchedulerEventEditFragment().apply {
                arguments = bundleOf(
                    KEY_ID to id,
                    KEY_FOR_GROUP to forGroup
                )
            }
        }

    }

}
