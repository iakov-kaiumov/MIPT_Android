package dev.phystech.mipt.ui.fragments.services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.fragments.services.bills.BillsFragment
import dev.phystech.mipt.ui.fragments.services.feedback.FeedbackFragment
import dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_help.PsychologistsHelpFragment
import dev.phystech.mipt.ui.fragments.services.settings.SettingsFragment
import dev.phystech.mipt.ui.fragments.services.sport_section_list.SportSectionListFragment
import dev.phystech.mipt.ui.utils.AvatarColor

class ServicesFragment : BaseFragment() {

    lateinit var tvAvatarName: TextView

    lateinit var tvTitle: TextView
    lateinit var tvNumber: TextView
    lateinit var rlInDevelop: RelativeLayout

    lateinit var llBills: LinearLayout
    lateinit var llAnalitycs: LinearLayout
    lateinit var llQuestions: LinearLayout
    lateinit var llFeedback: LinearLayout
    lateinit var llDocuments: LinearLayout
    lateinit var llDevelop: LinearLayout
    lateinit var llSections: LinearLayout
    lateinit var llPsychologist: LinearLayout
    lateinit var profile_image: CircleImageView

    lateinit var ivSettings: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return;

        tvAvatarName = view.findViewById(R.id.tvAvatarName)

        tvTitle = view.findViewById(R.id.tvTitle)
        tvNumber = view.findViewById(R.id.tvNumber)

        llBills = view.findViewById(R.id.llBills)
        llAnalitycs = view.findViewById(R.id.llAnalitycs)
        llQuestions = view.findViewById(R.id.llQuestions)
        llFeedback = view.findViewById(R.id.llFeedback)
        llDocuments = view.findViewById(R.id.llDocuments)
        llDevelop = view.findViewById(R.id.llDevelop)
        llSections = view.findViewById(R.id.llSections)
        llPsychologist = view.findViewById(R.id.llPsychologist)
        profile_image = view.findViewById(R.id.profile_image)

        ivSettings = view.findViewById(R.id.ivSettings)


        UserRepository.shared.userInfo?.let {
            tvTitle.text = ("${it.firstname ?: ""} ${it.lastname ?: ""}").trim()
            tvNumber.text = it.groups.firstOrNull()?.name ?: ""

            val fLetter = it.firstname?.firstOrNull()?.toUpperCase() ?: ""
            val lLetter = it.lastname?.firstOrNull()?.toUpperCase() ?: ""
            tvAvatarName.text = "$fLetter$lLetter"

            val colorRandomPosition = (it.firstname + it.lastname).hashCode() % 6
            val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
            profile_image.setImageResource(avatarColor.colorResId)

        }

        llBills.setOnClickListener {
            navigationPresenter.pushFragment(BillsFragment(), true)
        }

        llFeedback.setOnClickListener {
            navigationPresenter.pushFragment(FeedbackFragment(), true)
        }

        ivSettings.setOnClickListener {
            navigationPresenter.pushFragment(SettingsFragment(), true)
        }

        llPsychologist.setOnClickListener {
            navigationPresenter.pushFragment(PsychologistsHelpFragment(), true)
        }

        llDevelop.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mipt.phystech.dev/#rec353825131"))
            startActivity(intent)
        }

        llSections.setOnClickListener {
            val sectionsListFragment = SportSectionListFragment()
            navigationPresenter.pushFragment(sectionsListFragment, true)
        }
    }
}