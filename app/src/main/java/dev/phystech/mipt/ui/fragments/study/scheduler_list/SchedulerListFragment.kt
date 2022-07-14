package dev.phystech.mipt.ui.fragments.study.scheduler_list

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.SchedulerFiltredListAdapter
import dev.phystech.mipt.adapters.scheduler_filtred.FilterAdapterModelHelper
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.SchedulerFilterModel
import dev.phystech.mipt.models.SchedulerIndex
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.ui.fragments.study.ScheduleItemDetailFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditFragment
import dev.phystech.mipt.ui.utils.SchedulerDetailScreenMode
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/** Список занятий
 * Используется при создании добавлении занятия, изменении текущего
 *
 * Создание экземпляра: [SchedulerListFragment.newInstance]
 * Поля:
 * * course - курс, занятия которого нужно отобразить
 * * toRemoveSchedulerId - id занятия, которое нужно будет удалить,
 *                         при успешном добавлении нового занятия
 *
 * При выборе занятия запускается экран [ScheduleItemDetailFragment]
 */
class SchedulerListFragment: BaseFragment(), SchedulerFiltredListAdapter.Delegate {

    private lateinit var mainRecycler: RecyclerView
    private lateinit var etName: EditText
    private lateinit var progress: ProgressBar
    private lateinit var ivBack: ImageView
    private var course: String? = null
    private var toRemoveSchedulerId: Int? = null

    val nameFilterField: BehaviorSubject<String> = BehaviorSubject.create()
    val adapter = SchedulerFiltredListAdapter()
    var lastFilter: FilterAdapterModelHelper = FilterAdapterModelHelper()
    var requestDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        course = arguments?.getString(KEY_COURSE)
        toRemoveSchedulerId = arguments?.getInt(KEY_SCHEDULER_ID, -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scheduler_list, container, false)

        return view
    }

    override fun bindView(view: View?) {
        if (view == null) return

        mainRecycler = view.findViewById(R.id.mainRecycler)
        etName = view.findViewById(R.id.etName)
        progress = view.findViewById(R.id.progress)
        ivBack = view.findViewById(R.id.ivBack)

        progress.visibility = View.GONE

        adapter.delegate = this
        adapter.setFilter(lastFilter)
        mainRecycler.adapter = adapter

        etName.addTextChangedListener {
            nameFilterField.onNext(it.toString())
        }

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

        nameFilterField
            .debounce(100, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                lastFilter.model.name = it
                val model = lastFilter.model
                updateData(model)
            })

        updateData()
    }



    //  SCHEDULER FILTER DELEGATE
    override fun onUpdateFilter(filter: FilterAdapterModelHelper) {
        filter.model.name = lastFilter.model.name
        lastFilter = filter
        val model = filter.model

        updateData(model)
    }

    override fun createScheduler(model: SchedulerIndex) {
        hideKeyboard()
        val secret = model.urls?.findSecret() ?: return
        val id = model.id.toString()

        showProgress()
        SchedulersRepository.shared.getSchedulerItemById(model.id) {
            hideProgress()
            it ?: return@getSchedulerItemById

            val fragment = ScheduleItemDetailFragment(it, SchedulerDetailScreenMode.Add, toRemoveSchedulerId)
            navigationPresenter.pushFragment(fragment, true)
        }

    }

    override fun createNewScheduler() {
        navigationPresenter.popFragment()
        navigationPresenter.pushFragment(SchedulerEditFragment.newInstance(null, false, SchedulerEditFragment.Type.Create), true)
    }


    // OTHER
    private fun updateData(model: SchedulerFilterModel? = null) {
        if (requestDisposable?.isDisposed?.not() == true) {
            requestDisposable?.dispose()
        }

        progress.visibility = View.VISIBLE
        requestDisposable = ApiClient.shared.getScheduleIndex(
            hour = model?.timeBegin,
            type = model?.schedulerType,
            name = model?.name,
            teachers = model?.lector,
            places = model?.auditory,
            weekday = model?.dayOfWeek,
            courseId = course?.toIntOrNull()
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                progress.visibility = View.GONE
                if (it.status.isSuccess) {
                    it.data?.paginator?.let {
                        adapter.setItems(it)
                    }
                }
            }, {
                progress.visibility = View.GONE
                print(it)
            })
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }







    companion object {
        const val KEY_COURSE = "key.filter_value.course"
        const val KEY_SCHEDULER_ID = "key.filter_value.schduler_id"

        fun newInstance(course: String? = null, toRemoveSchedulerId: Int? = null): BaseFragment {
            return SchedulerListFragment().apply {
                arguments = bundleOf(
                    KEY_COURSE to course,
                    KEY_SCHEDULER_ID to toRemoveSchedulerId
                )
            }
        }
    }
}
