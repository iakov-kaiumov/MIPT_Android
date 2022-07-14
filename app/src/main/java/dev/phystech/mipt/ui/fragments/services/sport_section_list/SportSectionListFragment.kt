package dev.phystech.mipt.ui.fragments.services.sport_section_list

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.SportSectionsAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.SportSectionModel
import dev.phystech.mipt.repositories.SportSectionsRepository
import dev.phystech.mipt.ui.fragments.services.section_detail.SectionDetailFragment
import dev.phystech.mipt.ui.fragments.study.new_auditory.NewAuditoryFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SportSectionListFragment: BaseFragment(), SportSectionsAdapter.Delegate {

    private lateinit var mainRecycler: RecyclerView
    private lateinit var ivBack: ImageView
    private lateinit var etName: EditText

    private lateinit var adapter: SportSectionsAdapter

    override fun onResume() {
        super.onResume()
        navigationPresenter.setStatusBarTransparency(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sport_sections, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        mainRecycler = view.findViewById(R.id.mainRecycler)
        ivBack = view.findViewById(R.id.ivBack)
        etName = view.findViewById(R.id.etName)

        etName.addTextChangedListener(filterTextWatcher)
        adapter = SportSectionsAdapter(resources)
        adapter.delegate = this
        mainRecycler.adapter = adapter

        SportSectionsRepository.shared.sections
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                adapter.setItems(it)
            })

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }



    //  SECTION ADAPTER LIST DELEGATE
    override fun onSelect(model: SportSectionModel) {
        val id = model.id ?: return
        val sectionFragment = SectionDetailFragment.newInstance(id)
        navigationPresenter.pushFragment(sectionFragment, true)
    }


    //  LISTENERS
    private val filterTextWatcher: TextWatcher = NewAuditoryFragment.BaseTextWatcher {
        adapter.setFilter(etName.text.toString())
    }


}