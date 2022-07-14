package dev.phystech.mipt.ui.fragments.support.chat.single

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.utils.ChatUtils

import androidx.recyclerview.widget.LinearLayoutManager
import dev.phystech.mipt.adapters.ChatAdapter2
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.utils.visibility
import android.text.style.ForegroundColorSpan

import androidx.appcompat.view.menu.MenuBuilder
import dev.phystech.mipt.Application
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu as PopupMenu2

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.text.*

import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import dev.phystech.mipt.adapters.ChatAdapter2Groups
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.models.api.*
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment
import dev.phystech.mipt.ui.fragments.support.info_group.GroupInfoFragment
import dev.phystech.mipt.ui.utils.AvatarColor
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil
import io.realm.annotations.Ignore

class ChatFragment: BaseFragment(), ChatContract.View {

    private var presenter: ChatContract.Presenter? = null

    private lateinit var ivBack: ImageView
    private lateinit var ivAvatar: RoundedImageView
    private lateinit var llAvatar: LinearLayout
    private lateinit var tvAvatar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progress: ProgressBar
    private lateinit var rvChat: RecyclerView
    private lateinit var toolbar: Toolbar

    private lateinit var viewBlock: LinearLayout
    private lateinit var etMessage: EditText
    private lateinit var ivSend: ImageView

    private var conversation: Conversation? = null
    private var limitnum: Int = ChatUtils.perPage
    private var limitfrom: Int = 0

    private var isConnect = true
//    private var intentet: Boolean = false

