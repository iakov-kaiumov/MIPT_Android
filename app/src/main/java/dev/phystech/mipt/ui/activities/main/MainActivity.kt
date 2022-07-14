package dev.phystech.mipt.ui.activities.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.messaging.FirebaseMessaging
import dev.phystech.mipt.LocationServiceWrap
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ChairSettingsAdapter
import dev.phystech.mipt.adapters.SelectorAdapter
import dev.phystech.mipt.base.BaseActivity
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.base.interfaces.FragmentNavigation
import dev.phystech.mipt.base.utils.NavigationManager
import dev.phystech.mipt.helpers.EventsBottomSheetController
import dev.phystech.mipt.helpers.SchedulersBottomSheetController
import dev.phystech.mipt.repositories.*
import dev.phystech.mipt.ui.fragments.navigator.NavigatorFragment
import dev.phystech.mipt.ui.fragments.news.NewsFragment
import dev.phystech.mipt.ui.fragments.plugs.PlugAcademFragment
import dev.phystech.mipt.ui.fragments.plugs.PlugDeletedFragment
import dev.phystech.mipt.ui.fragments.services.ServicesFragment
import dev.phystech.mipt.ui.fragments.services.feedback.FeedbackFragment
import dev.phystech.mipt.ui.fragments.services.guest.ServicesGuestFragment
import dev.phystech.mipt.ui.fragments.study.ScheduleItemDetailFragment
import dev.phystech.mipt.ui.fragments.support.SupportFragment
import dev.phystech.mipt.ui.fragments.study.StudyFragment
import dev.phystech.mipt.ui.fragments.study.add_event.EventAddFragment
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType
import dev.phystech.mipt.ui.fragments.study.events.EventsFragment
import dev.phystech.mipt.ui.fragments.study.guest.SchedulersGuestFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditPresenter
import dev.phystech.mipt.ui.fragments.study.scheduler_event_detail.SchedulerEventDetailFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_list.SchedulerListFragment
import dev.phystech.mipt.ui.fragments.study.user_scheduler_list.UserSchedulerListFragment
import dev.phystech.mipt.ui.fragments.support.chats.ChatsFragment
import dev.phystech.mipt.ui.utils.SchedulerDetailScreenMode
import dev.phystech.mipt.utils.BackgroundDelegate
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.UserRole
import dev.phystech.mipt.utils.UserRole.*
import dev.phystech.mipt.utils.visibility
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDateTime
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.*
import com.google.gson.Gson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dev.phystech.mipt.models.User
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.models.api.Message
import dev.phystech.mipt.models.api.SocketConversatiomMessageModel
import dev.phystech.mipt.notification.MyFirebaseMessagingService
import dev.phystech.mipt.ui.activities.WebViewActivity
import dev.phystech.mipt.ui.activities.auth_webview.AuthorizationWebViewActivity
import dev.phystech.mipt.ui.activities.authorization.AuthorizationActivity
import dev.phystech.mipt.ui.fragments.support.chat.groups.GroupsChatFragment
import dev.phystech.mipt.ui.fragments.support.chat.single.ChatFragment
import dev.phystech.mipt.ui.fragments.support.chats_groups.ChatsGroupsFragment
import dev.phystech.mipt.utils.ChatUtils.WEB_SOCKET_URL
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class MainActivity : BaseActivity(), FragmentNavigation.Presenter, BackgroundDelegate,
    EventsBottomSheetController.Delegate, SchedulersBottomSheetController.Delegate, BottomSheetController {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var container: FrameLayout
//    private lateinit var bottomSheetRoot: ConstraintLayout


    private var firstFragment: BaseFragment = StudyFragment()
    private val newsFragment: BaseFragment by lazy { NewsFragment() }
    private var chatFragment: BaseFragment = ChatsFragment()
    private val navigatorFragment: BaseFragment by lazy { NavigatorFragment() }
    private var servicesFragment: BaseFragment = ServicesFragment()

    private var adapter: ChairSettingsAdapter = ChairSettingsAdapter(ArrayList())
    lateinit var behaviorEvents: BottomSheetBehavior<View>
    lateinit var behaviorSchedulers: BottomSheetBehavior<View>
    lateinit var behaviorSelector: BottomSheetBehavior<View>
    lateinit var backView: View
    lateinit var recyclerViewSelector: RecyclerView
    private var selectorAdapter = SelectorAdapter<SchedulerEditPresenter.StringID>()

    private var eventId: Int? = null
    private var schedulerId: Int? = null

    private var statusBarHeight = 0

    var convId: Long? = null
    var convIdGroup: Long? = null

    var timerGetDialogs: CountDownTimer? = null

    private var singleChatUnreadcount: Int = 0
    private var groupsChatUnreadcount: Int = 0

    var singleChatsDisposable: Disposable? = null
    var groupsChatsDisposable: Disposable? = null

    private var webSocketClient: WebSocketClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent.dataString?.let {
            Log.i("DEEPLINK", it)

            Regex("https://app\\.mipt\\.ru/schedule-event/subscribe/(\\d+)\\p{all}*", RegexOption.DOT_MATCHES_ALL).let { reg ->
                val matches = reg.matchEntire(it)
                matches?.groupValues?.firstOrNull{ v -> v.toIntOrNull() != null }?.let {
                    Log.i("DEEPLINK", "Scheduler-event id: ${it}")
                    eventId = it.toInt()
                }
            }

            Regex("https://app\\.mipt\\.ru/schedule/subscribe/(\\d+)\\p{all}*", RegexOption.DOT_MATCHES_ALL).let { reg ->
                val matches = reg.matchEntire(it)
                matches?.groupValues?.firstOrNull{ v -> v.toIntOrNull() != null }?.let {
                    Log.i("DEEPLINK", "Scheduler id: ${it}")
                    schedulerId = it.toInt()
                }
            }

        }

        if (UserRepository.shared.getToken() != null && UserRepository.shared.userRole != null) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener {}
                .addOnFailureListener { e: Exception? -> }
                .addOnCanceledListener {}
                .addOnCompleteListener { task: Task<String> ->
                    UserRepository.shared.sendDeviceToken(task.result)
                }
        }

        container = findViewById(R.id.container)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener(this::onMenuItemSelected)

        setTransparentStatusBar()

        when (UserRepository.shared.userRole) {
            Guest -> {
//                MenuInflater(this).inflate(R.menu.guest, bottomNavigation.menu)
                firstFragment = SchedulersGuestFragment()
                servicesFragment = ServicesGuestFragment()
                chatFragment = SupportFragment.newInstance(auth = false)
            }
            Student -> {
//                MenuInflater(this).inflate(R.menu.main, bottomNavigation.menu)
            }
            Employee -> {
//                MenuInflater(this).inflate(R.menu.main, bottomNavigation.menu)
            }
        }

        var disposable: Disposable? = null
        disposable = UserRepository.shared.userInfoRx.subscribe({
            disposable?.dispose()
            if (it?.roles?.contains(UserRole.Alumnus.getName()) == true) {
                firstFragment = PlugDeletedFragment()
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                pushFragment(firstFragment, false)
            } else if (it?.roles?.contains(UserRole.Expelled.getName()) == true) {
                firstFragment = PlugDeletedFragment()
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                pushFragment(firstFragment, false)
            } else if (it?.roles?.contains(UserRole.Academ.getName()) == true) {
                firstFragment = PlugAcademFragment()
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                pushFragment(firstFragment, false)
            }


        }, {
            disposable?.dispose()
        })

        UserRepository.shared.getUserId()?.let { userId ->
            ChatUtils.userId = userId
        }

        ChatRepository.shared.getConversationsDB()
        TagRepository.shared.loadAll()
        NewsRepository.shared.loadData()
        NewsRepository2.shared.loadData()
        ChairRepository.shared.loadData()
        EventRepository.shared.loadData()
        PlacesRepository.shared.loadData()
        HistoryRepository.shared.loadData()
        ContactsRepository.shared.loadData()
