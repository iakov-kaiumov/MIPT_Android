package dev.phystech.mipt.ui.fragments.services.notifications

import android.graphics.Rect
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.NotificationTimeListAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.notification.Alarm

class NotificationsFragment : BaseFragment(), NotificationTimeListAdapter.Delegate {

    lateinit var notificationsAdapter: NotificationTimeListAdapter
    lateinit var recyclerNotifications: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        val ivBack: ImageView = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            mainActivity.supportFragmentManager.popBackStack();
        }

        val timeItems = ArrayList(resources.getStringArray(R.array.schedulers_times_titles).toMutableList())
        val selectedNotificationPositionKey = resources.getString(R.string.pref_notification_selected_item_key)
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val pos = pref.getInt(selectedNotificationPositionKey, 0)

        notificationsAdapter = NotificationTimeListAdapter(timeItems, pos)
        notificationsAdapter.delegate = this
        recyclerNotifications = view.findViewById(R.id.recyclerNotificationList)

        recyclerNotifications.addItemDecoration(
                object : DividerItemDecoration(context, VERTICAL) {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildAdapterPosition(view)
                        if (position == state.itemCount - 1) {
                            outRect.setEmpty()
                        } else {
                            super.getItemOffsets(outRect, view, parent, state)
                        }
                    }
                }
        )

        recyclerNotifications.adapter = notificationsAdapter
    }

    override fun selectItem(withPosition: Int) {
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