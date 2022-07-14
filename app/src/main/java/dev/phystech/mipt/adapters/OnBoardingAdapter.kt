package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R

class OnBoardingAdapter: RecyclerView.Adapter<OnBoardingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = OnBoarding.getByPosition(viewType).getViewId()
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}

    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int = position


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)

}

enum class OnBoarding(position: Int) {
//    Actions(0),
//    Eventd(1),
//    Editing(2),
//    Sharing(3),
//    Notifications(4);

    ChatsWelcom(0),
    ChatsRules(1),
    ChatsNotifications(2);


    fun getViewId(): Int {
        return when (this) {
//            Actions -> R.layout.fragment_onboarding_1
//            Eventd -> R.layout.fragment_onboarding_2
//            Editing -> R.layout.fragment_onboarding_3
//            Sharing -> R.layout.fragment_onboarding_4
//            Notifications -> R.layout.fragment_onboarding_5

            ChatsWelcom -> R.layout.fragment_onboarding_chats_1
            ChatsRules -> R.layout.fragment_onboarding_chats_2
            ChatsNotifications -> R.layout.fragment_onboarding_chats_3
        }
    }

    companion object {
        fun getByPosition(position: Int): OnBoarding {
            return when (position) {
//                0 -> Actions
//                1 -> Eventd
//                2 -> Editing
//                3 -> Sharing
//                4 -> Notifications

                0 -> ChatsWelcom
                1 -> ChatsRules
                2 -> ChatsNotifications

                else -> throw Exception()
            }
        }
    }
}