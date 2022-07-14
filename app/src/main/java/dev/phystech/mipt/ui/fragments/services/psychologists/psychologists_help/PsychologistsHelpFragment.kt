package dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.ui.fragments.services.psychologists.UrgentHelpFragment
import dev.phystech.mipt.ui.fragments.services.psychologists.appointment_with_psy.AppointmentWithPsyFragment
import dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_list.PsychologistsListContract
import dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_list.PsychologistsListFragment
import dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_list.PsychologistsListPresenter

class PsychologistsHelpFragment : BaseFragment(), PsychologistsHelpContract.View {

    private var presenter: PsychologistsHelpContract.Presenter? = null

    private lateinit var ivBack: ImageView

    private lateinit var llOfflineApply: LinearLayout
    private lateinit var llOnlineApply: LinearLayout
    private lateinit var llUrgentHelp: LinearLayout

    private lateinit var rlPsyMiptWebsite: RelativeLayout
    private lateinit var rlPsyVk: RelativeLayout
    private lateinit var rlPsyPhone: RelativeLayout
    private lateinit var rlPsyPsychologistsInfo: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_psychologists_help, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = PsychologistsHelpPresenter(resources, bottomSheetController)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        presenter?.loadPsychologists()
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }

    //  MVP VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)

        llOfflineApply = view.findViewById(R.id.llOfflineApply)
        llOnlineApply = view.findViewById(R.id.llOnlineApply)
        llUrgentHelp = view.findViewById(R.id.llUrgentHelp)

        rlPsyMiptWebsite = view.findViewById(R.id.rlPsyMiptWebsite)
        rlPsyVk = view.findViewById(R.id.rlPsyVk)
        rlPsyPhone = view.findViewById(R.id.rlPsyPhone)
        rlPsyPsychologistsInfo = view.findViewById(R.id.rlPsyPsychologistsInfo)


        //  EVENTS

        ivBack.setOnClickListener(this::onBack)

        llOfflineApply.setOnClickListener(this::offlineApply)
        llOnlineApply.setOnClickListener(this::onlineApply)
        llUrgentHelp.setOnClickListener(this::urgentHelp)

        rlPsyMiptWebsite.setOnClickListener(this::psyMiptWebsite)
        rlPsyVk.setOnClickListener(this::psyVk)
        rlPsyPhone.setOnClickListener(this::psyPhone)
        rlPsyPsychologistsInfo.setOnClickListener(this::psyPsychologistsInfo)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  OTHERS

    fun onBack(view: View) {
        navigationPresenter.popFragment()
    }

    fun offlineApply(view: View) {
        presenter?.getPsychologists()?.let { psychologists ->
            if (psychologists.isNotEmpty())
                navigate(AppointmentWithPsyFragment.newInstance(psychologists = psychologists, online = false))
        }
    }

    fun onlineApply(view: View) {
        presenter?.getPsychologists()?.let { psychologists ->
            if (psychologists.isNotEmpty())
                navigate(AppointmentWithPsyFragment.newInstance(psychologists = psychologists, online = true))
        }
    }

    fun urgentHelp(view: View) {
        navigate(UrgentHelpFragment())
    }


    fun psyMiptWebsite(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PSY_MIPT_WEBSITE_URL))
        startActivity(intent)
    }

    fun psyVk(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PSY_VK_URL))
        startActivity(intent)
    }

    fun psyPhone(view: View) {
        val intent = Intent(Intent.ACTION_VIEW);
        intent.data = Uri.parse("tel:$PSY_PHONE")
        startActivity(intent)
    }

    fun psyPsychologistsInfo(view: View) {
        navigate(PsychologistsListFragment.newInstance(psychologists = presenter?.getPsychologists()?:arrayListOf()))
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

    companion object {
        private const val PSY_MIPT_WEBSITE_URL = "https://mipt.ru/students/life/psy/"
        private const val PSY_VK_URL = "https://vk.com/sdmipt"
        private const val PSY_PHONE = "+7 980 333 44 55"
    }

}