package dev.phystech.mipt.ui.fragments.support.chat.groups

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.utils.ChatUtils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import dev.phystech.mipt.Application
import dev.phystech.mipt.adapters.ChatAdapter2
import dev.phystech.mipt.adapters.ChatAdapter2Groups
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.models.api.Message
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.ui.fragments.support.info_group.GroupInfoFragment
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.visibility
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil

class GroupsChatFragment: BaseFragment(), GroupsChatContract.View {

    private var presenter: GroupsChatContract.Presenter? = null

    private lateinit var ivBack: ImageView
    private lateinit var llAvatar: LinearLayout
    private lateinit var tvAvatar: TextView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progress: ProgressBar
    private lateinit var rvChat: RecyclerView
    private lateinit var toolbar: Toolbar

    private lateinit var etMessage: EditText
    private lateinit var ivSend: ImageView

    private var conversation: Conversation? = null
    private var limitnum: Int = ChatUtils.perPage
    private var limitfrom: Int = 0

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
        presenter = GroupsChatPresenter(resources, bottomSheetController)
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
        return inflater.inflate(R.layout.fragment_chat_groups, container, false);
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        conversation?.id?.let { convId ->
            presenter?.initUserData(convId)
            presenter?.loadChat(convId, limitnum, limitfrom)
            presenter?.getGroupChatMembers(convId)
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
        llAvatar = view.findViewById(R.id.llAvatar)
        tvAvatar = view.findViewById(R.id.tvAvatar)
        tvName = view.findViewById(R.id.tvName)
        tvStatus = view.findViewById(R.id.tvStatus)
        progress = view.findViewById(R.id.progress)
        rvChat = view.findViewById(R.id.rvChat)
        etMessage = view.findViewById(R.id.etMessage)
        ivSend = view.findViewById(R.id.ivSend)

        ivBack.setOnClickListener(this::back)
        etMessage.addTextChangedListener(textWatcher)
        ivSend.setOnClickListener(this::sendMessage)

        llAvatar.setOnClickListener(this::showGroupInfo)
        tvName.setOnClickListener(this::showGroupInfo)
        tvStatus.setOnClickListener(this::showGroupInfo)

        conversation?.let { conversation ->
            setContent(conversation)
            if (conversation.unreadcount > 0) {
                conversation.unreadcount = 0
                (activity as MainActivity).calculateBadgeChatCount()
            }

            conversation.id?.let { id ->
                val foundChat = ChatUtils.chatsMessages.find { list -> list.id == id }
                foundChat?.let { chat ->
                    val adapter: ChatAdapter2Groups = ChatAdapter2Groups()
                    adapter.items.clear()
                    adapter.items.addAll(chat.messages)
                    ChatUtils.setHeaderDateToMessages(adapter.items)
                    adapter.notifyDataSetChanged()
                    setAdapter(adapter)
                }
            }
//            if (!conversation.savedMessages.isNullOrEmpty()) {
//                val adapter: ChatAdapter2Groups = ChatAdapter2Groups()
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
        setupToolbarMenu(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_block_user -> {
                conversation?.let { conversation ->
//                    showAlertBlockUser(conversation)
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

    override fun setContent(conversation: Conversation) {

        val colorRandomPosition = conversation.name.hashCode() % 6
        val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
        llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))
//        llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ColorUtil.getColorFromHash(conversation.name), BlendModeCompat.SRC_ATOP))

        tvAvatar.text = conversation.getAvatarName()
        tvName.text = conversation.name
        tvStatus.text = conversation.membercount?.let { ChatUtils.getStatusGroup(it) }
    }

    override fun updateMessageStatus(localeId: Long?, message: Message?) {
        rvChat.adapter?.let { adapter ->
            (adapter as ChatAdapter2Groups).updateMessageStatus(localeId, message)
            conversation?.id?.let { id ->
                if (message?.status == "successful") {
                    limitfrom ++
                    limitnum ++
                    ChatRepository.shared.saveMessagesOfDialog(id, adapter.items, 2)
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

    override fun setupToolbarMenu(intentet: Boolean) {

        // TODO если нет соединения

        if (intentet) {
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.chat_group)
        } else {
            toolbar.menu.clear()
            toolbar.inflateMenu(R.menu.chat_disable)
        }

        toolbar.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }
    }

    override fun deleteMessage(id: Long) {
        rvChat.adapter?.let { adapter ->
            (adapter as ChatAdapter2Groups).deleteMessage(id)
        }
    }

    override fun chatIsDeleted() {
        navigationPresenter.popFragment()
    }

    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {

        (adapter as ChatAdapter2Groups).onClickListener = ChatAdapter2Groups.OnClickListener {
            showMessageOptions(it.first, it.second)
        }

        (adapter as ChatAdapter2Groups).onLoadData = ChatAdapter2Groups.OnLoadData {
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

    private fun showGroupInfo(view: View) {
        presenter?.getMembers()?.let { members ->
            if (members.isNotEmpty())
                conversation?.let { conversation ->
                    navigate(GroupInfoFragment.newInstance(conversation, members))
                }
        }
    }

    private fun sendMessage(view: View) {
        rvChat.adapter?.let { adapter ->
            val message = etMessage.text.toString()
            val newMessage = ChatUtils.createNewMessage(ChatUtils.userId, message)
            etMessage.text.clear()
            (adapter as ChatAdapter2Groups).newMessage(newMessage)
            rvChat.smoothScrollToPosition(adapter.items.size)

            conversation?.id?.let { chatId ->
                presenter?.sendMessage(chatId, message.replace("\n", "<br>"), newMessage.localeId)
            }
        }
    }

    private fun showMessageOptions(view: View, model: Message) {
        context?.let { context ->

            val popup = PopupMenu(context, view)
            if (model.status == "error") {
                popup.inflate(R.menu.chat_message_options_error)
            } else {
                popup.inflate(R.menu.chat_message_options)
            }

            val item: MenuItem = popup.menu.getItem(popup.menu.size() - 1)
            val deleteItem = SpannableString(item.title)
            deleteItem.setSpan(ForegroundColorSpan(Application.context.getColor(R.color.holo_red_light)), 0, deleteItem.length, 0)
            item.title = deleteItem

            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                when (item!!.itemId) {

                    R.id.chat_message_options_resend -> {
                        model.status = "waiting"
                        updateMessageStatus(model.localeId, model)
                        conversation?.id?.let { chatId ->
                            presenter?.sendMessage(chatId, model.text, model.localeId)
                        }
                    }

                    R.id.chat_message_options_copy -> {
                        var copyText: String = ""
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            copyText = (Html.fromHtml(model.text.replace("\n", "<br>"), Html.FROM_HTML_MODE_LEGACY)).toString()
                        } else {
                            copyText = (Html.fromHtml(model.text.replace("\n", "<br>"))).toString()
                        }
                        val clipboard =
                            ContextCompat.getSystemService(context, ClipboardManager::class.java)
                        val clip = ClipData.newPlainText("copy message", copyText)
                        clipboard?.setPrimaryClip(clip)
                    }

                    R.id.chat_message_options_delete -> {
                        if (model.status == "error") {
                            rvChat.adapter?.let { adapter ->
                                (adapter as ChatAdapter2Groups).deleteMessageLocal(model.localeId)
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
            val fragment = GroupsChatFragment()
            fragment.conversation = conversation
            return fragment
        }
    }
}