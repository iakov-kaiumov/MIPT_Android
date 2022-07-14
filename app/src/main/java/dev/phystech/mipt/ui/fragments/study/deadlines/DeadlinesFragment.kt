package dev.phystech.mipt.ui.fragments.study.deadlines

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.utils.visibility
import java.util.function.Function

/** Экран со списком дедлайнов занятия
 *
 * Создание экземпляра [DeadlinesFragment.newInstance]
 * @param scheduleId ID занятия
 */
class DeadlinesFragment: BaseFragment(), DeadlinesContract.View {

    lateinit var ivBack: ImageView
    lateinit var ivNew: ImageView
    lateinit var mainRecycler: RecyclerView
    lateinit var tvEmpty: TextView

    private var presenter: DeadlinesContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val schedulerId = arguments?.getInt(KEY_ID)
        if (schedulerId == null) {
            showMessage(R.string.scheduler_not_found)
            return
        }

        presenter = DeadlinesPresenter(schedulerId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deadlines_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }


    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        ivNew = view.findViewById(R.id.ivNew)
        mainRecycler = view.findViewById(R.id.mainRecycler)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        ivBack.setOnClickListener(this::back)
        ivNew.setOnClickListener(this::newPressed)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  EVENTS
    fun back(view: View) {
        navigationPresenter.popFragment()
    }

    fun newPressed(view: View) {
        presenter?.newDeadline()
    }


    //  MVP
    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {
        mainRecycler.adapter = adapter
    }

    override fun showEditAlert(forModel: EventModel) {
        val alertView = layoutInflater.inflate(R.layout.alert_edit_confirm, null)
        val btnEditForSelf: Button = alertView.findViewById(R.id.btnEditForSelf)
        val btnEditForGroup: Button = alertView.findViewById(R.id.btnEditForGroup)
        val btnChage: Button = alertView.findViewById(R.id.btnChage)
        val btnDelete: Button = alertView.findViewById(R.id.btnDelete)

        val alert = AlertDialog.Builder(requireContext())
            .setView(alertView)
            .create()

        btnChage.visibility = false.visibility()
        btnEditForSelf.setOnClickListener {
            presenter?.confirmEditForSelf(forModel)
            alert.cancel()
        }

        btnEditForGroup.setOnClickListener {
            presenter?.confirmEditForGroup(forModel)
            alert.cancel()
        }

        btnDelete.setOnClickListener {
            presenter?.confirmDelete(forModel)
            alert.cancel()
        }

        alert.show()

    }

    override fun showAddAlert() {
        val alertView = layoutInflater.inflate(R.layout.alert_edit_confirm, null)
        val btnEditForSelf: Button = alertView.findViewById(R.id.btnEditForSelf)
        val btnEditForGroup: Button = alertView.findViewById(R.id.btnEditForGroup)
        val btnChage: Button = alertView.findViewById(R.id.btnChage)
        val btnDelete: Button = alertView.findViewById(R.id.btnDelete)
        val tvDescription: TextView = alertView.findViewById(R.id.tvDescription)

        tvDescription.setText(R.string.new_deadline_description)

        val alert = AlertDialog.Builder(requireContext())
            .setView(alertView)
            .create()

        btnChage.visibility = false.visibility()
        btnDelete.visibility = false.visibility()

        btnEditForSelf.setOnClickListener {
            presenter?.confirmAddForSelf()
            alert.cancel()
        }

        btnEditForGroup.setOnClickListener {
            presenter?.confirmAddForGroup()
            alert.cancel()
        }

        alert.show()
    }

    override fun showEmptyListVisibility(isVisible: Boolean) {
        tvEmpty.visibility = isVisible.visibility()
    }

    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }


    companion object {
        const val KEY_ID = "deadlines.scheduler_id"

        fun newInstance(scheduleId: Int): BaseFragment {
            val fragment = DeadlinesFragment()
            val arguments = Bundle()
            arguments.putInt(KEY_ID, scheduleId)


            return fragment.apply { this.arguments = arguments }
        }
    }

}