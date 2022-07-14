package dev.phystech.mipt.ui.fragments.users_list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.ui.fragments.users_list.UsersListDataResource.*

class UsersListFragment: BaseFragment(), UsersListContract.View {

    private lateinit var etName: EditText
    private lateinit var mainRecycler: RecyclerView
    private lateinit var ivBack: ImageView

    private var presenter: UsersListContract.Presenter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getInt(KEY_DATA_RESOURCE, -1)?.let {
            val type = UsersListDataResource.getByResource(it)
            when (type) {
                Event -> {
                    presenter = UsersListEventPresenter(arguments?.getInt(KEY_MODEL_ID, -1) ?: -1)
                }
                Schedule -> {
                    presenter = UsersListSchedulerPresenter(arguments?.getInt(KEY_MODEL_ID, -1) ?: -1)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //   BASE
    override fun bindView(view: View?) {
        if (view == null) return

        etName = view.findViewById(R.id.etName)
        mainRecycler = view.findViewById(R.id.mainRecycler)
        ivBack = view.findViewById(R.id.ivBack)

        ivBack.setOnClickListener(this::back)
        etName.addTextChangedListener(searchTextWatcher)


    }


    //  MVP
    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {
        mainRecycler.adapter = adapter
    }


    //  EVENT
    private fun back(view: View) {
        navigationPresenter.popFragment()
    }

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            presenter?.updateSearchValue(s.toString())
        }
    }

    companion object {
        const val KEY_DATA_RESOURCE = "users_list.key.data_resource"
        const val KEY_MODEL_ID = "users_list.key.model_id"

        fun newInstance(dataResource: UsersListDataResource, modelId: Int): BaseFragment {
            val fragment = UsersListFragment()
            val arguments = Bundle()
            arguments.putInt(KEY_DATA_RESOURCE, dataResource.resource)
            arguments.putInt(KEY_MODEL_ID, modelId)

            return fragment.apply { this.arguments =  arguments }
        }
    }



}

enum class UsersListDataResource(val resource: Int) {
    Event(1),
    Schedule(2);

    companion object {
        fun getByResource(value: Int): UsersListDataResource? {
            return values().firstOrNull { v -> v.resource == value }
        }
    }
}