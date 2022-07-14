package dev.phystech.mipt.ui.fragments.study.user_scheduler_list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.shuhart.stickyheader.StickyAdapter
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.schedulers_weekday_adapter.SchedulerWeekdayAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.ui.fragments.study.add_event.EventAddFragment
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class UserSchedulerListFragment: BaseFragment(), SchedulerWeekdayAdapter.Delegate {

    lateinit var ivBack: ImageView
    lateinit var mainRecycler: RecyclerView
    lateinit var etName: EditText

    private lateinit var adapter: SchedulerWeekdayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = SchedulerWeekdayAdapter(resources)
        adapter.delegate = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_scheduler_list, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        mainRecycler = view.findViewById(R.id.mainRecycler)
        etName = view.findViewById(R.id.etName)

        mainRecycler.adapter = adapter
        StickyHeaderItemDecorator(adapter).attachToRecyclerView(mainRecycler)

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }


        SchedulersRepository.shared.schedulers
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                adapter.setItems(it)
            }, {

            })

    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  ADAPTER DELEGATE
    override fun onSelect(model: ScheduleItem) {
        if (arguments?.getString(KEY_TYPE) == TYPE_BROADCASE) {
            val intent = Intent(ACTION).apply {
                putExtra(DATA_ID, model.id)
            }

            LocalBroadcastManager.getInstance(requireContext())
                .sendBroadcast(intent)
            navigationPresenter.popFragment()
        } else if (arguments?.getString(KEY_TYPE) == TYPE_NEW_DEADLINE) {
            val fragment = EventAddFragment.newInstance(AddEventType.Deadline, model.id)
            navigationPresenter.pushFragment(fragment, true)
        }
    }



    companion object {
        const val ACTION = "SCHEDULER_SELECT"
        const val DATA_ID = "scheduler_id"
        const val KEY_TYPE = "user_scheduler_list.action_type"

        const val TYPE_BROADCASE = "user_scheduler_list.type.broadcast"
        const val TYPE_NEW_DEADLINE = "user_scheduler_list.type.new_deadline"


        fun newInstance(type: String): BaseFragment {
            return  UserSchedulerListFragment().apply {
                arguments = Bundle().apply {
                    putString(UserSchedulerListFragment.KEY_TYPE, type)
                }
            }

        }
    }


}