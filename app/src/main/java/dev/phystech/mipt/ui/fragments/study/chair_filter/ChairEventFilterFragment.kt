package dev.phystech.mipt.ui.fragments.study.chair_filter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ChairFilterAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.News
import dev.phystech.mipt.models.api.ChairDataResponseModel
import dev.phystech.mipt.repositories.ChairFilterStorage
import dev.phystech.mipt.repositories.ChairRepository
import dev.phystech.mipt.utils.visibility
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.concurrent.thread

class ChairEventFilterFragment : BaseFragment(), ChairFilterAdapter.Delegate {

    lateinit var rvOfficial: RecyclerView
    lateinit var rvChairs: RecyclerView
    lateinit var rvOthers: RecyclerView
    lateinit var ivBack: ImageView

    lateinit var tvOfficial: TextView
    lateinit var tvChair: TextView
    lateinit var tvOthers: TextView

    val chairsAdapter: ChairFilterAdapter = ChairFilterAdapter(ChairFilterAdapter.Type.Chair)
    val othersAdapter: ChairFilterAdapter = ChairFilterAdapter(ChairFilterAdapter.Type.Others)
    val officialAdapter: ChairFilterAdapter = ChairFilterAdapter(ChairFilterAdapter.Type.Official)

    init {
        chairsAdapter.delegate = this
        othersAdapter.delegate = this
        officialAdapter.delegate = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chair_filter, container, false)

        return view
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun bindView(view: View?) {
        if (view == null) return

        rvOfficial = view.findViewById(R.id.rvOfficial)
        rvChairs = view.findViewById(R.id.rvChairs)
        rvOthers = view.findViewById(R.id.rvOthers)
        ivBack = view.findViewById(R.id.ivBack)

        tvOfficial = view.findViewById(R.id.tvOfficial)
        tvChair = view.findViewById(R.id.tvChair)
        tvOthers = view.findViewById(R.id.tvOthers)

        rvOfficial.adapter = officialAdapter
        rvChairs.adapter = chairsAdapter
        rvOthers.adapter = othersAdapter

        ChairRepository.shared.allEventChairs
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                chairsAdapter.items.clear()
                othersAdapter.items.clear()
                officialAdapter.items.clear()

                chairsAdapter.items.addAll(it.filter { v -> v.type == "chair" })
                othersAdapter.items.addAll(it.filter { v -> v.type == "other" })
                officialAdapter.items.addAll(it.filter { v -> v.type == "official" })

                tvOfficial.visibility = officialAdapter.items.isNotEmpty().visibility()
                tvChair.visibility = chairsAdapter.items.isNotEmpty().visibility()
                tvOthers.visibility = othersAdapter.items.isNotEmpty().visibility()

                if (ChairFilterStorage.shared.disabledChairs.hasValue()) {
                    ChairFilterStorage.shared.disabledChairs.value.let { disabledItems ->
                        chairsAdapter.disabled = disabledItems.toMutableSet()
                        othersAdapter.disabled = disabledItems.toMutableSet()
                        officialAdapter.disabled = disabledItems.toMutableSet()
                    }
                }

                chairsAdapter.notifyDataSetChanged()
                othersAdapter.notifyDataSetChanged()
                officialAdapter.notifyDataSetChanged()

            }, {
                print("")
            })

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }


    //  CHAIR FILTER DELEGATE
    override fun onChecked(
        item: ChairDataResponseModel.PaginatorItem,
        isChecked: Boolean,
        type: ChairFilterAdapter.Type
    ) {
        if (type == ChairFilterAdapter.Type.Chair ||
            type == ChairFilterAdapter.Type.Others ||
            type == ChairFilterAdapter.Type.Official
        ) {
                ChairFilterStorage.shared.setChairEnabled(item, isChecked)
        }
    }


    companion object {
        const val KEY_TYPE_INDEX = "type.index"

        fun newInstance(type: News.Type? = null): BaseFragment {
            return ChairEventFilterFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_TYPE_INDEX, type?.index ?: -1)
                }
            }
        }
    }

}