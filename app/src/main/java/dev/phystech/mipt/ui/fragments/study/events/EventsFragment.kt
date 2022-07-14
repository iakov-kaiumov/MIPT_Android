package dev.phystech.mipt.ui.fragments.study.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.scheduler_event.SchedulerEventVHModel
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.ui.activities.main.MainActivity

/** Список событий пользователя. Используется в качестве firstFragment в [MainActivity]
 */
class EventsFragment: BaseFragment(), EventsContract.View {

    lateinit var recycler: RecyclerView
    lateinit var ivSandwitch: ImageView
    lateinit var tvHeaderTitle: TextView
    lateinit var ivCalendar: ImageView

    private lateinit var presenter: EventsContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = EventsPresenter(bottomSheetController)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }


    //  BASE VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        recycler = view.findViewById(R.id.recycler)
        ivSandwitch = view.findViewById(R.id.ivSandwitch)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)
        ivCalendar = view.findViewById(R.id.ivCalendar)

        ivSandwitch.setOnClickListener(this::onSandwitchClick)
        ivCalendar.setOnClickListener(this::onCalendarClick)
    }


    //  MVP VIEW
    override fun <VH : RecyclerView.ViewHolder, A : RecyclerView.Adapter<VH>> setAdapter(adapter: A) {
        recycler.adapter = adapter
    }

    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    override fun scrollToPosition(itemPosition: Int) {
        recycler.scrollToPosition(itemPosition)
    }


    //  EVENTS
    private fun onSandwitchClick(view: View) {
        presenter.sandwitchSelected()
    }

    private fun onCalendarClick(view: View) {
//        showProgress()
        bottomSheetController.toSchedulers()
    }
}