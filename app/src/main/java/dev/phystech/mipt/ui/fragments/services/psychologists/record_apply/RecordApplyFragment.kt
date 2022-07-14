package dev.phystech.mipt.ui.fragments.services.psychologists.record_apply

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.api.PsyTimeResponseModel
import dev.phystech.mipt.models.api.UsersResponseModel
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable

import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import br.com.sapereaude.maskedEditText.MaskedEditText
import com.google.android.material.snackbar.Snackbar
import dev.phystech.mipt.Application
import dev.phystech.mipt.adapters.ChatsGroupsAdapter
import dev.phystech.mipt.models.*
import dev.phystech.mipt.repositories.ScheduleEventRepository
import io.realm.RealmList

class RecordApplyFragment : BaseFragment(), RecordApplyContract.View {

    private var presenter: RecordApplyContract.Presenter? = null

    lateinit var ivBack: ImageView
    lateinit var rlPsyFIO: RelativeLayout
    lateinit var tvAppointmentPsychologistDate: TextView
    lateinit var tvAppointmentPsychologistTime: TextView
    lateinit var tvPsyAddress: TextView
    lateinit var tvPsyFIO: TextView
    lateinit var etPhone: MaskedEditText
    lateinit var tvApply:TextView
    lateinit var llApply: LinearLayout

    private var psychologist: PsyTimeResponseModel.PsyInfoModel? = null
    private var dayOfWeek: Int? = null

