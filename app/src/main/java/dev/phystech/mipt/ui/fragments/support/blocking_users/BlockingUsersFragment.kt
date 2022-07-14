package dev.phystech.mipt.ui.fragments.support.blocking_users

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
import dev.phystech.mipt.adapters.BlockingUsersAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.api.BlockUserElement
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType

class BlockingUsersFragment: BaseFragment(), BlockingUsersContract.View {

    private var presenter: BlockingUsersContract.Presenter? = null

    private lateinit var ivBack: ImageView
    private lateinit var progress: ProgressBar
    private lateinit var rvBlockingUsers: RecyclerView

    private var textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (rvBlockingUsers.adapter != null)
                (rvBlockingUsers.adapter as BlockingUsersAdapter).updateFilter(s.toString().trim())
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    //  ANDROID LIFE CIRCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BlockingUsersPresenter(resources, bottomSheetController)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blocking_users, container, false);
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
        progress = view.findViewById(R.id.progress)
        rvBlockingUsers = view.findViewById(R.id.rvBlockingUsers)

        ivBack.setOnClickListener(this::back)
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

        (adapter as BlockingUsersAdapter).onClickListener = BlockingUsersAdapter.OnClickListener{

            when (it.second) {
                0 -> {

                }
                1 -> {
                    showAlertUnblockuser(it.first)
                }
            }
        }

        rvBlockingUsers.adapter = adapter
    }

    //  EVENTS

    private fun back(view: View) {
        navigationPresenter.popFragment()
    }

    private fun showAlertUnblockuser(user: BlockUserElement) {

        val alertView = layoutInflater.inflate(R.layout.alert_unblock_user, null)
        val tvDescription: TextView = alertView.findViewById(R.id.tvDescription)
        val btnUnlock: Button = alertView.findViewById(R.id.btnUnlock)
        val btnCansel: Button = alertView.findViewById(R.id.btnCansel)

        val alert = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(alertView)
            .create()

        tvDescription.text = this.getString(R.string.user_unlock_text, user.user.getTrimName())

        btnUnlock.setOnClickListener {
            presenter?.unblockUser(user.id)
            alert.cancel()
        }

        btnCansel.setOnClickListener {
            alert.cancel()
        }

        alert.show()
    }

    companion object {
        fun newInstance(): BaseFragment {
            val fragment = BlockingUsersFragment()
            return fragment
        }
    }
}