package dev.phystech.mipt.ui.fragments.study.scheduler_event_detail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.repositories.SchedulePlaceRepository
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.fragments.study.events.SchedulerEventType
import dev.phystech.mipt.utils.dpToPx
import dev.phystech.mipt.utils.visibility
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil
import java.text.DateFormat
import java.text.SimpleDateFormat

/** Детальный просмотр события
 *
 * Корректно создавать экземпляр нужно через метод [SchedulerEventDetailFragment.newInstance]
 * @param id id модели (будет браться с [ScheduleEventRepository])
 */
class SchedulerEventDetailFragment: BaseFragment(), SchedulerEventDetailContract.View {

    lateinit var tvType: TextView
    lateinit var viewMarker: View
    lateinit var tvSchedulerName: TextView
    lateinit var tvDateTime: TextView
    lateinit var llFieldPlace: LinearLayout
    lateinit var tvRoom: TextView
    lateinit var llFieldTeacher: LinearLayout
    lateinit var tvLectors: TextView
    lateinit var llTranslation: LinearLayout
    lateinit var tvTranslation: TextView
    lateinit var etPersonalNotes: EditText
    lateinit var rlShare: RelativeLayout
    lateinit var ivEdit: ImageView
    lateinit var ivBack: ImageView
    lateinit var tvSource: TextView
    lateinit var tvFloor: TextView

    lateinit var llRecords: LinearLayout
    lateinit var llMaterials: LinearLayout
    lateinit var llCourse: LinearLayout
    lateinit var tvDescriptionTranslation: TextView
    lateinit var tvDescriptionRecords: TextView
    lateinit var tvDescriptionMaterials: TextView
    lateinit var tvDescriptionCourse: TextView

    lateinit var llPersonalNotes: LinearLayout
    lateinit var llAdditionFields: LinearLayout
    lateinit var tvShare: TextView


    lateinit var llUsers: LinearLayout
    lateinit var tvDescriptionUsers: TextView

    private var eventId: Int = -1
    private var model: EventModel? = null

    private lateinit var presenter: SchedulerEventDetailContract.Presenter


    //  ANDROID LIFE CIRCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventId = arguments?.getInt(KEY_ID) ?: eventId

