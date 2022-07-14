package dev.phystech.mipt.ui.fragments.navigator.place_page

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFade
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.PlacesAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.models.api.PlacePaginatorItem
import dev.phystech.mipt.models.api.PlacesDataResponseModel
import dev.phystech.mipt.ui.fragments.navigator.place_detail.PlaceDetailFragment

class PlacesPageFragment: BaseFragment(), PlacePageContract.View {

    lateinit var recyclerPlaces: RecyclerView
    private val presenter: PlacePagePresenter = PlacePagePresenter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_places_page, container, false)
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
        if (view == null) return;

        recyclerPlaces = view.findViewById(R.id.recyclerPlaces)
    }



    override fun setAdapter(adapter: PlacesAdapter) {
        recyclerPlaces.adapter = adapter
    }

    override fun showPlaceDetail(forModel: PlaceModel) {
        activity?.supportFragmentManager?.let {
            val fragment = PlaceDetailFragment(forModel)
            fragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
                scrimColor = Color.TRANSPARENT
            }

            fragment.exitTransition = MaterialFade()

            it.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack("")
                .commit()

        }
    }

}