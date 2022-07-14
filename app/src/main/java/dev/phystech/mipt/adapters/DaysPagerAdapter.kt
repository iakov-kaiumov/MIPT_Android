package dev.phystech.mipt.adapters

import android.content.Context
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import dev.phystech.mipt.R
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerListShortItem
import dev.phystech.mipt.ui.fragments.DayFragment
import dev.phystech.mipt.ui.utils.Notifyable


class DaysPagerAdapter(
    private val timetable: ArrayList<SchedulerListShortItem>,
    private val timetableEvents: ArrayList<SchedulerListShortItem>,
    private val context: Context,
    fm: FragmentManager,
    private val evenOddCurrDate: Int,
    private val startWeekDay: Long,
    private val endWeekDay: Long
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT), ClassesAdapter.Delegate {

    val map: HashMap<Int, DayFragment> = HashMap()
    var delegate: ClassesAdapter.Delegate? = null

    override fun getItem(position: Int): Fragment {
        if (map.containsKey(position)) {
            return map[position] ?: DayFragment.new(
                position + 1,
                ArrayList(arrayListOf(timetable, timetableEvents).flatMap { v -> v }),
                evenOddCurrDate,
                startWeekDay,
                endWeekDay
            )
        }

        map[position] = DayFragment.new(
            position + 1,
            ArrayList(arrayListOf(timetable, timetableEvents).flatMap { v -> v }),
            evenOddCurrDate,
            startWeekDay,
            endWeekDay
        ).apply { delegate = this@DaysPagerAdapter }
        return map[position]!!
    }

    override fun getCount(): Int = 7

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getStringArray(R.array.week_short)[position].toString()
    }

    override fun getItemPosition(fragment: Any): Int {
        if (fragment is Notifyable) {
            fragment.notifySomeUpdated()
        }

        return super.getItemPosition(fragment)
    }


    override fun saveState(): Parcelable? {
        return null
    }

    override fun onClick(item: ScheduleItem) {
        delegate?.onClick(item)
    }

    override fun onClick(item: EventModel) {
        delegate?.onClick(item)
    }
}