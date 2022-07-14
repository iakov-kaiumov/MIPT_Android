package dev.phystech.mipt.ui.fragments.services.section_detail

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.SectionSchedulerAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.SchedulerIndex
import dev.phystech.mipt.models.SportSectionModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.repositories.SportSectionsRepository
import dev.phystech.mipt.repositories.TeachersRepository
import dev.phystech.mipt.ui.fragments.study.ScheduleItemDetailFragment
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment
import dev.phystech.mipt.utils.isSuccess
import dev.phystech.mipt.utils.visibility
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SectionDetailFragment : BaseFragment(), SectionSchedulerAdapter.Delegate {

    lateinit var collapsing_toolbar: CollapsingToolbarLayout

    lateinit var backdrop: ImageView
    lateinit var ivBack: ImageView
    lateinit var tvDescription: TextView
    lateinit var schedulerRecycler: RecyclerView

    lateinit var llAuthor: LinearLayout
    lateinit var llPhone: LinearLayout
    lateinit var llWebSite: LinearLayout
    lateinit var llLocation: LinearLayout

    lateinit var tvContactsTitle: TextView
    lateinit var tvAuthor: TextView
    lateinit var tvPhone: TextView
    lateinit var tvWebSite: TextView
    lateinit var tvLocation: TextView
    lateinit var addSchedulers: MaterialButton

    lateinit var tvSchedulerTitle: TextView
    lateinit var lastDevider: View

    private var adapter: SectionSchedulerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigationPresenter.setStatusBarTransparency(true)
        adapter = SectionSchedulerAdapter(resources)
        return inflater.inflate(R.layout.fragment_sport_section_details, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar)
        backdrop = view.findViewById(R.id.backdrop)
        ivBack = view.findViewById(R.id.ivBack)
        tvDescription = view.findViewById(R.id.tvDescription)
        schedulerRecycler = view.findViewById(R.id.schedulerRecycler)
        llAuthor = view.findViewById(R.id.llAuthor)
        llPhone = view.findViewById(R.id.llPhone)
        llWebSite = view.findViewById(R.id.llWebSite)
        llLocation = view.findViewById(R.id.llLocation)
        tvContactsTitle = view.findViewById(R.id.tvContactsTitle)
        tvAuthor = view.findViewById(R.id.tvAuthor)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvWebSite = view.findViewById(R.id.tvWebSite)
        tvLocation = view.findViewById(R.id.tvLocation)
        addSchedulers = view.findViewById(R.id.addSchedulers)
        tvSchedulerTitle = view.findViewById(R.id.tvSchedulerTitle)
        lastDevider = view.findViewById(R.id.lastDevider)

        arguments?.getString(KEY_ID)?.let {
            SportSectionsRepository.shared.getByID(it) {
                it?.let { setContent(it) }
            }
        }

        adapter?.delegate = this

        schedulerRecycler.adapter = adapter
        val divider = DividerItemDecoration(requireContext(), RecyclerView.VERTICAL).apply {
            ResourcesCompat.getDrawable(resources, R.drawable.divider, null)?.let { res ->
                setDrawable(res)
            }
        }
        schedulerRecycler.addItemDecoration(divider)


        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

        addSchedulers.setOnClickListener {
            arguments?.getString(KEY_ID)?.let {
                showProgress()
                SportSectionsRepository.shared.getByID(it) { model ->
                    hideProgress()
                    model?.let {
                        val totalCount = it.schedules.count()
                        var countSuccess = 0
                        var countError = 0

                        showProgress()
                        fun handleRequest() {
                            if (countSuccess + countError == totalCount) {
                                hideProgress()

                                if (countError == 0) {
                                    showMessage(R.string.success)
                                } else {
                                    showMessage(R.string.message_some_error_try_late)
                                }

                                SchedulersRepository.shared.loadData()
                                addSchedulers.isEnabled = countError != 0
                            }
                        }

                        it.schedules.forEach {
                            val secret = it.urls?.findSecret()
                            val id = it.id

                            if (secret == null) {
                                countError++
                                handleRequest()
                                return@forEach
                            }

                            ApiClient.shared.schedulerSubscribe(id.toString(), secret)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    if (it.status.isSuccess) {
                                        ++countSuccess
                                    } else {
                                        ++countError
                                    }

                                    handleRequest()

                                }, {
                                    countError++
                                    handleRequest()
                                })

                            it.id
                        }

                    }
                }
            }
        }

    }

    override fun getBackgroundColor(): Int {
        return Color.TRANSPARENT
    }

    private fun setContent(model: SportSectionModel) {
        var filledFields = 0
        tvDescription.text = model.description
        collapsing_toolbar.title = model.name
        tvAuthor.text = model.schedules.flatMap { v -> v.teachers }
            .filterNot { v -> v.name.isNullOrEmpty() }
            .firstOrNull()?.name?.trim()
            .also { if (it.isNullOrEmpty().not()) ++filledFields }
        tvWebSite.text = model.url?.trim()
            .also { if (it.isNullOrEmpty().not()) ++filledFields }


//        tvPhone.text = model.phone
//        tvLocation.text = model.location

        llAuthor.visibility = tvAuthor.text.isNullOrEmpty().not().visibility()
        llWebSite.visibility = tvWebSite.text.isNullOrEmpty().not().visibility()
        llPhone.visibility = false.visibility()
        llLocation.visibility = false.visibility()

        tvContactsTitle.visibility = (filledFields > 0).visibility()
        lastDevider.visibility = (filledFields > 0).visibility()

        llAuthor.setOnClickListener {
            model.schedules.firstOrNull()?.teachers?.firstOrNull()?.let { teacher ->
                TeachersRepository.shared.addTeacher(teacher)
                teacher.id?.toIntOrNull()?.let { teacherId ->
                    val teacherFragment = TeacherDetailFragment.newInstance(teacherId, teacher.name)
                    navigationPresenter.pushFragment(teacherFragment, true)
                }
            }
        }

        llWebSite.setOnClickListener {
            model.url?.let {
                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(urlIntent)
            }
        }



        adapter?.items?.clear()
        adapter?.items?.addAll(model.schedules)
        adapter?.notifyDataSetChanged()

        model.image?.signatures?.firstOrNull()?.let { imgSignature ->
            val imgUrl =
                NetworkUtils.getImageUrl(imgSignature.id, imgSignature.dir, imgSignature.path)
            Picasso.get()
                .load(imgUrl)
                .into(backdrop)
        }

        tvSchedulerTitle.visibility = (adapter?.itemCount != 0).visibility()
        addSchedulers.isEnabled = model.schedules.all {
            return@all SchedulersRepository.shared.getSchedulerItemById(it.id) != null
        }.not()

    }


    companion object {
        const val KEY_ID = "section.id"

        fun newInstance(sectionId: String): BaseFragment {
            val fragment = SectionDetailFragment()
            val arguments = Bundle().apply {
                putString(KEY_ID, sectionId)
            }


            return fragment.apply { this.arguments = arguments }
        }
    }


    override fun onSelect(model: SchedulerIndex) {
        showProgress()
        SchedulersRepository.shared.getSchedulerItemById(model.id) {
            hideProgress()
            if (it == null) return@getSchedulerItemById
            val detailFragment = ScheduleItemDetailFragment(it)
            navigationPresenter.pushFragment(detailFragment, true)

        }

    }
}