        presenter = SchedulerEventDetailPresenter(eventId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        R.layout.fragment_deadlines_list
        val view = inflater.inflate(R.layout.fragment_schedule_event_detail, container, false)

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


    //  BASE VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        //  binding view
        tvType = view.findViewById(R.id.tvType)
        viewMarker = view.findViewById(R.id.viewMarker)
        tvSchedulerName = view.findViewById(R.id.tvSchedulerName)
        tvDateTime = view.findViewById(R.id.tvDateTime)
        llFieldPlace = view.findViewById(R.id.llFieldPlace)
        tvRoom = view.findViewById(R.id.tvRoom)
        llFieldTeacher = view.findViewById(R.id.llFieldTeacher)
        tvLectors = view.findViewById(R.id.tvLectors)
        llTranslation = view.findViewById(R.id.llTranslation)
        tvTranslation = view.findViewById(R.id.tvTranslation)
        etPersonalNotes = view.findViewById(R.id.etPersonalNotes)
        rlShare = view.findViewById(R.id.rlShare)
        ivEdit = view.findViewById(R.id.ivEdit)
        ivBack = view.findViewById(R.id.ivBack)
        tvSource = view.findViewById(R.id.tvSource)
        tvFloor = view.findViewById(R.id.tvFloor)

        llRecords = view.findViewById(R.id.llRecords)
        llMaterials = view.findViewById(R.id.llMaterials)
        llCourse = view.findViewById(R.id.llCourse)
        tvDescriptionTranslation = view.findViewById(R.id.tvDescriptionTranslation)
        tvDescriptionRecords = view.findViewById(R.id.tvDescriptionRecords)
        tvDescriptionMaterials = view.findViewById(R.id.tvDescriptionMaterials)
        tvDescriptionCourse = view.findViewById(R.id.tvDescriptionCourse)
        llUsers = view.findViewById(R.id.llUsers)
        tvDescriptionUsers = view.findViewById(R.id.tvDescriptionUsers)
        llPersonalNotes = view.findViewById(R.id.llPersonalNotes)
        llAdditionFields = view.findViewById(R.id.llAdditionFields)
        tvShare = view.findViewById(R.id.tvShare)

        //  event attaching
        llFieldPlace.setOnClickListener(this::auditoryClick)
        llFieldTeacher.setOnClickListener(this::teacherClick)
        rlShare.setOnClickListener(this::shareClick)
        llTranslation.setOnClickListener(this::translationClick)
        ivEdit.setOnClickListener(this::editClick)
        ivBack.setOnClickListener(this::back)
        llTranslation.setOnClickListener(this::clickTranslation)
        llRecords.setOnClickListener(this::clickRecords)
        llMaterials.setOnClickListener(this::clickMaterials)
        llCourse.setOnClickListener(this::clickCourse)
        llUsers.setOnClickListener(this::clickUsers)
        etPersonalNotes.addTextChangedListener(noteTextWatcher)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  MVP VIEW
    override fun setContent(model: EventModel) {
        this.model = model
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val dateStart = dateFormat.parse(model.startTime ?: "")
        val dateEnd = dateFormat.parse(model.endTime ?: "")

        val auditory = model.auditorium.firstOrNull()
        val teacher = model.teachers.firstOrNull()

        tvSchedulerName.text = model.name
        if (model.allDay) {
            dateStart?.let {
                tvDateTime.text = allDayDateFormat.format(it).capitalize()
            }
        } else {
            tvDateTime.text = "${displayDateFormat.format(dateStart)} - ${displayDateFormat.format(dateEnd)}"
        }

        auditory?.let {
            tvRoom.text = it.name
        }

        teacher?.name?.let {
            tvLectors.text = it
        }

        llFieldPlace.visibility = (auditory?.name?.isNullOrEmpty() == false).visibility()
        llFieldTeacher.visibility = (teacher?.name?.isNullOrEmpty() == false).visibility()

        llTranslation.visibility = model.urls?.broadcast.isNullOrEmpty().not().visibility()
        llRecords.visibility = model.urls?.records.isNullOrEmpty().not().visibility()
        llMaterials.visibility = model.urls?.materials.isNullOrEmpty().not().visibility()
        llCourse.visibility = model.urls?.course.isNullOrEmpty().not().visibility()

        tvDescriptionTranslation.text = getUrlTitle(model.urls?.broadcast)
        tvDescriptionRecords.text = getUrlTitle(model.urls?.records)
        tvDescriptionMaterials.text = getUrlTitle(model.urls?.materials)
        tvDescriptionCourse.text = getUrlTitle(model.urls?.course)
        tvDescriptionUsers.text = model.users.size.toString()

        etPersonalNotes.setText(model.notes)

        rlShare.visibility = model.urls?.share.isNullOrEmpty().not().visibility()

        model.type.let { type ->
            val strRes = SchedulerEventType.getByType(type)?.getDysplayedResId() ?: return@let
            tvType.text = resources.getString(strRes)
        }

        SchedulePlaceRepository.shared.getById(model.auditorium.firstOrNull()?.id ?: "") {
            if (it?.floor != null) {
                tvFloor.text = resources.getString(R.string.floor_value, it.floor)
            }
        }


        if (model.updater != null && model.origin == "user") {
            tvSource.text = resources.getString(R.string.changed_for_user, model.updater?.name)
        } else {
            tvSource.text = resources.getString(R.string.load_from_lms)
        }

        //  Marker
        val factory = UserRepository.shared.createSchedulersColorFactory()
        val dr = ContextCompat.getDrawable(requireContext(), ColorUtil.getBackgroundDrawable(model.type)) as GradientDrawable
        dr.setStroke(2.dpToPx(), ColorStateList.valueOf(factory.getColorByType(model.type)))
        dr.setColor(android.R.color.white)
        viewMarker.background = dr

        if (ScheduleEventRepository.shared.containsInMyEvents(model.id.toString()).not()) {
            llPersonalNotes.visibility = false.visibility()
            llAdditionFields.visibility = false.visibility()
            tvShare.setText(R.string.add)
            ivEdit.visibility = false.visibility()
        }
    }

    override fun showEditAlert(canEdit: Boolean) {
        val alertView = layoutInflater.inflate(R.layout.alert_edit_confirm, null)
        val btnEditForSelf = alertView.findViewById<View>(R.id.btnEditForSelf)
        val btnEditForGroup = alertView.findViewById<View>(R.id.btnEditForGroup)
        val btnChage = alertView.findViewById<View>(R.id.btnChage)
        val btnDelete = alertView.findViewById<View>(R.id.btnDelete)

        val alert = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(alertView)
            .create()

        btnDelete.setOnClickListener {
            presenter.confirmDelete()
            alert.cancel()
        }
        btnChage.visibility = false.visibility()

        btnEditForSelf.setOnClickListener {
            presenter.confirmEditPersonal()
            alert.cancel()
        }

        if (canEdit || true) {
            btnEditForGroup.setOnClickListener {
                presenter.confirmEditCommon()
                alert.cancel()
            }
        } else {
            btnEditForGroup.alpha = 0.5f
        }

        alert.show()
    }

    override fun sendLink(link: String) {
        val linkIntent = Intent(Intent.ACTION_SEND)
        linkIntent.putExtra(Intent.EXTRA_TEXT, link)
        linkIntent.type = "text/plain"

        activity?.packageManager?.let {
            if (linkIntent.resolveActivity(it) != null) {
                startActivity(linkIntent)
            }
        }

    }

    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    override fun back() {
        navigationPresenter.popFragment()
    }

    override fun toNews() {
        navigationPresenter.showNews()
    }

    override fun showStudy() {
        navigationPresenter.showStudy()
    }


    //  EVENTS
    private fun teacherClick(view: View) {
        presenter.lectorPressed()
    }

    private fun auditoryClick(view: View) {
        presenter.locationPressed()
    }

    private fun translationClick(view: View) {
        presenter.translationPressed()
    }

    private fun shareClick(view: View) {
        presenter.sharePressed()
    }

    private fun editClick(view: View) {
        presenter.editPressed()
    }

    private fun back(view: View) {
        navigationPresenter.popFragment()
    }

    private fun clickTranslation(view: View) {
        model?.urls?.broadcast?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            activity?.packageManager?.let {
                if (intent.resolveActivity(it) != null) {
                    startActivity(intent)
                }
            }
        }
    }

