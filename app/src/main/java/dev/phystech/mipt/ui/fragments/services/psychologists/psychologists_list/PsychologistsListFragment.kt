package dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.PsychologistsListAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.models.api.UsersResponseModel
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment

class PsychologistsListFragment : BaseFragment(), PsychologistsListContract.View {

    private val allPsychologists: ArrayList<UsersResponseModel.UserInfoModel> = arrayListOf()
    private var presenter: PsychologistsListContract.Presenter? = null

    lateinit var ivBack: ImageView
    private lateinit var psychologistsRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_psychologists_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = PsychologistsListPresenter(resources, bottomSheetController)
    }

    override fun onStart() {
        super.onStart()
        presenter?.attach(this)
        if (allPsychologists.isEmpty()) {
            presenter?.load()
        } else {
            presenter?.setPsychologists(allPsychologists)
        }
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }

    //  MVP VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        psychologistsRecycler = view.findViewById(R.id.psychologistsRecycler)

        //  EVENTS

        ivBack.setOnClickListener(this::onBack)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    //  OTHERS

    fun onBack(view: View) {
        navigationPresenter.popFragment()
    }

    override fun navigate(fragment: BaseFragment) {
        navigationPresenter.pushFragment(fragment, true)
    }

    override fun showLoader(visible: Boolean) {
        if (visible) {
            showProgress()
        } else {
            hideProgress()
        }
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {

        if(allPsychologists.isEmpty()) {
            allPsychologists.addAll((adapter as PsychologistsListAdapter).items)
        }

        (adapter as PsychologistsListAdapter).onClickListener = PsychologistsListAdapter.OnClickListener {
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

        psychologistsRecycler.adapter = adapter
    }

    companion object {
        fun newInstance(psychologists: ArrayList<UsersResponseModel.UserInfoModel>): BaseFragment {
            val fragment = PsychologistsListFragment()
            fragment.allPsychologists.clear()
            fragment.allPsychologists.addAll(psychologists)
            return fragment
        }
    }
}