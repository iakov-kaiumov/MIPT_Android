package dev.phystech.mipt.ui.fragments.study.teacher_detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.bundleOf
import com.squareup.picasso.Picasso
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.models.*
import dev.phystech.mipt.models.api.BaseApiEntity
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.repositories.TeachersRepository
import dev.phystech.mipt.ui.fragments.support.chat.single.ChatFragment
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.visibility
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil
import io.realm.RealmList

/** Экран с подробной информацией преподвателя
 *
 */
class TeacherDetailFragment : BaseFragment() {

    lateinit var rlCall: RelativeLayout
    lateinit var rlMail: RelativeLayout
    lateinit var rlProfile: RelativeLayout
    lateinit var rlMiptLink: RelativeLayout
    lateinit var rlWikiLink: RelativeLayout

    lateinit var scrollContent: View
    lateinit var llNoTeacher: View

    lateinit var tvCall: TextView
    lateinit var tvMail: TextView
    lateinit var tvProfile: TextView
    lateinit var tvMiptLink: TextView
    lateinit var tvWikiLink: TextView
    lateinit var tvLinks: TextView

    lateinit var tvWriteMessage: TextView
    lateinit var llWriteMessage: LinearLayout
    lateinit var llUserNotAutorize: LinearLayout

    lateinit var tvName: TextView
    lateinit var tvRole: TextView
    lateinit var tvPost: TextView
    lateinit var ivAvatar: ImageView
    lateinit var llAvatar: RelativeLayout
    lateinit var tvAvatar: TextView
    lateinit var ivBack: ImageView

    private var teacherModel: Teacher? = null

    private var userModel: Teacher? = null
    private var rolesModel: List<String>? = null
    private var lastactiveModel: Lastactive? = null
    private var showWriteMessage = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_detail, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        val teacherId = arguments?.getInt(KEY_ID, -1)

        rlCall = view.findViewById(R.id.rlCall)
        rlMail = view.findViewById(R.id.rlMail)
        rlProfile = view.findViewById(R.id.rlProfile)
        rlMiptLink = view.findViewById(R.id.rlMiptLink)
        rlWikiLink = view.findViewById(R.id.rlWikiLink)

        scrollContent = view.findViewById(R.id.scrollContent)
        llNoTeacher = view.findViewById(R.id.llNoTeacher)

        tvCall = view.findViewById(R.id.tvCall)
        tvMail = view.findViewById(R.id.tvMail)

        tvProfile = view.findViewById(R.id.tvProfile)
        tvMiptLink = view.findViewById(R.id.tvMiptLink)
        tvWikiLink = view.findViewById(R.id.tvWikiLink)
        tvLinks = view.findViewById(R.id.tvLinks)
        tvName = view.findViewById(R.id.tvName)
        tvRole = view.findViewById(R.id.tvRole)
        tvPost = view.findViewById(R.id.tvPost)

        tvWriteMessage = view.findViewById(R.id.tvWriteMessage)
        llWriteMessage = view.findViewById(R.id.llWriteMessage)
        llUserNotAutorize = view.findViewById(R.id.llUserNotAutorize)

        ivAvatar = view.findViewById(R.id.ivAvatar)
        llAvatar = view.findViewById(R.id.llAvatar)
        tvAvatar = view.findViewById(R.id.tvAvatar)
        ivBack = view.findViewById(R.id.ivBack)

        if (userModel != null) {
            userModel?.id?.let { id ->
                getUserInfo(id, userModel!!)
            }
            setContent(userModel!!)
            tvRole.visibility = (!rolesModel.isNullOrEmpty()).visibility()
        } else {

            showProgress()

            TeachersRepository.shared.getByID(teacherId.toString()) {
                hideProgress()
                teacherModel = it

                if (teacherModel == null) {
                    teacherModel = Teacher().apply {
                        name = arguments?.getString(KEY_NAME)
                    }
//                showMessage(R.string.teacher_not_found)
//                navigationPresenter.popFragment()
//                return@getByID
                }

                teacherModel?.id?.let { id ->
                    getUserInfo(id, teacherModel!!)
                }

                setContent(teacherModel!!)
            }
        }

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    private fun setContent(teacherModel: Teacher) {
        if (teacherModel.id == null || (teacherModel.post.isNullOrEmpty() && teacherModel.wikiUrl.isNullOrEmpty() && teacherModel.miptUrl.isNullOrEmpty() && teacherModel.socialUrl.isNullOrEmpty())) {
            tvName.text = teacherModel.name
            scrollContent
            llNoTeacher.visibility = true.visibility()

        }
//        tvCall.text = teacherModel.phone
//        tvMail.text = teacherModel.email
        tvProfile.text = teacherModel.socialUrl
//        tvMiptLink.text = teacherModel.miptUrl
//        tvWikiLink.text = teacherModel.wikiUrl
        tvName.text = teacherModel.name
        if (!rolesModel.isNullOrEmpty()) tvRole.text = ChatUtils.getUserRoles(rolesModel!!)
        tvPost.text = Html.fromHtml(teacherModel.post ?: "", Html.FROM_HTML_MODE_LEGACY)
        tvAvatar.text = teacherModel.name
            ?.split(' ')
            ?.filterNotNull()
            ?.take(2)
            ?.joinToString(String.empty()) { v ->
                v.first().toString()
            } ?: String.empty()

        rlCall.visibility = false.visibility()
        rlMail.visibility = false.visibility()
        rlProfile.visibility = false.visibility()
//        rlProfile.visibility = teacherModel.socialUrl.isNullOrBlank().not().visibility()
        rlMiptLink.visibility = teacherModel.miptUrl.isNullOrBlank().not().visibility()
        rlWikiLink.visibility = teacherModel.wikiUrl.isNullOrBlank().not().visibility()
        tvName.visibility = teacherModel.name.isNullOrBlank().not().visibility()
        tvPost.visibility = teacherModel.post.isNullOrBlank().not().visibility()

        tvLinks.visibility =
            (teacherModel.miptUrl.isNullOrBlank() && teacherModel.wikiUrl.isNullOrBlank()).not()
                .visibility()

        rlMiptLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(teacherModel.miptUrl))
            startActivity(intent)
        }
        rlWikiLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(teacherModel.wikiUrl))
            startActivity(intent)
        }

