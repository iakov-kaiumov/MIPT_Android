package dev.phystech.mipt.ui.fragments.support.search_users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ChatsGroupsAdapter
import dev.phystech.mipt.adapters.ChatsUsersAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType.*
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment
import dev.phystech.mipt.ui.fragments.support.blocking_users.BlockingUsersFragment
import dev.phystech.mipt.ui.fragments.support.chats.ChatsContract

class SearchUsersFragment: BaseFragment(), SearchUsersContract.View {

    private var presenter: SearchUsersContract.Presenter? = null

    private lateinit var ivBack: ImageView
    private lateinit var ivLokedUsers: ImageView
    private lateinit var etSearch: TextInputEditText
    private lateinit var progress: ProgressBar
    private lateinit var usersRecycler: RecyclerView

    private var textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter?.searchUsers(s.toString())
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    //  ANDROID LIFE CIRCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = SearchUsersPresenter(resources, bottomSheetController)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_users, container, false);
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
        ivLokedUsers = view.findViewById(R.id.ivLokedUsers)
        etSearch = view.findViewById(R.id.etName)
        progress = view.findViewById(R.id.progress)
        usersRecycler = view.findViewById(R.id.usersRecycler)

        ivBack.setOnClickListener(this::back)
        ivLokedUsers.setOnClickListener(this::showLokedUsers)
        etSearch.addTextChangedListener(textWatcher)
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

    override fun showBlockedUsersButton(visible: Boolean) {
        ivLokedUsers.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {

        (adapter as ChatsUsersAdapter).onClickListener = ChatsUsersAdapter.OnClickListener {
            if (!it.id.isNullOrEmpty() && !it.name.isNullOrEmpty()) {

                val userData: Teacher = Teacher()
                userData.id = it.id
                userData.name = it.name
                userData.post = it.post
                userData.image = it.image
                userData.socialUrl = it.socialUrl
                userData.miptUrl = it.miptUrl
                userData.wikiUrl = it.wikiUrl

                val teacherFragment = TeacherDetailFragment.newInstance(it.id!!.toInt(), it.name, userModel = userData, roles = it.roles, lastactive = it.lastactive)
                navigate(teacherFragment)
            }
        }

        usersRecycler.adapter = adapter
    }

    //  EVENTS

    private fun back(view: View) {
        navigationPresenter.popFragment()
    }

    private fun showLokedUsers(view: View){
        navigate(BlockingUsersFragment())
    }

    companion object {
        fun newInstance(): BaseFragment {
            val fragment = SearchUsersFragment()
            return fragment
        }
    }
}