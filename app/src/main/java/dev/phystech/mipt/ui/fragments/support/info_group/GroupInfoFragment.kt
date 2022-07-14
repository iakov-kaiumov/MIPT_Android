package dev.phystech.mipt.ui.fragments.support.info_group

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ChatGroupMembersAdapter
import dev.phystech.mipt.adapters.ChatsGroupsAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.models.api.BaseApiEntity
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment
import dev.phystech.mipt.ui.fragments.support.chat.groups.GroupsChatFragment
import dev.phystech.mipt.ui.fragments.support.chat.groups.GroupsChatPresenter
import dev.phystech.mipt.ui.fragments.support.chats.ChatsContract
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.ChatUtils
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil

class GroupInfoFragment: BaseFragment() {

    private lateinit var ivBack: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView

    private lateinit var progress: ProgressBar
    private lateinit var rvMembers: RecyclerView

    private var conversation: Conversation? = null
    private var members: ArrayList<Member> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group_info, container, false);
    }

    //  MVP BASE VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        tvName = view.findViewById(R.id.tvName)
        tvStatus = view.findViewById(R.id.tvStatus)

        progress = view.findViewById(R.id.progress)
        rvMembers = view.findViewById(R.id.rvMembers)

        conversation?.let { setContent(it) }

        ivBack.setOnClickListener(this::back)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    fun setAdapter() {
        val adapter: ChatGroupMembersAdapter = ChatGroupMembersAdapter()
        adapter.items.clear()
        adapter.items.addAll(members)
        adapter.onClickListener = ChatGroupMembersAdapter.OnClickListener {
            if (!it.id.isNullOrEmpty() && !it.user?.name.isNullOrEmpty()) {

                val userData: Teacher = Teacher()
                userData.id = it.user?.id
                userData.name = it.user?.name
                userData.post = it.user?.post
                userData.image = it.user?.image
                userData.socialUrl = it.user?.socialURL
                userData.miptUrl = it.user?.miptURL
                userData.wikiUrl = it.user?.wikiURL

                val teacherFragment = TeacherDetailFragment.newInstance(it.id.toInt(), it.user?.name, userModel = userData, roles = it.user?.roles, lastactive = it.user?.lastactive)
                navigate(teacherFragment)
            }
        }
        rvMembers.adapter = adapter
    }

    private fun back(view: View) {
        navigationPresenter.popFragment()
    }

    fun setContent(conversation: Conversation) {
        tvName.text = conversation.name
        tvStatus.text = ChatUtils.getStatusGroup(members.size.toLong())
        setAdapter()
    }

    companion object {
        fun newInstance(conversation: Conversation, members: ArrayList<Member>): BaseFragment {
            val fragment = GroupInfoFragment()
            fragment.conversation = conversation
            fragment.members = members
            return fragment
        }
    }
}