//        ContactsRepository.shared.loadDeletedData()
        ChairTopicRepository.shared.loadData()
        TeachersRepository.shared.loadData()
        TimeSlotsRepository.shared.load()
        SchedulePlaceRepository.shared.loadData()
        ScheduleEventRepository.shared.loadData()
        SportSectionsRepository.shared.loadData()

        pushFragment(firstFragment, false)

        setPushData()
        setupChatsobservers()

        createTimer()

        ChairTopicRepository.shared.chairs
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                updateBottomSheetItems()
            }

        backView = findViewById(R.id.backView)
        val bottomSheetEvents = findViewById<View>(R.id.bottomSheetEvents)
        val bottomSheetSchedulers = findViewById<View>(R.id.bottomSheetSchedulers)
        val bottomSheetSelector = findViewById<View>(R.id.bottomSheetSelector)
        behaviorEvents = BottomSheetBehavior.from(bottomSheetEvents)
        behaviorSchedulers = BottomSheetBehavior.from(bottomSheetSchedulers)
        behaviorSelector = BottomSheetBehavior.from(bottomSheetSelector)

        behaviorEvents.isHideable = true
        behaviorEvents.isDraggable = true
        behaviorEvents.isFitToContents = true
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN

        behaviorSchedulers.isHideable = true
        behaviorSchedulers.isDraggable = true
        behaviorSchedulers.isFitToContents = true
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN

        behaviorSelector.isHideable = true
        behaviorSelector.isDraggable = true
        behaviorSelector.isFitToContents = true
        behaviorSelector.state = BottomSheetBehavior.STATE_HIDDEN

        EventsBottomSheetController(bottomSheetEvents).delegate = this
        SchedulersBottomSheetController(bottomSheetSchedulers).delegate = this

        behaviorEvents.addBottomSheetCallback(bottomSheetCallback)
        behaviorSchedulers.addBottomSheetCallback(bottomSheetCallback)