    private var textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            ivSend.visibility = (!s.toString().isNullOrEmpty()).visibility()
        }
    }

    //  ANDROID LIFE CIRCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ChatPresenter(resources, bottomSheetController)
    }

    override fun onResume() {
        (mainActivity as MainActivity).setBottomNavigationVisibility(false)
        super.onResume()
    }

    override fun onPause() {
        (mainActivity as MainActivity).setBottomNavigationVisibility(true)
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        conversation?.id?.let { convid ->
            presenter?.initUserData(convid)
            presenter?.loadChat(convid, limitnum, limitfrom)
        }
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }

    //  MVP BASE VIEW
    override fun bindView(view: View?) {
        if (view == null) return
        toolbar = view.findViewById(R.id.toolbar)
        ivBack = view.findViewById(R.id.ivBack)
        ivAvatar = view.findViewById(R.id.ivAvatar)
        llAvatar = view.findViewById(R.id.llAvatar)
        tvAvatar = view.findViewById(R.id.tvAvatar)
        tvName = view.findViewById(R.id.tvName)
        tvStatus = view.findViewById(R.id.tvStatus)
        progress = view.findViewById(R.id.progress)
        rvChat = view.findViewById(R.id.rvChat)
        viewBlock = view.findViewById(R.id.view_block)
        etMessage = view.findViewById(R.id.etMessage)
        ivSend = view.findViewById(R.id.ivSend)

        ivBack.setOnClickListener(this::back)
        etMessage.addTextChangedListener(textWatcher)
        ivSend.setOnClickListener(this::sendMessage)

        ivAvatar.setOnClickListener(this::showUserInfo)
        llAvatar.setOnClickListener(this::showUserInfo)
        tvName.setOnClickListener(this::showUserInfo)
        tvStatus.setOnClickListener(this::showUserInfo)

        conversation?.let { conversation ->
            if (!conversation.members.isNullOrEmpty()) {
                conversation.members.first()?.let { setContent(it) }
            }
            if (conversation.unreadcount > 0) {
                conversation.unreadcount = 0
                (activity as MainActivity).calculateBadgeChatCount()
            }

            conversation.id?.let { id ->
                val foundChat = ChatUtils.chatsMessages.find { list -> list.id == id }
                foundChat?.let { chat ->
                    val adapter: ChatAdapter2 = ChatAdapter2()
                    adapter.items.clear()
                    adapter.items.addAll(chat.messages)
                    ChatUtils.setHeaderDateToMessages(adapter.items)
                    adapter.notifyDataSetChanged()
                    setAdapter(adapter)
                }

            }
//            if (!conversation.savedMessages.isNullOrEmpty()) {
//                val adapter: ChatAdapter2 = ChatAdapter2()
//                adapter.items.clear()
//                adapter.items.addAll(conversation.savedMessages)
//                ChatUtils.setHeaderDateToMessages(adapter.items)
//                adapter.notifyDataSetChanged()
//                setAdapter(adapter)
//            }

            if (conversation.id == null) {
                val adapter = presenter?.getAdapter()
                adapter?.let {
                    it.loadNewMessages = false
                    setAdapter(it)
                }
            }
        }
//        setupToolbarMenu(intentet)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_block_user -> {
                conversation?.let { conversation ->
                    showAlertBlockUser(conversation)
                }
                true
            }
            R.id.menu_item_unblock_user -> {
                conversation?.let { conversation ->
                    showAlertUnblockuser(conversation)
                }
                true
            }
            R.id.menu_item_delete_dialog -> {
                conversation?.id?.let { conversationId ->
                    presenter?.deleteChat(conversationId)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    override fun setContent(model: Member) {

        // Раскомментировать когда данные о собеседнике будут правильно приходить с сервера, если захотят
//        conversation?.let { conversation ->
//            if (!conversation.members.isNullOrEmpty()) {
//                conversation.members[0] = model
//            }
//        }

        val img = if (model.user!!.image != null && !model.user!!.image?.signatures.isNullOrEmpty()) {
            model.user!!.image?.signatures?.firstOrNull()
        } else null
        if (img != null) {
            llAvatar.visibility = View.GONE
            Picasso.get()
                .load(NetworkUtils.getImageUrl(img.id, img.dir, img.path))
                // .error(R.drawable.ic_filter_dield_example)
                .into(ivAvatar)
        } else {

            val colorRandomPosition = model.user!!.name.hashCode() % 6
            val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
            llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))
//            llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ColorUtil.getColorFromHash(model.user.name!!), BlendModeCompat.SRC_ATOP))

            tvAvatar.text = model.getAvatarName()
            llAvatar.visibility = View.VISIBLE
        }

        tvName.text = model.user!!.getTrimName()

        tvStatus.text = if (isConnect) ChatUtils.getStatus(model.isonline, model.user!!.lastactive) else getString(R.string.connection)

        setupToolbarMenu(model.isblocked)
        viewBlock.visibility = (model.isblocked).visibility()
        etMessage.isEnabled = !model.isblocked
    }

    override fun updateMessageStatus(localeId: Long?, message: Message?) {
        rvChat.adapter?.let { adapter ->
            (adapter as ChatAdapter2).updateMessageStatus(localeId, message)
            conversation?.id?.let { id ->
                if (message?.status == "successful") {
                    limitfrom ++
                    limitnum ++
                    ChatRepository.shared.saveMessagesOfDialog(id, adapter.items, 1)
                }
            }
        }
    }

    override fun showLoader(visible: Boolean) {
        progress.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun setupToolbarMenu(userIsBlock: Boolean) {

//        this.intentet = intentet

        // TODO если нет соединения

//        if (intentet) {
//            toolbar.menu.clear()
//            toolbar.inflateMenu(R.menu.chat)
//        } else {
//            toolbar.menu.clear()
//            toolbar.inflateMenu(R.menu.chat_disable)
//        }

        if (userIsBlock) {
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.chat_unblock_user)
        } else {
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.chat)
        }

        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }
    }

    override fun deleteMessage(id: Long) {
        rvChat.adapter?.let { adapter ->
            (adapter as ChatAdapter2).deleteMessage(id)
        }
    }

    override fun chatIsDeleted() {
        navigationPresenter.popFragment()
    }

    override fun connect(value: Boolean) {
        isConnect = value
        conversation?.let { conversation ->
            if (!conversation.members.isNullOrEmpty()) {
                conversation.members.first()?.let { setContent(it) }
            }
        }
    }

    override fun updateConversationId(conversationId: Long) {
        conversation?.let { conversation ->
            conversation.id = conversationId
        }
    }

    override fun showBlockUserView(value: Boolean) {
        setupToolbarMenu(value)
        viewBlock.visibility = (value).visibility()
        etMessage.isEnabled = !value
    }

    private fun showAlertBlockUser(conversation: Conversation) {
        conversation.members?.let { members ->
            if (members.isNotEmpty()) {
                val alertView = layoutInflater.inflate(R.layout.alert_block_user, null)
                val tvDescription: TextView = alertView.findViewById(R.id.tvDescription)
                val btnUnlock: Button = alertView.findViewById(R.id.btnUnlock)
                val btnCansel: Button = alertView.findViewById(R.id.btnCansel)

                val alert = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setView(alertView)
                    .create()

                tvDescription.text = this.getString(R.string.user_lock_text, members.first()!!.user!!.getTrimName())

                btnUnlock.setOnClickListener {
                    presenter?.blockUser(members.first()!!.id)
                    alert.cancel()
                }

                btnCansel.setOnClickListener {
                    alert.cancel()
                }

                alert.show()
            }
        }
    }

    private fun showAlertUnblockuser(conversation: Conversation) {
        conversation.members?.let { members ->
            if (members.isNotEmpty()) {
                val alertView = layoutInflater.inflate(R.layout.alert_unblock_user, null)
                val tvDescription: TextView = alertView.findViewById(R.id.tvDescription)
                val btnUnlock: Button = alertView.findViewById(R.id.btnUnlock)
                val btnCansel: Button = alertView.findViewById(R.id.btnCansel)

                val alert = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setView(alertView)
                    .create()

                tvDescription.text = this.getString(R.string.user_unlock_text, members.first()!!.user!!.getTrimName())

                btnUnlock.setOnClickListener {
                    presenter?.unblockUser(members.first()!!.user!!.id)
                    alert.cancel()
                }

                btnCansel.setOnClickListener {
                    alert.cancel()
                }

                alert.show()
            }
        }
    }

    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {

        (adapter as ChatAdapter2).onClickListener = ChatAdapter2.OnClickListener {
            showMessageOptions(it.first, it.second)
        }

        (adapter as ChatAdapter2).onLoadData = ChatAdapter2.OnLoadData {
            limitfrom = limitnum
            limitnum += ChatUtils.perPage

            conversation?.id?.let { chatId ->
                presenter?.loadChat(chatId, limitnum, limitfrom)
            }
        }

        (rvChat.layoutManager as? LinearLayoutManager)?.let { lm ->
            lm.stackFromEnd = true
        }

        rvChat.setHasFixedSize(true)
        rvChat.adapter = adapter
        rvChat.smoothScrollToPosition(adapter.items.size)
    }

    override fun scrollToBottom() {

        limitfrom ++
        limitnum ++

        presenter?.getAdapter()?.let { adapter ->
            rvChat.smoothScrollToPosition(adapter.items.size)
        }
    }

    //  EVENTS

    fun back(view: View) {
        navigationPresenter.popFragment()
    }

    private fun showUserInfo(view: View) {
        conversation?.members?.first()?.let {
            if (!it.id.isNullOrEmpty() && !it.user!!.name.isNullOrEmpty()) {

                val userData: Teacher = Teacher()
                userData.id = it.user!!.id
                userData.name = it.user!!.name
                userData.post = it.user!!.post
                userData.image = it.user!!.image
                userData.socialUrl = it.user!!.socialURL
                userData.miptUrl = it.user!!.miptURL
                userData.wikiUrl = it.user!!.wikiURL

                val teacherFragment = TeacherDetailFragment.newInstance(it.id.toInt(), it.user!!.name, userModel = userData, roles = it.user!!.roles, lastactive = it.user!!.lastactive, showWriteMessage = false)
                navigate(teacherFragment)
            }
        }
    }

    private fun sendMessage(view: View) {
        rvChat.adapter?.let { adapter ->
            val message = etMessage.text.toString()
            val newMessage = ChatUtils.createNewMessage(ChatUtils.userId, message)
            etMessage.text.clear()
            (adapter as ChatAdapter2).newMessage(newMessage)
            rvChat.smoothScrollToPosition(adapter.items.size)

            conversation?.let { conversation ->
                if (conversation.id != null) {
                    presenter?.sendMessage(conversation.id!!, message.replace("\n", "<br>"), newMessage.localeId)
                } else {
                    if (!conversation.members.isNullOrEmpty()) {
                        presenter?.sendMessageNewChat(conversation.members.first()!!.id, message.replace("\n", "<br>"), newMessage.localeId)
                    } else {
                        showError(Application.context.getString(R.string.chat_send_message_no_work))
                    }
                }
            }
        }
    }

    private fun showMessageOptions(view: View, model: Message) {
        context?.let { context ->

            val popup = PopupMenu2(context, view)
            if (model.status == "error") {
                popup.inflate(R.menu.chat_message_options_error)
            } else {
                popup.inflate(R.menu.chat_message_options)
            }

            val item: MenuItem = popup.menu.getItem(popup.menu.size() - 1)
            val deleteItem = SpannableString(item.title)
            deleteItem.setSpan(ForegroundColorSpan(Application.context.getColor(R.color.holo_red_light)), 0, deleteItem.length, 0)
            item.title = deleteItem

            popup.setOnMenuItemClickListener(PopupMenu2.OnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {

                    R.id.chat_message_options_resend -> {
                        model.status = "waiting"
                        updateMessageStatus(model.localeId, model)
                        conversation?.let { conversation ->
                            if (conversation.id != null) {
                                presenter?.sendMessage(conversation.id!!, model.text, model.localeId)
                            } else {
                                if (!conversation.members.isNullOrEmpty()){
                                    presenter?.sendMessageNewChat(conversation.members.first()!!.id, model.text, model.localeId)
                                }  else {
                                    showError(Application.context.getString(R.string.chat_send_message_no_work))
                                }
                            }
                        }
                    }

                    R.id.chat_message_options_copy -> {
                        var copyText: String = ""
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            copyText = (Html.fromHtml(model.text.replace("\n", "<br>"), Html.FROM_HTML_MODE_LEGACY)).toString()
                        } else {
                            copyText = (Html.fromHtml(model.text.replace("\n", "<br>"))).toString()
                        }
                        val clipboard = getSystemService(context, ClipboardManager::class.java)
                        val clip = ClipData.newPlainText("copy message", copyText)
                        clipboard?.setPrimaryClip(clip)
                    }

                    R.id.chat_message_options_delete -> {
                        if (model.status == "error") {
                            rvChat.adapter?.let { adapter ->
                                (adapter as ChatAdapter2).deleteMessageLocal(model.localeId)
                            }
                        } else {
                            model.id?.let { messageId ->
                                conversation?.id?.let { convId ->
                                    presenter?.deleteMessage(convId, messageId)
                                }
                            }
                        }
                    }
                }
                true
            })

            val menuHelper = MenuPopupHelper(context, popup.getMenu() as MenuBuilder, view)
            menuHelper.setForceShowIcon(true)
            menuHelper.show()
        }
    }

    companion object {
        fun newInstance(conversation: Conversation): BaseFragment {
            val fragment = ChatFragment()
            fragment.conversation = conversation
            return fragment
        }
    }
}