package dev.phystech.mipt.ui.fragments.study

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.*
import dev.phystech.mipt.ui.fragments.navigator.place_detail.PlaceDetailFragment
import dev.phystech.mipt.ui.fragments.study.deadlines.DeadlinesFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_list.SchedulerListFragment
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment
import dev.phystech.mipt.ui.fragments.users_list.UsersListDataResource
import dev.phystech.mipt.ui.fragments.users_list.UsersListFragment
import dev.phystech.mipt.ui.utils.SchedulerDetailScreenMode
import dev.phystech.mipt.ui.utils.SchedulerDetailScreenMode.*
import dev.phystech.mipt.utils.isSuccess
import dev.phystech.mipt.utils.visibility
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

/** Детальный просмотр занятия
 * Фрагмент поддерживает 2 режима:
 * * Просмотр занятия [ScheduleItemDetailFragment] (model, [SchedulerDetailScreenMode.View])
 * * Добавление занятие в рассписание [ScheduleItemDetailFragment] (model, [SchedulerDetailScreenMode.Add])
 *
 * Поле: scheduleIdToRemove используется в режиме добавления. Если оно не null, то при успешном
 * добавлении занятия, произойдет отписка от занятия, id которого будет равно scheduleIdToRemove
 */
class ScheduleItemDetailFragment(
    private var scheduleItem: ScheduleItem,
    private val screenMode: SchedulerDetailScreenMode = SchedulerDetailScreenMode.View,
    private val scheduleIdToRemove: Int? = null
) : BaseFragment() {

    private lateinit var tvDateTime: TextView
    private lateinit var tvRoom: TextView
    private lateinit var tvSchedulerName: TextView
    private lateinit var ivEdit: ImageView
    private lateinit var tvTypeLesson: TextView
    private lateinit var etPersonalNotes: EditText
    private lateinit var ivBack: ImageView
    private lateinit var tvLectors: TextView
    private lateinit var viewMarker: View
    private lateinit var rlShare: RelativeLayout

    private lateinit var llPersonalFields: LinearLayout
    private lateinit var llPersonalNotes: LinearLayout
    private lateinit var tvActionTitle: TextView
    private lateinit var tvTitle: TextView

    private lateinit var tvDescriptionCourse: TextView
    private lateinit var tvDescriptionDeadlines: TextView
    private lateinit var tvDescriptionUsers: TextView
    private lateinit var tvDescriptionBroadcast: TextView


    private lateinit var llCourse: LinearLayout
    private lateinit var llDeadlines: LinearLayout
    private lateinit var llUsers: LinearLayout
    private lateinit var llBroadcast: LinearLayout

    private lateinit var llFieldPlace: LinearLayout
    private lateinit var llFieldTeacher: LinearLayout

    private lateinit var tvSource: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("TAGGG", "onCreate")
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(schedulerEditedReceiver, IntentFilter(SchedulerEditFragment.ACTION_SCHEDULER_EDITED))

        return inflater.inflate(R.layout.fragment_schedule_item_detail, container, false)
    }

    override fun onResume() {
        super.onResume()
        navigationPresenter.setStatusBarTransparency(false)
    }

    override fun onPause() {
        super.onPause()
        SchedulerNotesRepository.shared.set(scheduleItem.id, etPersonalNotes.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(schedulerEditedReceiver)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        tvDateTime = view.findViewById(R.id.tvDateTime)
        tvRoom = view.findViewById(R.id.tvRoom)
        ivEdit = view.findViewById(R.id.ivEdit)
        tvTypeLesson = view.findViewById(R.id.tvTypeLesson)
        etPersonalNotes = view.findViewById(R.id.etPersonalNotes)
        ivBack = view.findViewById(R.id.ivBack)
        tvSchedulerName = view.findViewById(R.id.tvSchedulerName)
        tvLectors = view.findViewById(R.id.tvLectors)
        viewMarker = view.findViewById(R.id.viewMarker)
        rlShare = view.findViewById(R.id.rlShare)
        llPersonalFields = view.findViewById(R.id.llPersonalFields)
        llPersonalNotes = view.findViewById(R.id.llPersonalNotes)
        tvActionTitle = view.findViewById(R.id.tvActionTitle)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDescriptionCourse = view.findViewById(R.id.tvDescriptionCourse)
        tvDescriptionDeadlines = view.findViewById(R.id.tvDescriptionDeadlines)
        tvDescriptionUsers = view.findViewById(R.id.tvDescriptionUsers)
        tvDescriptionBroadcast = view.findViewById(R.id.tvDescriptionBroadcast)
        llCourse = view.findViewById(R.id.llCourse)
        llDeadlines = view.findViewById(R.id.llDeadlines)
        llUsers = view.findViewById(R.id.llUsers)
        llBroadcast = view.findViewById(R.id.llBroadcast)

        llFieldPlace = view.findViewById(R.id.llFieldPlace)
        llFieldTeacher = view.findViewById(R.id.llFieldTeacher)
        tvSource = view.findViewById(R.id.tvSource)

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

        ivEdit.setOnClickListener {
            val alertView = layoutInflater.inflate(R.layout.alert_edit_confirm, null)
            val btnEditForSelf = alertView.findViewById<View>(R.id.btnEditForSelf)
            val btnEditForGroup = alertView.findViewById<View>(R.id.btnEditForGroup)
            val btnChage = alertView.findViewById<View>(R.id.btnChage)
            val btnDelete = alertView.findViewById<View>(R.id.btnDelete)

            val alert = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(alertView)
                .create()

            btnDelete.setOnClickListener {
                showProgress()
                SchedulersRepository.shared.delete(scheduleItem.id) {
                    hideProgress()
                    if (it) {
                        navigationPresenter.popFragment()
                    } else {
                        showMessage(R.string.message_some_error_try_late)
                    }
                }
                alert.cancel()
            }
            btnChage.setOnClickListener {
                val fragment = SchedulerListFragment.newInstance(
                    course = scheduleItem.course,
                    toRemoveSchedulerId = scheduleItem.id
                )
                navigationPresenter.pushFragment(fragment, true)
                alert.cancel()
            }
            btnEditForSelf.setOnClickListener {
                val fragment = SchedulerEditFragment.newInstance(
                    scheduleItem.id,
                    false,
                    SchedulerEditFragment.Type.Edit
                )
                navigationPresenter.pushFragment(fragment, true)
                alert.cancel()
            }
            if (scheduleItem.canEdit || true) {
                btnEditForGroup.setOnClickListener {
                    val fragment = SchedulerEditFragment.newInstance(
                        scheduleItem.id,
                        true,
                        SchedulerEditFragment.Type.Edit
                    )
                    navigationPresenter.pushFragment(fragment, true)
                    alert.cancel()
                }
            } else {
                btnEditForGroup.alpha = 0.5f
            }

            alert.show()

        }

        rlShare.setOnClickListener {
            when (screenMode) {
                Add -> {
                    val secret = scheduleItem.urls.findSecret() ?: return@setOnClickListener
                    val id = scheduleItem.id.toString()

                    showProgress()

                    fun invokeSubscribe() {
                        ApiClient.shared.schedulerSubscribe(id, secret)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                hideProgress()
                                if (it.status.isSuccess) {
                                    navigationPresenter.showStudy()
                                    SchedulersRepository.shared.loadData()
                                } else {
                                    showMessage(R.string.message_some_error_try_late)
                                }
                            }, {
                                hideProgress()
                                showMessage(R.string.message_some_error_try_late)
                            })
                    }

                    if (scheduleIdToRemove != null) {
                        SchedulersRepository.shared.delete(scheduleIdToRemove) {
                            invokeSubscribe()
                        }
                    } else {
                        invokeSubscribe()
                    }
                }

                SchedulerDetailScreenMode.View -> {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        NetworkUtils.SERVER_ADDRESS + scheduleItem.urls.share
                    )

                    startActivity(shareIntent)
                }

            }
        }

        if (SchedulePlaceRepository.shared.places.hasValue()) {
            scheduleItem.auditorium.firstOrNull()?.id?.let { id ->
                llFieldPlace.setOnClickListener {
                    SchedulePlaceRepository.shared.getById(id) {
                        it?.building?.id.let { schedulerModelID ->
                            PlacesRepository.shared.getById(schedulerModelID.toString()) {
                                if (it == null) return@getById

                                val fragment = PlaceDetailFragment(it)
                                navigationPresenter.pushFragment(fragment, true)
                            }
                        }
                    }
                }
            }
        }

        llFieldTeacher.setOnClickListener {
            scheduleItem.teachers?.firstOrNull()?.let { teacherModel ->
                val teacherDetailFragment = TeacherDetailFragment.newInstance(teacherModel.id, teacherModel.name)
                navigationPresenter.pushFragment(teacherDetailFragment, true)
            }
        }

        llCourse.setOnClickListener {
            scheduleItem.urls.course?.let { urlStr ->
                activity?.packageManager?.let { pm ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
                    if (intent.resolveActivity(pm) != null) {
                        startActivity(intent)
                    }
                }
            }
        }

        llBroadcast.setOnClickListener {
            scheduleItem.urls.broadcast?.let { urlStr ->
                activity?.packageManager?.let { pm ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
                    if (intent.resolveActivity(pm) != null) {
                        startActivity(intent)
                    }
                }
            }
        }

        llDeadlines.setOnClickListener {
            val fragment = DeadlinesFragment.newInstance(scheduleItem.id)
            navigationPresenter.pushFragment(fragment, true)
        }

        llUsers.setOnClickListener {
            val userListFragment =
                UsersListFragment.newInstance(UsersListDataResource.Schedule, scheduleItem.id)
            navigationPresenter.pushFragment(userListFragment, true)
        }

        setContent()
    }

    override fun getBackgroundColor(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.color_pressed_button, null)
        } else {
            resources.getColor(R.color.color_pressed_button)
        }
    }

    private fun setContent() {
        llPersonalFields.visibility = (screenMode != Add).visibility()
        llPersonalNotes.visibility = (screenMode != Add).visibility()
        ivEdit.visibility = (screenMode != Add).visibility()

        if (screenMode == Add) {
            tvActionTitle.setText(R.string.add)
            tvTitle.setText(R.string.add_scheduler)
        }

        rlShare.visibility = scheduleItem.urls.share
            .isNullOrEmpty().not()
            .or(screenMode == Add)
            .visibility()

        val oddValue = resources.getStringArray(R.array.scheduler_repeat)
            .getOrNull(scheduleItem.evenodd)
            ?.toLowerCase(Locale.getDefault())
            ?.run { "($this)" } ?: String.empty()

//        serverDateFormat.parse(scheduleItem.startTime)?.let {
//            val even = SchedulersRepository.shared.getDateOdd(it)
//            oddValue = resources.getString(if (even == 0) R.string.odd else R.string.even)
//            oddValue = "($oddValue)"
//        }

        if (scheduleItem.allDay) {
            resources.getStringArray(R.array.weeks).getOrNull(scheduleItem.day - 1)?.let {
                tvDateTime.text = it
            }
//            serverDateFormat.parse(scheduleItem.startTime)?.let { date ->
//                tvDateTime.text = allDayDateFormat.format(date)
//            }
        } else {
            tvDateTime.text =
                "${dateField()}, ${scheduleItem.startTime} - ${scheduleItem.endTime} $oddValue"
        }


        tvSchedulerName.text = scheduleItem.name
        val factory = UserRepository.shared.createSchedulersColorFactory()
        val color = factory.getColorByType(scheduleItem.type)
        viewMarker.backgroundTintList = ColorStateList.valueOf(color)

        ScheduleEventRepository.shared.getForCourse(scheduleItem.course.toString()) {
            tvDescriptionDeadlines.text = it.size.toString()
        }

        tvDescriptionUsers.text = scheduleItem.users.size.toString()
        tvDescriptionCourse.text = getUrlTitle(scheduleItem.urls.course ?: "")
        tvDescriptionBroadcast.text = getUrlTitle(scheduleItem.urls.broadcast)

        llCourse.visibility = tvDescriptionCourse.text.isNullOrEmpty().not().visibility()
        llBroadcast.visibility = tvDescriptionBroadcast.text.isNullOrEmpty().not().visibility()

        val teacherNameValue = if (scheduleItem.teachers.isNullOrEmpty()) {
            scheduleItem.prof
        } else {
            scheduleItem.teachers?.joinToString(separator = "\n") { v -> v.name }
        }

        tvLectors.text = teacherNameValue
        llFieldTeacher.visibility = teacherNameValue.isNullOrEmpty().not().visibility()

        etPersonalNotes.setText(SchedulerNotesRepository.shared.get(scheduleItem.id))

        context?.let {
            tvTypeLesson.text = typeName(it, scheduleItem.type)
        }
        tvRoom.text = scheduleItem.place
        llFieldPlace.visibility = tvRoom.text.isNotEmpty().visibility()

        if (scheduleItem.updater != null && scheduleItem.origin == "user") {
            tvSource.text =
                resources.getString(R.string.changed_for_user, scheduleItem.updater?.name)
        } else {
            tvSource.text = resources.getString(R.string.load_from_lms)
        }

    }


    private fun dateField(): String {
        return when (scheduleItem.day) {
            1 -> Application.context.resources.getString(R.string.Monday)
            2 -> Application.context.resources.getString(R.string.Tuesday)
            3 -> Application.context.resources.getString(R.string.Wednesday)
            4 -> Application.context.resources.getString(R.string.Thursday)
            5 -> Application.context.resources.getString(R.string.Friday)
            6 -> Application.context.resources.getString(R.string.Saturday)
            7 -> Application.context.resources.getString(R.string.Sunday)
            else -> String.empty()
        }
    }

    private fun typeName(context: Context, type: String): String {
        return when (type) {
            "LAB" -> context.resources.getString(R.string.schedule_item_lab)
            "SEM" -> context.resources.getString(R.string.schedule_item_sem)
            "LEC" -> context.resources.getString(R.string.schedule_item_lec)
            else -> String.empty()
        }
    }


    //  OTHERS
    private fun getUrlTitle(urlStr: String?): String? {
        val url = Uri.parse(urlStr)
        return url.host
    }

    val schedulerEditedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getIntExtra(SchedulerEditFragment.ARG_KEY_NEW_ID, scheduleItem.id)?.let { id ->
                SchedulersRepository.shared.getSchedulerItemById(id) {
                    it?.let {
                        this@ScheduleItemDetailFragment.scheduleItem = it
                        setContent()
                    }
                }
            }

        }
    }

    companion object {
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val allDayDateFormat = SimpleDateFormat("E, dd MMM")
    }

}