    private fun clickRecords(view: View) {
        model?.urls?.records?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            activity?.packageManager?.let {
                if (intent.resolveActivity(it) != null) {
                    startActivity(intent)
                }
            }
        }
    }

    private fun clickMaterials(view: View) {
        model?.urls?.materials?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            activity?.packageManager?.let {
                if (intent.resolveActivity(it) != null) {
                    startActivity(intent)
                }
            }
        }
    }

    private fun clickCourse(view: View) {
        model?.urls?.course?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            activity?.packageManager?.let {
                if (intent.resolveActivity(it) != null) {
                    startActivity(intent)
                }
            }
        }
    }

    private fun clickUsers(view: View) {
        presenter.usersClick()
    }



    private val noteTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            presenter.updateNote(s.toString())
        }
    }


    //  OTHERS
    private fun getUrlTitle(urlStr: String?): String? {
        val url = Uri.parse(urlStr)
        return url.host
    }


    companion object {

        private val allDayDateFormat = SimpleDateFormat("EEEE, dd MMMM")
        private val displayDateFormat: DateFormat = SimpleDateFormat("HH:mm")
        const val KEY_ID = "event_detail.id"

        fun newInstance(id: Int): SchedulerEventDetailFragment {
            val fragment = SchedulerEventDetailFragment()
            val arguments = Bundle()
            arguments.putInt(KEY_ID, id)

            fragment.arguments = arguments
            return fragment
        }
    }

}