package dev.phystech.mipt.ui.fragments.support.chats_groups

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ChatsAdapter
import dev.phystech.mipt.adapters.ChatsGroupsAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType
import dev.phystech.mipt.ui.fragments.support.chat.groups.GroupsChatFragment
import dev.phystech.mipt.ui.fragments.support.chats.ChatsContract

class ChatsGroupsFragment: BaseFragment(), ChatsContract.View {

    private var presenter: ChatsContract.Presenter? = null

    private lateinit var ivBack: ImageView
    private lateinit var etSearch: TextInputEditText
    private lateinit var progress: ProgressBar
    private lateinit var rvChats: RecyclerView

    private var textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (rvChats.adapter != null)
                (rvChats.adapter as ChatsGroupsAdapter).updateFilter(s.toString().trim())
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    //  ANDROID LIFE CIRCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ChatsGroupsPresenter(resources, bottomSheetController)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats_groups, container, false);
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }


    //  MVP BASE VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        etSearch = view.findViewById(R.id.etName)
        progress = view.findViewById(R.id.progress)
        rvChats = view.findViewById(R.id.chatsRecycler)

        ivBack.setOnClickListener(this::back)
        etSearch.addTextChangedListener(textWatcher)

        (activity as MainActivity).loadChats(single = false, group = true)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    /***    MVP VIEW
     * @see EventAddContract.View
     */
    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    override fun showLoader(visible: Boolean) {
        progress.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {

        (adapter as ChatsGroupsAdapter).onClickListener = ChatsGroupsAdapter.OnClickListener {
            navigate(GroupsChatFragment.newInstance(it.second))
        }

        rvChats.adapter = adapter
    }

    //  EVENTS

    private fun back(view: View) {
        navigationPresenter.popFragment()
    }

    companion object {
        fun newInstance(): BaseFragment {
            val fragment = ChatsGroupsFragment()
            return fragment
        }
    }
}