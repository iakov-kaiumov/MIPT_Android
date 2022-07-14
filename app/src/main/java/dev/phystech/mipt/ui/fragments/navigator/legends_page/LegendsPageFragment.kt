package dev.phystech.mipt.ui.fragments.navigator.legends_page

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFade
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.LegendsAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.ui.fragments.navigator.legend_detail.LegendDetailFragment

class LegendsPageFragment : BaseFragment(), LegendsPageContract.View {

    lateinit var recyclerNews: RecyclerView
    private val presenter: LegendsPagePresenter = LegendsPagePresenter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        exitTransition = MaterialElevationScale(/* growing= */ false)
        reenterTransition = MaterialElevationScale(/* growing= */ true)

        return inflater.inflate(R.layout.fragment_news_page, container, false)
    }

    override fun onStart() {
        super.onStart()
        presenter.attach(this)
    }

    override fun onStop() {
        presenter.detach()
        super.onStop()
    }

    override fun bindView(view: View?) {
        if (view == null) return

        recyclerNews = view.findViewById(R.id.recyclerNews)
        recyclerNews.layoutManager = LinearLayoutManager(mainActivity)
    }


    //  CONTRACT VIEW
    override fun showLegendDetail(forView: View, withModel: LegendModel) {
        activity?.supportFragmentManager?.let {
            val fragment = LegendDetailFragment(withModel)
            fragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
                scrimColor = Color.TRANSPARENT
            }
            fragment.exitTransition = MaterialFade()

            it.beginTransaction()
                    .addSharedElement(forView, "legend")
                    .replace(R.id.container, fragment)
                    .addToBackStack("")
                    .commit()

        }
    }

    override fun setAdapter(adapter: LegendsAdapter) {
        recyclerNews.adapter = adapter
    }

}