//        .split(" ").filter { v -> v.length > 0 }.take(2).joinToString { v -> v.first().toString() }

//        teacherModel.image?.signatures?.firstOrNull()?.let { imgModel ->
//            val imgUrl = NetworkUtils.getImageUrl(imgModel.id, imgModel.dir, imgModel.path)
//            Picasso.get()
//                .load(imgUrl)
//                .into(ivAvatar)
//        }

        val img = if (teacherModel.image != null && !teacherModel.image?.signatures.isNullOrEmpty()) {
            teacherModel.image?.signatures?.firstOrNull()
        } else null
        if (img != null) {
            llAvatar.visibility = View.GONE
            Picasso.get()
                .load(NetworkUtils.getImageUrl(img.id, img.dir, img.path))
                // .error(R.drawable.ic_filter_dield_example)
                .into(ivAvatar)
        } else {
            val colorRandomPosition = teacherModel.name.hashCode() % 6
            val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
            llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))
            llAvatar.visibility = View.VISIBLE
        }
    }

    private fun getUserInfo(id: String, model: Teacher) {
        ChatRepository.shared.getUserInfo(id) { members ->
            if (showWriteMessage && members.isNotEmpty()) {
                llWriteMessage.visibility = true.visibility()
                llUserNotAutorize.visibility = (lastactiveModel == null).visibility()
                if (members.first().canmessage != null && members.first().canmessage!!) {
                    tvWriteMessage.text = Application.context.getString(R.string.write_message)
                    tvWriteMessage.setTextColor(ContextCompat.getColor(Application.context, R.color.color_android_main))
                    llWriteMessage.background = ContextCompat.getDrawable(Application.context, R.drawable.bg_button_cornerred_2_enable)
                    tvWriteMessage.setOnClickListener {

                        ChatUtils.singleChats.value?.let { arrayDialogs ->

                            var conversation = arrayDialogs.firstOrNull { v -> v.baseChatType == 0 && v.members.isNotEmpty() && v.members.first()?.id == id}

                            if (conversation != null) {
                                val fragment = ChatFragment.newInstance(conversation)
                                navigationPresenter.pushFragment(fragment, true)
                            } else {
                                members.first().user = User().apply {
                                    this.id = id
                                    name = model.name
                                    post = model.post
                                    image = model.image
                                    socialURL = model.socialUrl
                                    miptURL = model.miptUrl
                                    wikiURL = model.wikiUrl
                                    updated = null
                                    lastactive = lastactiveModel
                                    roles = if (teacherModel != null) RealmList("teacher") else {
                                        val rolesTemp = RealmList<String>()
                                        rolesModel?.let { rolesTempModel -> rolesTemp.addAll(rolesTempModel)}
                                        rolesTemp
                                    }
                                }

                                conversation = Conversation().apply {
                                    this.members = RealmList(members.first())
                                    unreadcount = 0
                                }

                                val fragment = ChatFragment.newInstance(conversation)
                                navigationPresenter.pushFragment(fragment, true)
                            }
                        }
                    }
                }
            } else {
                llWriteMessage.visibility = false.visibility()
            }
        }
    }

    companion object {

        const val KEY_ID = "teacher.id"
        const val KEY_NAME = "teacher.name"

        fun newInstance(id: Int, name: String?, userModel: Teacher? = null, roles: List<String>? = null, lastactive: Lastactive? = null, showWriteMessage: Boolean = true): BaseFragment {
            val fragment = TeacherDetailFragment().apply {
                arguments = bundleOf(
                    KEY_ID to id,
                    KEY_NAME to name
                )
            }

            fragment.userModel = userModel
            fragment.rolesModel = roles
            fragment.lastactiveModel = lastactive

            fragment.showWriteMessage = showWriteMessage

            return fragment
        }
    }
}