//        behaviorSelector.addBottomSheetCallback(bottomSheetCallback)

        recyclerViewSelector = bottomSheetSelector.findViewById<RecyclerView>(R.id.rvSelector)

        backView.setOnClickListener {
            behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
            behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN
            behaviorSelector.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val breakSwitch = bottomSheetSchedulers.findViewById<SwitchMaterial>(R.id.switchBreak)
        val spacesEnabledKey = resources.getString(R.string.pref_show_spaces)

        val eventSwitch = bottomSheetSchedulers.findViewById<SwitchMaterial>(R.id.switchEvents)
        val eventsEnabledKey = resources.getString(R.string.pref_show_events)

        breakSwitch.isChecked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(spacesEnabledKey, true)
        eventSwitch.isChecked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(eventsEnabledKey, true)

        breakSwitch.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(spacesEnabledKey, isChecked)
                .apply()

            val notifyIntent = Intent().apply { action = "notification" }
            LocalBroadcastManager.getInstance(this).sendBroadcast(notifyIntent)
        }

        eventSwitch.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(eventsEnabledKey, isChecked)
                .apply()

            val notifyIntent = Intent().apply { action = "notification" }
            LocalBroadcastManager.getInstance(this).sendBroadcast(notifyIntent)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            (supportFragmentManager.fragments.lastOrNull() as? BaseFragment)?.let {
                setStatusBarColor(it.backgroundColor)
            }
        }

        container.doOnLayout {
//            pushFragment(firstFragment, false)

            eventId?.let {
                val fragment = SchedulerEventDetailFragment.newInstance(it)
                add(fragment)
            }
            schedulerId?.let {
                showProgress()
                SchedulersRepository.shared.getSchedulerItemById(it) {
                    hideProgress()
                    it?.let { schedulerItem ->
                        var disposable: Disposable? = null
                        showProgress()
                        disposable = SchedulersRepository.shared.schedulers
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                hideProgress()
                                disposable?.dispose()
                                val fragment = if (it.firstOrNull { v -> v.id == schedulerId } == null) {
                                        ScheduleItemDetailFragment(schedulerItem, SchedulerDetailScreenMode.Add)
                                    } else {
                                        ScheduleItemDetailFragment(schedulerItem)
                                    }

                                add(fragment)

                            }, {
                                hideProgress()
                                val fragment = ScheduleItemDetailFragment(schedulerItem, SchedulerDetailScreenMode.Add)
                                add(fragment)
                                disposable?.dispose()
                            })

                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initWebSocket()
    }

    override fun onPause() {
        super.onPause()
        webSocketClient?.close()
    }

    private fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_item_study -> {
                pushFragment(firstFragment, false)
            }
            R.id.menu_item_news -> {
                pushFragment(newsFragment, false)
            }
            R.id.menu_item_chat -> {
                pushFragment(chatFragment, false)
            }
            R.id.menu_item_navigator -> {
                pushFragment(navigatorFragment, false)
            }
            R.id.menu_item_services -> {
                pushFragment(servicesFragment, false)
            }
        }

        setStatusBarTransparency(false)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LocationServiceWrap.REQUEST_CODE) {
            LocationServiceWrap.shared.startListen(true)
        }
    }


    //  NAVIGATION PRESENTER
    override fun popFragment() {
        supportFragmentManager.popBackStack()
    }

    override fun showStudy() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        pushFragment(firstFragment, false)
    }

    override fun showNews() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        pushFragment(newsFragment, false)
    }

    override fun showSupport() {
        val fragment = FeedbackFragment()
        pushFragment(fragment, true)
    }



    override fun pushFragment(fragment: BaseFragment?, is_add: Boolean) {
        if (fragment == null) return
        fragment.attachNavigationPresenter(this)
        fragment.attachBottomSheetContoller(this)
        fragment.setBackgroundDelegate(this)
        NavigationManager.replaceFragment(this, fragment, R.id.container, is_add)

//        container.setBackgroundColor(fragment.backgroundColor)
    }

    override fun add(fragment: BaseFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.addToBackStack(fragment.toString())
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        fragment.attachNavigationPresenter(this)
        fragment.attachBottomSheetContoller(this)
        fragment.setBackgroundDelegate(this)

        transaction.commit()
    }

    override fun setStatusBarTransparency(enabled: Boolean) {
        container.doOnPreDraw {
            if (enabled) {
                container.setPadding(0, 0, 0, 0)
            } else {
                container.setPadding(0, statusBarHeight, 0, 0)
            }
        }

        container.doOnLayout {
            if (enabled) {
                container.setPadding(0, 0, 0, 0)
            } else {
                container.setPadding(0, statusBarHeight, 0, 0)
            }
        }

        if (enabled) {
            container.setPadding(0, 0, 0, 0)
        } else {
            container.setPadding(0, statusBarHeight, 0, 0)
        }
    }

    override fun setBottomNavigationVisibility(isVisible: Boolean) {
        bottomNavigation.visibility = isVisible.visibility()
//        bottomNavigation.visibility = true.visibility()
    }


    //  CONFIGURE BOTTOM SHEET
    fun configBottomSheet() {
//        val recycler = bottomSheetRoot.findViewById<RecyclerView>(R.id.recycler)
//        recycler.adapter = adapter

//        updateBottomSheetItems()
    }

    fun updateBottomSheetItems() {
//        adapter.items.clear()
//        ChairTopicRepository.shared.chairs.value.let {
//            adapter.items.addAll(it)
//        }
//
//        adapter.notifyDataSetChanged()

    }


    //  OTHERS
    private fun setTransparentStatusBar() {
        window.statusBarColor = Color.TRANSPARENT
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            statusBarHeight = insets.systemWindowInsetTop
//            setStatusBarTransparency(false)

            val newInsets = ViewCompat.onApplyWindowInsets(
                view,
                insets.replaceSystemWindowInsets(
                    0,
                    0,
                    0,
                    insets.systemWindowInsetBottom
                )
            )

            if (supportFragmentManager.backStackEntryCount <= 1) setStatusBarTransparency(false)

            return@setOnApplyWindowInsetsListener newInsets
        }
    }


    //  BACKGROUND DELEGATE
    override fun setStatusBarColor(color: Int) {
        container.setBackgroundColor(color)
    }


    /** BOTTOM SHEET DELEGATE
     *  @see    EventsBottomSheetController.Delegate
     *  @see    SchedulersBottomSheetController.Delegate
     */
    override fun clickAddLesson() {
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN

        val fragment = SchedulerListFragment.newInstance()
        pushFragment(fragment, true)
    }

    override fun clickAddEvent() {
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN

        val fragment = EventAddFragment.newInstance(AddEventType.SimpleEvent)
        pushFragment(fragment, true)
    }

    override fun clickAddDeadline() {
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN

        val fragment = UserSchedulerListFragment.newInstance(UserSchedulerListFragment.TYPE_NEW_DEADLINE)
        pushFragment(fragment, true)
    }

    override fun clickNotifyError() {
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN

        val fragment = FeedbackFragment()
        pushFragment(fragment, true)
    }

    override fun clickBackChanges() {
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun toSchedulers() {
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN

        firstFragment = StudyFragment()
        popFragment()
        pushFragment(firstFragment, false)
    }

    override fun breaksEnabled(isEnabled: Boolean) {

    }

    override fun toEvents() {
        behaviorEvents.state = BottomSheetBehavior.STATE_HIDDEN
        behaviorSchedulers.state = BottomSheetBehavior.STATE_HIDDEN

        firstFragment = EventsFragment()
        popFragment()
        pushFragment(firstFragment, false)
    }


    //  BOTTOM SHEET CONTROLLER
    override fun showEventOptions() {
        behaviorEvents.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun showSchedulersOptions() {
        behaviorSchedulers.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun <T> showSelector(items: List<T>, onSelect: ((T) -> Unit)?) {
        behaviorSelector.addBottomSheetCallback(bottomSheetCallback)

        val adapter = SelectorAdapter(items)
        adapter.delegate = object : SelectorAdapter.Delegate<T> {
            override fun onClick(item: T) {
                onSelect?.invoke(item)
                behaviorSelector.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        recyclerViewSelector.adapter = adapter
        behaviorSelector.state = BottomSheetBehavior.STATE_COLLAPSED
    }


    private fun setupChatsobservers() {
        singleChatsDisposable = ChatUtils.singleChats
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { conversations ->

                ChatRepository.shared.saveModelsInRealm(conversations)

                var conversation: Conversation? = null

                singleChatUnreadcount = 0
                for (item in conversations) {
                    convId?.let { convId ->
                        if (convId > 0 && item.id == convId) conversation = item
                    }
                    if (item.baseChatType == 0 && item.unreadcount > 0) singleChatUnreadcount += item.unreadcount
                }

                showChatItemBanner()

                convId = -1
                conversation?.let {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    bottomNavigation.selectedItemId = R.id.menu_item_chat
                    pushFragment(chatFragment, false)
                    pushFragment(ChatFragment.newInstance(it), true)
                }
            }

        groupsChatsDisposable =  ChatUtils.groupsChats
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { conversations ->

                ChatRepository.shared.saveModelsInRealm(conversations)

                var conversation: Conversation? = null

                groupsChatUnreadcount = 0
                for (item in conversations) {
                    convIdGroup?.let { convIdGroup ->
                        if (convIdGroup >= 0 && item.id == convIdGroup) conversation = item
                    }
                    if (item.unreadcount > 0) groupsChatUnreadcount += item.unreadcount
                }

                showChatItemBanner()

                convIdGroup = -1
                conversation?.let {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    bottomNavigation.selectedItemId = R.id.menu_item_chat
                    pushFragment(chatFragment, false)
                    pushFragment(ChatsGroupsFragment(), true)
                    pushFragment(GroupsChatFragment.newInstance(it), true)
                }
            }
    }

    fun calculateBadgeChatCount() {
        singleChatUnreadcount = 0
        ChatUtils.singleChats.value?.let { singleChats ->
            for (item in singleChats)
                if (item.baseChatType == 0 && item.unreadcount > 0) singleChatUnreadcount += item.unreadcount
        }

        groupsChatUnreadcount = 0
        ChatUtils.groupsChats.value?.let { groupsChats ->
            for (item in groupsChats)
                if (item.unreadcount > 0) groupsChatUnreadcount += item.unreadcount
        }

        showChatItemBanner()
    }

    override fun onDestroy() {
        timerGetDialogs?.cancel()
        timerGetDialogs = null

        singleChatsDisposable?.dispose()
        groupsChatsDisposable?.dispose()

        super.onDestroy()
    }

    fun loadChats(single: Boolean, group: Boolean) {
        if (single) {
            ChatUtils.singleChatsLoader.onNext(true)
            ChatRepository.shared.loadChats()
        }
        if (group) ChatRepository.shared.loadGroupChats()
    }

    private fun createTimer() {
        timerGetDialogs?.cancel()
        timerGetDialogs = object: CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (timerGetDialogs != null) {
                    loadChats(single = true, group = true)
                    createTimer()
                }
            }
        }
        timerGetDialogs?.start()
    }

    fun showChatItemBanner() {

        val value = singleChatUnreadcount + groupsChatUnreadcount

        val menuItemId = bottomNavigation.menu.getItem(2).itemId
        if (value > 0) {
            val badge = bottomNavigation.getOrCreateBadge(menuItemId)
            badge.backgroundColor = ContextCompat.getColor(this, R.color.badge)
            badge.number = value
        } else {
            bottomNavigation.removeBadge(menuItemId)
        }
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> {
                    backView.visibility = View.GONE
                    behaviorSelector.removeBottomSheetCallback(this)
                }
                BottomSheetBehavior.STATE_DRAGGING -> backView.visibility = View.VISIBLE
                BottomSheetBehavior.STATE_COLLAPSED -> backView.visibility = View.VISIBLE
                else -> backView.visibility = View.VISIBLE
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            backView.alpha = 1 + slideOffset
        }

    }

    private fun setPushData() {
        val bundle = intent.extras
        if (bundle != null) {

            val convid = bundle.getString(MyFirebaseMessagingService.EXT_CONV_ID)?:""
            var externalUrl = bundle.getString(MyFirebaseMessagingService.EXT_EXTERNAL_URL)?:""

            if (!externalUrl.isNullOrEmpty()) {

                val intentWebView = Intent(this, WebViewActivity::class.java)
                intentWebView.putExtra("url", externalUrl)
                startActivity(intentWebView)

            } else if (!convid.isNullOrEmpty()) {

                convId = convid.toLong()
                convIdGroup = convid.toLong()
//                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
//                supportFragmentManager.fragments.clear()
//                pushFragment(chatFragment, false)

            }
        }
    }

    private fun initWebSocket() {
        UserRepository.shared.getToken()?.let { token ->
            val coinbaseUri: URI? = URI(WEB_SOCKET_URL + token)
            createWebSocketClient(coinbaseUri)
            val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            webSocketClient?.setSocketFactory(socketFactory)
            webSocketClient?.connect()
        }
    }

    private fun createWebSocketClient(coinbaseUri: URI?) {
        webSocketClient = object : WebSocketClient(coinbaseUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("TAG_SOCKET_TEST", "onOpen")
                subscribeSocket()
            }

            override fun onMessage(message: String?) {
                Log.d("TAG_SOCKET_TEST", "onMessage")
                setUpSocketMessage(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("TAG_SOCKET_TEST", "onClose")
                unsubscribeSocket()
            }

            override fun onError(ex: Exception?) {
                Log.e("TAG_SOCKET_TEST", "onError: ${ex?.message}")

                Thread.sleep(5000)
                initWebSocket()
            }
        }
    }

    private fun subscribeSocket() {
//        webSocketClient.send(
//            "{\n" +
//                    "    \"type\": \"subscribe\",\n" +
//                    "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-EUR\"] }]\n" +
//                    "}"
//        )
    }

    private fun unsubscribeSocket() {
//        webSocketClient.send(
//            "{\n" +
//                    "    \"type\": \"unsubscribe\",\n" +
//                    "    \"channels\": [\"ticker\"]\n" +
//                    "}"
//        )
    }

    private fun setUpSocketMessage(message: String?) {
        message?.let {

            val socketMessage = Gson().fromJson(message, SocketConversatiomMessageModel::class.java)

            val convMessage = Message().apply {
                id = socketMessage.savedmessageid
                useridfrom = socketMessage.userfrom?.id ?: ""
                text = socketMessage.smallmessage ?: ""
                timecreated = socketMessage.timecreated ?: 0
                user = socketMessage.userfrom

                conversationid = socketMessage.convid
            }

            if (convMessage.id != null && convMessage.useridfrom.isNotEmpty() && convMessage.text.isNotEmpty() && convMessage.timecreated > 0 && convMessage.conversationid != null)
                runOnUiThread {

                    ChatUtils.newMessage.onNext(convMessage)

                    var currentConversation: Conversation? = null

                    ChatUtils.singleChats.value?.let { singleChats ->
                        currentConversation = singleChats.find { list -> list.id == convMessage.conversationid }
                    }

                    if (currentConversation == null) {
                        ChatUtils.groupsChats.value?.let { groupsChats ->
                            currentConversation = groupsChats.find { list -> list.id == convMessage.conversationid }
                        }
                    }

                    if (currentConversation != null) {

                        currentConversation!!.messages.add(convMessage)
                        currentConversation!!.unreadcount ++
                        when(currentConversation!!.type) {
                            1L -> {
                                ChatUtils.singleChats.value?.let { singleChats ->
                                    Log.d("TAG_SOCKET_TEST", "Добавлено сообщение в один из личных чатов")
                                    ChatUtils.singleChats.onNext(singleChats)
                                }
                            }
                            2L -> {
                                ChatUtils.groupsChats.value?.let { groupsChats ->
                                    Log.d("TAG_SOCKET_TEST", "Добавлено сообщение в один из групповых чатов")
                                    ChatUtils.groupsChats.onNext(groupsChats)
                                }
                            }
                        }

                        calculateBadgeChatCount()
                    } else {
                        Log.d("TAG_SOCKET_TEST", "Чат не найдет. Обновляю все чаты")
                        loadChats(single = true, group = true)
                    }
                }
        }
    }
}