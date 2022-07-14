package dev.phystech.mipt.ui.fragments.services.bills

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.BillsAdapter
import dev.phystech.mipt.adapters.OnBillClickListener
import dev.phystech.mipt.base.BaseFragment

class BillsFragment : BaseFragment() {

    lateinit var billsAdapter: BillsAdapter
    lateinit var recyclerBills: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bills, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        billsAdapter = BillsAdapter(object : OnBillClickListener {
            override fun onClick() {

            }
        })
        recyclerBills = view.findViewById(R.id.recyclerBills)
        recyclerBills.addItemDecoration(
                DividerItemDecoration(
                        mainActivity,
                        DividerItemDecoration.VERTICAL
                )
        )

        recyclerBills.addItemDecoration(
                object : DividerItemDecoration(context, VERTICAL) {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildAdapterPosition(view)
                        if (position == state.itemCount - 1) {
                            outRect.setEmpty()
                        } else {
                            super.getItemOffsets(outRect, view, parent, state)
                        }
                    }
                }
        )

        recyclerBills.adapter = billsAdapter

        val ivBack: ImageView = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            mainActivity.supportFragmentManager.popBackStack();
        }
    }
}