package dev.phystech.mipt.ui.fragments.study.scheduler_edit

import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.LinksAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.*
import dev.phystech.mipt.repositories.*
import dev.phystech.mipt.ui.fragments.study.auditory_list.AuditoryListFragment
import dev.phystech.mipt.ui.fragments.study.teachers_list.TeachersListFragment
import dev.phystech.mipt.utils.visibility

/** Изменение занятия. Также используется для создания нового занятия.
 *
 * Экземпляр создается методом [SchedulerEditFragment.newInstance]
 * Поля:
 * id - id занятия (необязательное поле)
 * forGroup - bool (создавать/изменять для себя или для всех)
 * type - Создание занятия или изменение занятия
  */
class SchedulerEditFragment: BaseFragment(), SchedulerEditContract.View {

    lateinit var tvCourse: MaterialAutoCompleteTextView
    lateinit var tvAuditory: MaterialAutoCompleteTextView
    lateinit var tvLector: MaterialAutoCompleteTextView
    lateinit var tvDayOfWeek: MaterialAutoCompleteTextView
    lateinit var tvTimeBegin: MaterialAutoCompleteTextView
    lateinit var tvTimeEnd: MaterialAutoCompleteTextView
    lateinit var tvRepeat: MaterialAutoCompleteTextView
    lateinit var tvSchedulerType: MaterialAutoCompleteTextView
    lateinit var tvCategory: MaterialAutoCompleteTextView

    lateinit var wrapCourse: TextInputLayout
    lateinit var wrapAuditory: TextInputLayout
    lateinit var wrapLector: TextInputLayout
    lateinit var wrapDayOfWeek: TextInputLayout
    lateinit var wrapTimeBegin: TextInputLayout
    lateinit var wrapTimeEnd: TextInputLayout
    lateinit var wrapRepeat: TextInputLayout
    lateinit var wrapSchedulerType: TextInputLayout
    lateinit var wrapCategory: TextInputLayout

    lateinit var tvTitle: TextView
    lateinit var btnAddLink: Button
    lateinit var rvLink: RecyclerView
    lateinit var ivBack: ImageView
    lateinit var ivEdit: ImageView

    lateinit var boxCourse: View
    lateinit var boxAuditory: View
    lateinit var boxTeacher: View

    lateinit var rlClearChanger: RelativeLayout
    lateinit var rlDelete: RelativeLayout

    private lateinit var presenter: SchedulerEditPresenter

    private var schedulerId: Int? = null
    private var forGroup: Boolean = false
    private var schedulerTypes: List<SchedulerType> = emptyList()
    private var courses: List<SportSectionModel> = emptyList()
    private var scheduleItem: ScheduleItem? = null

    private lateinit var currType: Type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typeValue = arguments?.getInt(ARG_KEY_TYPE)
        currType = Type.getByTypeValue(typeValue ?: -1) ?: throw IllegalArgumentException("Incorrect argument")

        if (arguments?.containsKey(ARG_KEY_ID) == true) {
            arguments?.getInt(ARG_KEY_ID)?.let { schedulerId = it }
            arguments?.getBoolean(ARG_KEY_FOR_GROUP, false)?.let { forGroup = it }
        }

        presenter = SchedulerEditPresenter(resources, bottomSheetController, currType, schedulerId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheduler_edit, container, false)

