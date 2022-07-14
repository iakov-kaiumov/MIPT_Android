package dev.phystech.mipt.ui.fragments.study.teachers_list

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.shuhart.stickyheader.StickyAdapter
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.TeachersAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.repositories.TeachersRepository
import dev.phystech.mipt.utils.visibility
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

/** Экран со списком преподавателей с фильтром
 *  При выборе преподавателя отправляется [LocalBroadcastManager] и окно закрывается
 */
class TeachersListFragment: BaseFragment(), TeachersAdapter.Delegate {

    lateinit var mainRecycler: RecyclerView
    lateinit var ivBack: ImageView
    lateinit var etName: EditText
    lateinit var tvTitle: TextView
    lateinit var tvTeacherNotFound: TextView
    lateinit var rlAddTeacher: RelativeLayout
    lateinit var notFoundLayout: View


    private val adapter: TeachersAdapter = TeachersAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scheduler_list, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        mainRecycler = view.findViewById(R.id.mainRecycler)
        ivBack = view.findViewById(R.id.ivBack)
        etName = view.findViewById(R.id.etName)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvTeacherNotFound = view.findViewById(R.id.tvTeacherNotFound)
        rlAddTeacher = view.findViewById(R.id.rlAddTeacher)
        notFoundLayout = view.findViewById(R.id.notFoundLayout)
        tvTitle.setText(R.string.teachers)

        mainRecycler.adapter = adapter
        StickyHeaderItemDecorator(adapter).attachToRecyclerView(mainRecycler)
        adapter.delegate = this

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

        TeachersRepository.shared.teachers
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                adapter.setItems(it)
                adapter.notifyDataSetChanged()
            }, {
                print(it)
            })

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.setFilter(s.toString())
                tvTeacherNotFound.text = resources.getString(R.string.some_teacher_not_found, s.toString())
            }
        })

        rlAddTeacher.setOnClickListener {
            val intent = Intent(ACTION)
            intent.putExtra(DATA_FIELD, etName.text.toString())

            LocalBroadcastManager
                .getInstance(requireContext())
                .sendBroadcast(intent)

            navigationPresenter.popFragment()
        }
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }



    //  ADAPTER DELEGATE
    override fun onTeacherSelect(model: Teacher) {
        val intent = Intent(ACTION)
        intent.putExtra(DATA_ID, model.id)

        LocalBroadcastManager.getInstance(requireContext())
            .sendBroadcast(intent)

        navigationPresenter.popFragment()
    }

    override fun updateDisplayedCount(count: Int) {
        notFoundLayout.visibility = (count == 0 && etName.text.isNotBlank()).visibility()
    }



    companion object {
        const val ACTION = "TEACHER_SELECT"
        const val DATA_ID = "teacher_id"
        const val DATA_FIELD = "teacher_field"

    }
}