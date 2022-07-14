package dev.phystech.mipt.ui.fragments.services.fake_login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.UsersShortAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.utils.nullIfEmpty
import dev.phystech.mipt.models.Schedule
import dev.phystech.mipt.models.api.UsersResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.repositories.*
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.visibility
import edu.phystech.iag.kaiumov.shedule.utils.DataUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.realm.Realm
import java.util.concurrent.TimeUnit

class FakeLoginFragment: BaseFragment(), UsersShortAdapter.Delegate {

    lateinit var mainRecycler: RecyclerView
    lateinit var etName: EditText
    lateinit var tvTitle: TextView
    lateinit var ivBack: ImageView
    lateinit var progress: ProgressBar
    private var filterField: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private var filterFieldDisposable: Disposable? = null
    lateinit var adapter: UsersShortAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users_list, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        mainRecycler = view.findViewById(R.id.mainRecycler)
        etName = view.findViewById(R.id.etName)
        tvTitle = view.findViewById(R.id.tvTitle)
        ivBack = view.findViewById(R.id.ivBack)
        progress = view.findViewById(R.id.progress)

        tvTitle.setText(R.string.users)

        filterFieldDisposable = filterField
            .debounce(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loadData()
            }, {})

//        loadData()

        adapter = UsersShortAdapter()
        adapter.delegate = this
        mainRecycler.adapter = adapter

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }


        etName.addTextChangedListener {
            filterField.onNext(it.toString())
//            adapter.setFilter(it.toString())
        }

    }

    private fun loadData() {
        showIndicatorProgress()
        ApiClient.shared.getUsers(filterField.value.nullIfEmpty())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                hideIndicatorProgress()
                it.data?.paginator?.map { v -> v.value }?.let {
                    adapter.setItems(it)
                    adapter.notifyItemRangeInserted(0, it.size)
                }
            }, {
                hideIndicatorProgress()
            })
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    private fun showIndicatorProgress() {
        progress.visibility = true.visibility()
    }

    private fun hideIndicatorProgress() {
        progress.visibility = false.visibility()
    }


    //  DELEGATE
    override fun onSelect(model: UsersResponseModel.UserInfoModel) {
        model.id?.toIntOrNull()?.let {
            showProgress()
//            UserRepository.shared.logout()
            UserRepository.shared.fakeLogin(it) {
                hideProgress()
                if (it) {
                    Realm.getDefaultInstance().executeTransactionAsync {
                        it.deleteAll()
                    }

                    ChatUtils.clearChats()

                    TeachersRepository.shared.preferences.edit().clear().apply()

                    DataUtils.saveSchedule(requireContext(), Schedule("", hashMapOf()))

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    showMessage(R.string.message_some_error_try_late)
                }
            }
        }

    }
}