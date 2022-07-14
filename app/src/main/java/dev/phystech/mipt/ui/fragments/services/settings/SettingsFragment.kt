package dev.phystech.mipt.ui.fragments.services.settings

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.iterator
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.notification.Alarm
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.repositories.TeachersRepository
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.activities.authorization.AuthorizationActivity
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.ui.fragments.services.fake_login.FakeLoginFragment
import dev.phystech.mipt.ui.fragments.services.notifications.NotificationsFragment
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.UserRole
import dev.phystech.mipt.utils.visibility
import io.realm.Realm

class SettingsFragment : BaseFragment() {

    lateinit var rlNotifications: RelativeLayout
    lateinit var tvNotificationValue: TextView
    lateinit var tvLogout: TextView
    lateinit var ivBack: ImageView
    lateinit var switchNotificationScheduler: SwitchCompat
    lateinit var switchNotificationChat: SwitchCompat
    lateinit var tvUsers: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    //  MVP VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        tvNotificationValue = view.findViewById(R.id.tvNotificationValue)
        rlNotifications = view.findViewById(R.id.rlNotifications)
        tvLogout = view.findViewById(R.id.tvLogout)
        ivBack = view.findViewById(R.id.ivBack)
        switchNotificationScheduler = view.findViewById(R.id.switchNotificationScheduler)
        switchNotificationChat = view.findViewById(R.id.switchNotificationChat)
        tvUsers = view.findViewById(R.id.tvUsers)


        UserRepository.shared.userInfo?.roles
            ?.contains(UserRole.Admin.getName())
            ?.or(UserRepository.shared.containFakeAccount())
            ?.visibility()?.let {
                tvUsers.visibility = it
            }

        val notificationEnabledKey = resources.getString(R.string.pref_notification_enabled_key)
        val checked = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(notificationEnabledKey, false)

        val selectedNotificationPositionKey = resources.getString(R.string.pref_notification_selected_item_key)
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val pos = pref.getInt(selectedNotificationPositionKey, 0)
        val title = resources.getStringArray(R.array.schedulers_times_titles).getOrNull(pos)


        rlNotifications.isClickable = false
        switchNotificationScheduler.isChecked = checked
        if (checked) enableNotifications() else disableNotifications()
        tvNotificationValue.text = title

        setupSwitchNotificationChat()

        //  ALERT CREATE
        val alertView = layoutInflater.inflate(R.layout.alert_scheduler_notification, null)
        val alert = AlertDialog.Builder(requireContext())
            .setView(alertView)
            .create()

        val rGroup: RadioGroup = alertView.findViewById(R.id.radioGroup)
        rGroup.children.toList().getOrNull(pos)?.id?.let { rGroup.check(it) }
        rGroup.setOnCheckedChangeListener { g, selectedId ->
            val position = g.children.indexOfFirst { v -> v.id == selectedId }
            val newTitle = resources.getStringArray(R.array.schedulers_times_titles)[position]
            tvNotificationValue.text = newTitle
            selectItem(position)
            alert.cancel()
        }

        //  EVENTS
        tvLogout.setOnClickListener {
            val loginIntent = Intent(requireContext(), AuthorizationActivity::class.java)
            UserRepository.shared.logout() {

                UserRepository.shared.clear()
                ChatUtils.clearChats()

                Realm.getDefaultInstance().executeTransactionAsync {
                    it.deleteAll()
                }
                TeachersRepository.shared.preferences.edit().clear().apply()
                startActivity(loginIntent)
                activity?.finish()
            }
        }

        rlNotifications.setOnClickListener {
            //TODO: переделать
//            (context as MainActivity).pushFragment(NotificationsFragment(), true)
            alert.show()
        }

        switchNotificationScheduler.setOnCheckedChangeListener { buttonView, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean(notificationEnabledKey, isChecked)
                .apply()

            if (isChecked) {
                enableNotifications()
            } else {
                disableNotifications()
            }
        }

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
//            mainActivity.supportFragmentManager.popBackStack();
        }

        tvUsers.setOnClickListener {
            if (UserRepository.shared.containFakeAccount()) {
                UserRepository.shared.logout() {
                    ChatUtils.clearChats()
                    UserRepository.shared.fakeLogout()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            } else {
                val fragment = FakeLoginFragment()
                navigationPresenter.pushFragment(fragment, true)
            }
        }

        if (UserRepository.shared.containFakeAccount()) {
            tvUsers.text = "Back to Admin account"
        }
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  OTHERS
    fun disableNotifications() {
        rlNotifications.isEnabled = false
        rlNotifications.isClickable = false
        rlNotifications.alpha = 0.5f
//        tvNotiifcation.setTextColor(ContextCompat.getColor(mainActivity, R.color.light_gray))
//        tvNotificationValue.setTextColor(ContextCompat.getColor(mainActivity, R.color.light_gray))
//        ivNotificationArrow.setColorFilter(ContextCompat.getColor(mainActivity, R.color.light_gray), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    fun enableNotifications() {
        rlNotifications.isEnabled = true
        rlNotifications.isClickable = true
        rlNotifications.alpha = 1f
//        tvNotiifcation.setTextColor(ContextCompat.getColor(mainActivity, R.color.black))
//        tvNotificationValue.setTextColor(ContextCompat.getColor(mainActivity, R.color.secondary_text))
//        ivNotificationArrow.setColorFilter(ContextCompat.getColor(mainActivity, R.color.secondary_text), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    fun setupSwitchNotificationChat() {
        showProgress()
        UserRepository.shared.getUserMessagePreferences() { check, error ->
            hideProgress()
            switchNotificationChat.setOnCheckedChangeListener(null)
            switchNotificationChat.isChecked = check
            switchNotificationChat.setOnCheckedChangeListener(this::switchNotificationChatOnCheckedChangeListener)
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }

        switchNotificationChat.setOnCheckedChangeListener(this::switchNotificationChatOnCheckedChangeListener)
    }

    private fun switchNotificationChatOnCheckedChangeListener(buttonView: CompoundButton , isChecked: Boolean ) {
        showProgress()
        UserRepository.shared.setUserMessagePreferences(isChecked) { check, error ->
            hideProgress()
            switchNotificationChat.setOnCheckedChangeListener(null)
            switchNotificationChat.isChecked = check
            switchNotificationChat.setOnCheckedChangeListener(this::switchNotificationChatOnCheckedChangeListener)
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    fun selectItem(withPosition: Int) {
        val selectedItemKey = resources.getString(R.string.pref_notification_selected_item_key)
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putInt(selectedItemKey, withPosition)
            .apply()

        val timeItems = ArrayList(resources.getStringArray(R.array.schedulers_times_values).toMutableList())
        val selectedItem = timeItems[withPosition]
        val notificationsBeforeKey = resources.getString(R.string.pref_notification_before_key)
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putString(notificationsBeforeKey, selectedItem)
            .apply()

        Alarm.schedule(requireContext())
    }

}