    private var textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (etPhone.getRawText() != null && etPhone.getRawText().toString().length >= 10) {
                llApply.isEnabled = true
                llApply.isClickable = true
                tvApply.setTextColor(ContextCompat.getColor(Application.context, R.color.colorPrimary))
                llApply.background = ContextCompat.getDrawable(Application.context, R.drawable.bg_button_cornerred_2_enable)

            } else {
                llApply.isEnabled = false
                llApply.isClickable = false
                tvApply.setTextColor(ContextCompat.getColor(Application.context, R.color.secondary_text))
                llApply.background = ContextCompat.getDrawable(Application.context, R.drawable.bg_button_cornerred_2_disable)
            }
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_psychologist_record_apply, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = RecordApplyPresenter(resources, bottomSheetController)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }

    //  MVP VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        rlPsyFIO = view.findViewById(R.id.rlPsyFIO)
        tvAppointmentPsychologistDate = view.findViewById(R.id.tvAppointmentPsychologistDate)
        tvAppointmentPsychologistTime = view.findViewById(R.id.tvAppointmentPsychologistTime)
        tvPsyAddress = view.findViewById(R.id.tvPsyMiptAddress)
        tvPsyFIO = view.findViewById(R.id.tvPsyFIO)
        etPhone = view.findViewById(R.id.etPhone)
        tvApply = view.findViewById(R.id.tvApply)
        llApply = view.findViewById(R.id.llApply)

        llApply.isEnabled = false
        llApply.isClickable = false

        //  EVENTS

        ivBack.setOnClickListener(this::onBack)
        rlPsyFIO.setOnClickListener(this::openPsychologistInfo)
        llApply.setOnClickListener(this::onApply)

        etPhone.addTextChangedListener(textWatcher)

        setContent()
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  OTHERS

    fun onBack(view: View) {
        navigationPresenter.popFragment()
    }

    fun openPsychologistInfo(view: View) {
        psychologist?.let { psychologist ->
            if (!psychologist.user_psy?.id.isNullOrEmpty() && !psychologist.user_psy?.name.isNullOrEmpty()) {

                val userData: Teacher = Teacher()
                userData.id = psychologist.user_psy?.id
                userData.name = psychologist.user_psy?.name
                userData.post = psychologist.user_psy?.post
                userData.image = psychologist.user_psy?.image
                userData.socialUrl = psychologist.user_psy?.socialUrl
                userData.miptUrl = psychologist.user_psy?.miptUrl
                userData.wikiUrl = psychologist.user_psy?.wikiUrl

                val teacherFragment = TeacherDetailFragment.newInstance(psychologist.user_psy?.id!!.toInt(), psychologist.user_psy?.name, userModel = userData, roles = psychologist.user_psy?.roles, lastactive = psychologist.user_psy?.lastactive)
                navigate(teacherFragment)
            }
        }
    }

    fun onApply(view: View) {
        psychologist?.id?.let { id ->
            val text = "7${etPhone.getRawText().toString()}"
            presenter?.recordApply(id, text)
        }
    }

    fun setContent() {
        psychologist?.let { psychologist ->

            val weekday = resources.getStringArray(R.array.weeks_en)
            val dateFormatTime = SimpleDateFormat("HH:mm")
            var startTimeStr = "";
            var endTimeStr = "";

            psychologist.startTime?.getFormatDate()?.let { startTime ->
                val dateFormat = SimpleDateFormat("dd MMMM")
                val date = dateFormat.format(startTime.time)

                val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone(psychologist.startTime?.timezone))
                calendar.time = startTime
                val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
                this.dayOfWeek = if (dayOfWeek == 1) 7 else dayOfWeek-1

                tvAppointmentPsychologistDate.text = "${weekday.get(dayOfWeek-1)}, $date"
                tvAppointmentPsychologistDate.visibility = View.VISIBLE

                startTimeStr = dateFormatTime.format(startTime)
            }

            psychologist.endTime?.getFormatDate()?.let { endTime ->
                endTimeStr = dateFormatTime.format(endTime)
            }

            if (startTimeStr.isNotEmpty() && endTimeStr.isNotEmpty()) {
                tvAppointmentPsychologistTime.text = "$startTimeStr - $endTimeStr"
                tvAppointmentPsychologistTime.visibility = View.VISIBLE
            }

            tvPsyFIO.text = psychologist.user_psy?.name?:""

            try {
                psychologist.user_psy?.post?.let { post ->
                    var cutString = post.replace("</strong>", "")
                    cutString = cutString.replace("&nbsp;", "")
                    cutString = cutString.replace("</p>", "")
                    val splitList = cutString.split("Психолог принимает:")
                    if (splitList.isNotEmpty() && splitList.size > 1) {
                        cutString = splitList.last()
                        if (cutString.isNotEmpty() && cutString.first() == ' ') {
                            cutString = cutString.substring(1);
                        }
                        tvPsyAddress.text = cutString
                    }
                }
            } catch (e: Exception) {
                Log.d("TAG_CUT", "Not parse")
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
        var resultMessage = getString(R.string.psy_apple_success)
        if (message.isNotEmpty())
            resultMessage = getString(R.string.psy_apply_error)
        this.view?.let { view ->
            Snackbar.make(view, resultMessage, Snackbar.LENGTH_LONG)
                .setAction("OK") { }
                .setBackgroundTint(context!!.getColor(R.color.colorPrimaryDark))
                .setTextColor(context!!.getColor(R.color.white))
                .setActionTextColor(context!!.getColor(R.color.white))
                .show()
        }

        if (message.isEmpty()) {
            navigationPresenter.popFragment()
            navigationPresenter.popFragment()
        }
    }

    override fun creatingEventInSchedule() {

        val psyModel: Teacher = Teacher()
        psychologist?.user_psy?.let { psychologist ->
            psyModel.id = psychologist.id
            psyModel.name = psychologist.name
            psyModel.post = psychologist.post
            psyModel.image = psychologist.image
            psyModel.socialUrl = psychologist.socialUrl
            psyModel.miptUrl = psychologist.miptUrl
            psyModel.wikiUrl = psychologist.wikiUrl
        }

        val eventModel = EventModel().apply {
            allDay = false
            name = "Запись к психологу"
            type = "other"
            startTime = psychologist?.startTime?.date ?: ""
            endTime = psychologist?.endTime?.date ?: ""
            notes = if (tvPsyAddress.text.isNotEmpty()) "Место приема: ${tvPsyAddress.text}\n\nСобытие создано автоматически. Сообщение с подтверждением записи должно прийти на email." else "Событие создано автоматически. Сообщение с подтверждением записи должно прийти на email."
            teachers = RealmList(psyModel)
            auditorium = RealmList()
            urls = null
        }

        presenter?.createSchedule(eventModel)
    }

    companion object {
        fun newInstance(psychologist: PsyTimeResponseModel.PsyInfoModel): BaseFragment {
            val fragment = RecordApplyFragment()
            fragment.psychologist = psychologist
            return fragment
        }
    }
}