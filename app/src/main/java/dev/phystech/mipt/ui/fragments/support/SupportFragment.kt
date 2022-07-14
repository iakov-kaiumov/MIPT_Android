package dev.phystech.mipt.ui.fragments.support

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.TopicsAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.utils.ChatSession
import dev.phystech.mipt.utils.KeyboardUtils
import dev.phystech.mipt.utils.visibility
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import dev.phystech.mipt.models.News
import dev.phystech.mipt.ui.fragments.study.chair_filter.ChairFilterFragment

class SupportFragment : BaseFragment(), ChatSession.Delegate, TopicsAdapter.Delegate {

    private lateinit var arrayList: ArrayList<String>

    private lateinit var ivBack: ImageView
    lateinit var recyclerTopics: RecyclerView
    lateinit var recyclerChat: RecyclerView
    lateinit var ivSend: ImageView
    lateinit var etMessage: EditText
    lateinit var mainScroll: NestedScrollView

    private lateinit var topicsAdapter: TopicsAdapter
    private val chatSession: ChatSession = ChatSession()

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var toolbarGuest: androidx.appcompat.widget.Toolbar

    private var isAuthorize = true;
    private var firstOpen = true;

    private var textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            ivSend.visibility = (!s.toString().isNullOrEmpty()).visibility()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_support, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        toolbar = view.findViewById(R.id.toolbar)
        toolbarGuest = view.findViewById(R.id.toolbar_guest)
        toolbar.visibility = isAuthorize.visibility()
        toolbarGuest.visibility = (!isAuthorize).visibility()

        arrayList = ArrayList(listOf(
            getString(R.string.support_t1),
            getString(R.string.support_t2),
            getString(R.string.support_t3),
            getString(R.string.support_t4),
            getString(R.string.support_t5)
        ))

        topicsAdapter = TopicsAdapter(arrayList)

        ivBack = view.findViewById(R.id.ivBack)
        recyclerTopics = view.findViewById(R.id.recyclerTopics)
        recyclerChat = view.findViewById(R.id.recyclerChat)
        ivSend = view.findViewById(R.id.ivSend)
        etMessage = view.findViewById(R.id.etMessage)
        mainScroll = view.findViewById(R.id.mainScroll)

        chatSession.delegate = this
        topicsAdapter.delegate = this

        etMessage.addTextChangedListener(textWatcher)

        if (!isAuthorize) {
            ivBack.isEnabled = false
            ivBack.visibility = View.GONE
        }
        ivBack.setOnClickListener(this::back)

        val layoutManager = FlexboxLayoutManager(mainActivity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_END
        recyclerTopics.layoutManager = layoutManager
        recyclerTopics.adapter = topicsAdapter

        recyclerChat.adapter = chatSession.getAdapter()

        (recyclerChat.layoutManager as? LinearLayoutManager)?.let { lm ->
            lm.stackFromEnd = true
        }

        ivSend.setOnClickListener {
            val content = etMessage.text
            if (content.isNotEmpty()) {
                chatSession.sendMessage(content.toString())
                etMessage.text.clear()

                scrollToBottom()
//                mainScroll.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }

//        updateNetworkState()
//        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
//        connectivityManager?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
//            override fun onCapabilitiesChanged(
//                network: Network,
//                networkCapabilities: NetworkCapabilities
//            ) {
//                activity?.runOnUiThread {
//                    updateNetworkState()
//                }
//            }
//
//            override fun onLost(network: Network) {
//                super.onLost(network)
//                activity?.runOnUiThread {
//                    updateNetworkState()
//                }
//            }
//        })

        if (isAuthorize) {
            chatSession.loadDataFromDB()
        } else {
            if (firstOpen) {
                firstOpen = false
                chatSession.loadDataFromDB()
            }
        }
    }

//    @UiThread
//    private fun updateNetworkState() {
//        if (NetworkUtils.isOnline(requireContext())) {
//            tvTitle.text = getString(R.string.toolbar_support)
//            tvSubTitle.text = getString(R.string.toolbar_support_secondary)
//            tvSubTitle.visibility = true.visibility()
//        } else {
//            tvTitle.text = getString(R.string.toolbar_support_no_internet)
//            tvSubTitle.visibility = false.visibility()
//        }
//    }


    //  CHAT SESSION DELEGATE
    override fun onNewMessage() {
//        recyclerChat.scrollToPosition(chatSession.getItemsCount() - 1)
//        mainScroll.fullScroll(ScrollView.FOCUS_DOWN)
        scrollToBottom()
    }

    override fun updateTopics(topics: ArrayList<String>) {
        topicsAdapter.arrayList.clear()
        topicsAdapter.arrayList.addAll(topics)
        topicsAdapter.notifyDataSetChanged()

        recyclerTopics.visibility = true.visibility()
    }

    override fun scrollToDown() {
        scrollToBottom()
    }

    override fun getBackgroundColor(): Int {
        if (isAuthorize) return resources.getColor(R.color.color_pressed_button, null)
        else return Color.parseColor("#F7F7F7")
    }

    //  TOPIC ADAPTER DELEGATE
    override fun onTopicSelect(topic: String) {
        chatSession.sendMessage(topic)
//        recyclerTopics.visibility = false.visibility()
    }

    fun back(view: View) {
        navigationPresenter.popFragment()
    }

    override fun onResume() {
        if (isAuthorize) (mainActivity as MainActivity).setBottomNavigationVisibility(false)
        super.onResume()
    }

    override fun onPause() {
        if (isAuthorize) (mainActivity as MainActivity).setBottomNavigationVisibility(true)
        super.onPause()
    }

    fun scrollToBottom() {
        mainScroll.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val scrollViewHeight: Int = mainScroll.getHeight()
                    if (scrollViewHeight > 0) {
                        mainScroll.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                        val lastView: View =
                            mainScroll.getChildAt(mainScroll.getChildCount() - 1)
                        val lastViewBottom: Int =
                            lastView.bottom + mainScroll.getPaddingBottom()
                        val deltaScrollY: Int =
                            lastViewBottom - scrollViewHeight - mainScroll.getScrollY()
                        /* If you want to see the scroll animation, call this. */mainScroll.smoothScrollBy(
                            0,
                            deltaScrollY
                        )
                        /* If you don't want, call this. */mainScroll.scrollBy(
                            0,
                            deltaScrollY
                        )
                    }
                }
            })
    }

    companion object {
        fun newInstance(auth: Boolean = true): BaseFragment {
            return SupportFragment().apply {
                isAuthorize = auth
            }
        }
    }
}