        val auditoryFilter = IntentFilter(AuditoryListFragment.ACTION)
        val teacherFilter = IntentFilter(TeachersListFragment.ACTION)

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, auditoryFilter)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, teacherFilter)

        return view
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        tvCourse = view.findViewById(R.id.tvCourse)
        tvAuditory = view.findViewById(R.id.tvAuditory)
        tvLector = view.findViewById(R.id.tvLector)
        tvDayOfWeek = view.findViewById(R.id.tvDayOfWeek)
        tvTimeBegin = view.findViewById(R.id.tvTimeBegin)
        tvTimeEnd = view.findViewById(R.id.tvTimeEnd)
        tvRepeat = view.findViewById(R.id.tvRepeat)
        tvSchedulerType = view.findViewById(R.id.tvSchedulerType)
        tvCategory = view.findViewById(R.id.tvCategory)

        tvTitle = view.findViewById(R.id.tvTitle)
        btnAddLink = view.findViewById(R.id.btnAddLink)
        rvLink = view.findViewById(R.id.rvLink)
        ivBack = view.findViewById(R.id.ivBack)
        ivEdit = view.findViewById(R.id.ivEdit)

        wrapCourse = view.findViewById(R.id.wrapCourse)
        wrapAuditory = view.findViewById(R.id.wrapAuditory)
        wrapLector = view.findViewById(R.id.wrapLector)
        wrapDayOfWeek = view.findViewById(R.id.wrapDayOfWeek)
        wrapTimeBegin = view.findViewById(R.id.wrapTimeBegin)
        wrapTimeEnd = view.findViewById(R.id.wrapTimeEnd)
        wrapRepeat = view.findViewById(R.id.wrapRepeat)
        wrapSchedulerType = view.findViewById(R.id.wrapSchedulerType)
        wrapCategory = view.findViewById(R.id.wrapCategory)

        boxCourse = view.findViewById(R.id.boxCourse)
        boxTeacher = view.findViewById(R.id.boxTeacher)
        boxAuditory = view.findViewById(R.id.boxAuditory)

        rlClearChanger = view.findViewById(R.id.rlClearChanger)
        rlDelete = view.findViewById(R.id.rlDelete)

        boxTeacher.setOnClickListener {
            val fragment = TeachersListFragment()
            navigationPresenter.pushFragment(fragment, true)

        }

        boxAuditory.setOnClickListener {
            val fragment = AuditoryListFragment()
            navigationPresenter.pushFragment(fragment, true)
        }

        tvCourse.doOnTextChanged { _, _, _, _ -> wrapCourse.error = null }
        tvAuditory.doOnTextChanged { _, _, _, _ -> wrapAuditory.error = null }
        tvLector.doOnTextChanged { _, _, _, _ -> wrapLector.error = null }
        tvDayOfWeek.doOnTextChanged { _, _, _, _ -> wrapDayOfWeek.error = null }
        tvTimeBegin.doOnTextChanged { _, _, _, _ -> wrapTimeBegin.error = null }
        tvTimeEnd.doOnTextChanged { _, _, _, _ -> wrapTimeEnd.error = null }
        tvRepeat.doOnTextChanged { _, _, _, _ -> wrapRepeat.error = null }
        tvSchedulerType.doOnTextChanged { _, _, _, _ -> wrapSchedulerType.error = null }
        tvCategory.doOnTextChanged { _, _, _, _ -> wrapCategory.error = null }

        if (schedulerId == null) {
            tvTitle.setText(R.string.edit_create)
        } else {
            tvTitle.setText(R.string.edit_change)
        }

        btnAddLink.setOnClickListener {
            presenter.addLinkClick()
        }

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

        ivEdit.setOnClickListener {
            val titleId = if (forGroup) R.string.edit_confirm_group_title else R.string.edit_confirm_self_title
            val descriptionId = if (forGroup) R.string.edit_confirm_group_description else R.string.edit_confirm_self_description

            AlertDialog.Builder(requireContext())
                .setNegativeButton(R.string.edit_cancel, null)
                .setTitle(titleId)
                .setMessage(descriptionId)
                .setPositiveButton(R.string.edit_save, {_, _ ->
                    val scheduler = buildScheduler() ?: return@setPositiveButton

                    if (schedulerId == null) { presenter.saveClick(scheduler, forGroup) }
                    else { presenter.editClick(scheduler, forGroup) }
                }).create()
//                .show()

            val scheduler = buildScheduler() ?: return@setOnClickListener

            if (schedulerId == null) { presenter.saveClick(scheduler, forGroup) }
            else { presenter.editClick(scheduler, forGroup) }



        }

        rlClearChanger.setOnClickListener {
            schedulerId?.let { schedulerId ->
                showProgress()
                SchedulersRepository.shared.resetChanges(schedulerId) {
                    hideProgress()

                    if (it) {
                        navigationPresenter.showStudy()
                        showMessage(R.string.changes_reseted)
                    } else {
                        showMessage(R.string.message_some_error_try_late)
                    }
                }

            }
        }

        rlDelete.setOnClickListener {
            //  TODO: Delete scheduler
        }


        if (schedulerId != null) {
            SchedulersRepository.shared.getSchedulerItemById(schedulerId!!)?.let { schedulerItem ->
                this.scheduleItem = schedulerItem
                setContent(schedulerItem)
            }
        } else {

        }

        rlClearChanger.visibility = (currType == Type.Edit).visibility()
        rlDelete.visibility = (currType == Type.Edit).visibility()
        wrapCategory.visibility = (currType == Type.Create).visibility()
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  OTHERS
    private fun setContent(schedulerItem: ScheduleItem) {
        val weekDays = resources.getStringArray(R.array.weeks)
        tvLector.setText(schedulerItem.prof)
        tvCourse.setText(schedulerItem.name)
        tvAuditory.setText(schedulerItem.auditorium.firstOrNull()?.name ?: schedulerItem.place)
        tvDayOfWeek.setText(weekDays[schedulerItem.day - 1])
        tvTimeBegin.setText(schedulerItem.startTime)
        tvTimeEnd.setText(schedulerItem.endTime)
        resources.getStringArray(R.array.scheduler_repeat).getOrNull(schedulerItem.evenodd)?.let {
            tvRepeat.setText(it)
        }
        courses.firstOrNull { v -> v.id == schedulerItem.course }?.let {
            tvCategory.setText(it.name)
        }


        val type = when (schedulerItem.type.toLowerCase()) {
            "lec" -> resources.getString(R.string.schedule_item_lec)
            "sem" -> resources.getString(R.string.schedule_item_sem)
            "lab" -> resources.getString(R.string.schedule_item_lab)
            else -> null
        }

        tvSchedulerType.setText(type)


    }


    //  MVP VIEW
    override fun setCourses(value: ArrayList<String>) {
        setAdapter(tvCourse, value)
    }

    override fun setAuditories(value: List<String>) {
        setAdapter(tvAuditory, value)
    }

    override fun setLectors(value: List<String>) {
        setAdapter(tvLector, value)
    }

    override fun setDayOfWeeks(value: List<String>) {
        setAdapter(tvDayOfWeek, value)
    }

    override fun setBeginTimes(value: List<String>) {
        val diffTime = resources.getString(R.string.diff_time)
        val list = value.toMutableList().apply { add(diffTime) }

        setAdapter(tvTimeBegin, list, object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent ?: return

                if (position == list.size - 1) {
                    TimePickerDialog(requireContext(), object : TimePickerDialog.OnTimeSetListener {
                        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                            tvTimeBegin.setText("%02d:%02d".format(hourOfDay, minute), false)
                        }
                    }, 0, 0, true).show()
                }
            }

        })
    }

    override fun setEndTimes(value: List<String>) {
        val diffTime = resources.getString(R.string.diff_time)
        val list = value.toMutableList().apply { add(diffTime) }

        setAdapter(tvTimeEnd, list, object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent ?: return

                if (position == list.size - 1) {
                    TimePickerDialog(requireContext(), object : TimePickerDialog.OnTimeSetListener {
                        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                            tvTimeEnd.setText("%02d:%02d".format(hourOfDay, minute), false)
                        }
                    }, 0, 0, true).show()
                }
            }

        })
    }

    override fun setRepeatValues(value: List<String>) {
        setAdapter(tvRepeat, value)
    }

    override fun setCategories(value: List<SportSectionModel>) {
        courses = value
        setAdapter(tvCategory, value.mapNotNull { v -> v.name })
        scheduleItem?.course?.let {
            courses.firstOrNull { v -> v.id == it }?.let {
                tvCategory.setText(it.name)
            }
        }
    }

    override fun setTypesOfScheduler(value: ArrayList<SchedulerType>) {
        schedulerTypes = value
        val adapter = SchedulerTypeAdapter(requireContext(), value)
        tvSchedulerType.setAdapter(adapter)
    }

    override fun setLinkAdapter(adapter: LinksAdapter) {
        rvLink.adapter = adapter
    }

    override fun back() {
        navigationPresenter.popFragment()
    }

    override fun setSchedulerName(value: String?) {
        tvCourse.setText(value)
    }

    override fun setAuitoryName(value: String?) {
        tvAuditory.setText(value)
    }

    override fun setTeacherName(value: String?) {
        tvLector.setText(value)
    }

    override fun showCallbackMessage(message: String, callback: (() -> Unit)?) {
        AlertDialog.Builder(requireContext())
            .setOnCancelListener {
                callback?.invoke()
            }.setPositiveButton("OK") {_, _ ->
                callback?.invoke()
            }.setMessage(message)
            .create()
            .show()
    }

    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    override fun sendSchedulerEditedBroadcast(newId: Int?) {
        LocalBroadcastManager
            .getInstance(requireContext())
            .sendBroadcast(Intent(ACTION_SCHEDULER_EDITED).apply {
                putExtra(ARG_KEY_NEW_ID, newId)
            })
    }


    //  OTHERS
    private fun <T>setAdapter(
        forView: AutoCompleteTextView,
        withValues: List<T>,
        listener: AdapterView.OnItemClickListener? = null
    ) {
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, withValues)
        forView.setAdapter(adapter)
        listener?.let { forView.onItemClickListener = it }

    }

    private fun buildScheduler(): ScheduleItem? {
        var errorCount = 0

        val dayPosition = resources.getStringArray(R.array.weeks).indexOf(tvDayOfWeek.text.toString()) + 1
        if (dayPosition == 0) {
            wrapDayOfWeek.error = "Заполните поле"
            ++errorCount
        }

        val teacherNameValue = tvLector.text.toString()
        val teacher = TeachersRepository.shared.realm
            .where(Teacher::class.java)
            .equalTo("name", teacherNameValue)
            .findFirst()

        if ((teacher == null || teacherNameValue.isNullOrEmpty()) && false) {
            wrapLector.error = "Заполните поле"
            ++errorCount
        }

        val courseUserValue = tvCourse.text.toString()
        if (courseUserValue.isNullOrEmpty()) {
            wrapCourse.error = "Заполните поле"
            ++errorCount
        }

        if (tvCategory.text.toString().isNullOrEmpty() && currType == Type.Create) {
            wrapCategory.error = "Заполните поле"
            ++errorCount
        }

        if (tvAuditory.text.toString().isNullOrEmpty() && false) {
            wrapAuditory.error = "Заполните поле"
            ++errorCount
        }

        val timeBegin = tvTimeBegin.text.toString()
        val timeEnd = tvTimeEnd.text.toString()
        val slots = TimeSlotsRepository.shared.slots.value

        if (timeBegin.isNullOrEmpty()) {
            wrapTimeBegin.error = "Заполните поле"
            ++errorCount
        }

        if (timeEnd.isNullOrEmpty()) {
            wrapTimeEnd.error = "Заполните поле"
            ++errorCount
        }

        var repeatIndex = 0
        if ((tvRepeat.text.toString().isNullOrEmpty()) && false) {
            wrapRepeat.error = "Заполните поле"
            ++errorCount
        } else {
            repeatIndex = resources.getStringArray(R.array.scheduler_repeat).indexOf(tvRepeat.text.toString())
        }

        if ((tvSchedulerType.text.toString().isNullOrEmpty()) && false) {
            wrapSchedulerType.error = "Заполните поле"
            ++errorCount
        }



        if (errorCount > 0) return null

        val teaherList = if (teacher == null)
            arrayListOf()
        else arrayListOf(TeacherModel(teacher.id?.toInt() ?: 0, ""))

        return ScheduleItem(
            name = tvCourse.text.toString(),
            prof = tvLector.text.toString(),
            place = tvAuditory.text.toString(),
            day = dayPosition,
            type = schedulerTypes.firstOrNull{v -> v.title == tvSchedulerType.text.toString()}?.type?.toUpperCase()
                ?: tvSchedulerType.text.toString(),
            startTime = tvTimeBegin.text.toString(),
            endTime = tvTimeEnd.text.toString(),
            teachers = teaherList
        ).apply {
            schedulerId?.let { id -> this.id = id }
            evenodd = repeatIndex
            course = courses.firstOrNull { v -> v.name == tvCategory.text.toString() }?.id


            tvAuditory.text.toString().let {
                if (SchedulePlaceRepository.shared.places.hasValue()) {
                    SchedulePlaceRepository.shared.places.value.firstOrNull { v -> v.name == it }?.let { placeModel ->
                        auditorium = listOf(placeModel)
                    }
                }
            }
        }
    }

    private val broadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    AuditoryListFragment.ACTION -> {
                        val id = intent.getStringExtra(AuditoryListFragment.DATA_ID) ?: return
                        presenter.setAuditoryById(id)
                    }
                    TeachersListFragment.ACTION -> {
                        if (intent.hasExtra(TeachersListFragment.DATA_ID)) {
                            val id = intent.getStringExtra(TeachersListFragment.DATA_ID) ?: return
                            presenter.setTeacherById(id)
                        } else if (intent.hasExtra(TeachersListFragment.DATA_FIELD)) {
                            val name = intent.getStringExtra(TeachersListFragment.DATA_FIELD) ?: return
                            presenter.setTeacherByName(name)
                        }
                    }
                }
            }
        }
    }


    class SchedulerTypeAdapter(context: Context, objects: List<SchedulerType>): ArrayAdapter<SchedulerType>(context, R.layout.list_item_markered, R.id.tvValue, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)

            SchedulersRepository.shared.getScheduler()

            val colorFactory = UserRepository.shared.createSchedulersColorFactory()
            getItem(position)?.let { item ->
                val color = colorFactory.getColorByType(item.type)
                val viewMarker = view.findViewById<View>(R.id.viewMarker)
                viewMarker.backgroundTintList = ColorStateList.valueOf(color)
            }

            return view
        }
    }




    companion object {
        const val ACTION_SCHEDULER_EDITED = "scheduler_edit_fragment.action.ACTION_SCHEDULER_EDITED"
        const val ARG_KEY_NEW_ID = "scheduler_edit_fragment.arg_key.new_id"
        const val ARG_KEY_ID = "scheduler_edit_fragment.arg_key.id"
        const val ARG_KEY_TYPE = "scheduler_edit_fragment.arg_key.type"
        const val ARG_KEY_FOR_GROUP = "scheduler_edit_fragment.arg_key.for_group"

        fun newInstance(id: Int? = null, forGroup: Boolean, type: Type): BaseFragment {
            val fragment = SchedulerEditFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_KEY_TYPE, type.typeValue)

            id?.let {
                bundle.putInt(ARG_KEY_ID, id)
                bundle.putBoolean(ARG_KEY_FOR_GROUP, forGroup)
            }

            fragment.arguments = bundle

            return fragment
        }
    }

    enum class Type(val typeValue: Int) {
        Edit(0),
        Create(1);

        companion object {
            fun getByTypeValue(typeValue: Int): Type? {
                return when (typeValue) {
                    0 -> Type.Edit
                    1 -> Type.Create
                    else -> null
                }
            }
        }
    }
}