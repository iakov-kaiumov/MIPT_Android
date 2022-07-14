package dev.phystech.mipt.ui.fragments.study.auditory_list

import android.content.BroadcastReceiver
import android.content.Context
import dev.phystech.mipt.adapters.AuditoryAdapter
import dev.phystech.mipt.models.SchedulePlace


import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.repositories.SchedulePlaceRepository
import dev.phystech.mipt.ui.fragments.study.new_auditory.NewAuditoryFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers


/** Список аудиторий
 *
 * При выборе аудитории отправляется Notification через [LocalBroadcastManager]
 * в котором передаются ACTION {@see AuditoryListFragment.ACTION}
 * и ID выбранной аудитории {@see AuditoryListFragment.DATA_ID}
 *
 */
class AuditoryListFragment: BaseFragment(), AuditoryAdapter.Delegate {

    lateinit var mainRecycler: RecyclerView
    lateinit var ivBack: ImageView
    lateinit var tvTitle: TextView
    lateinit var etName: EditText

    private lateinit var adapter: AuditoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(receiver, IntentFilter(NewAuditoryFragment.ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = AuditoryAdapter(requireContext())
        return inflater.inflate(R.layout.fragment_scheduler_list, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        mainRecycler = view.findViewById(R.id.mainRecycler)
        ivBack = view.findViewById(R.id.ivBack)
        tvTitle = view.findViewById(R.id.tvTitle)
        etName = view.findViewById(R.id.etName)

        tvTitle.setText(R.string.auditory_title)

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.setFilter(s.toString())
            }
        })

        mainRecycler.adapter = adapter
        StickyHeaderItemDecorator(adapter).attachToRecyclerView(mainRecycler)
        adapter.delegate = this

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

        SchedulePlaceRepository.shared.places
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                adapter.setItems(it)
                adapter.notifyDataSetChanged()
            }, {
                print(it)
            })

    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  ADAPTER DELEGATE
    override fun onTeacherSelect(model: SchedulePlace) {
        val intent = Intent(ACTION)
        intent.putExtra(DATA_ID, model.id)

        LocalBroadcastManager
            .getInstance(requireContext())
            .sendBroadcast(intent)

        navigationPresenter.popFragment()
    }

    override fun newAuditory() {
        val newAuditoryFragment = NewAuditoryFragment()
        navigationPresenter.pushFragment(newAuditoryFragment, true)
    }


    //  OTHERS
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra(NewAuditoryFragment.PLACE_ID)?.let {
                val messageIntent = Intent(ACTION)
                messageIntent.putExtra(DATA_ID, it)

                LocalBroadcastManager
                    .getInstance(requireContext())
                    .sendBroadcast(messageIntent)

                navigationPresenter.popFragment()
            }
        }
    }


    companion object {
        const val ACTION = "AUDITORY_SELECT"
        const val DATA_ID = "auditory_id"

    }


}
