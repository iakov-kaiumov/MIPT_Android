package dev.phystech.mipt.ui.fragments.navigator.legend_detail

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.PlacesAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.PlacesRepository
import dev.phystech.mipt.repositories.TagRepository
import dev.phystech.mipt.ui.activities.main.MainActivity
import dev.phystech.mipt.ui.fragments.navigator.place_detail.PlaceDetailFragment
import dev.phystech.mipt.utils.visibility

class LegendDetailFragment(val model: LegendModel): BaseFragment(), PlacesAdapter.Delegate {

    lateinit var img: ImageView
    lateinit var ivClose: ImageView
    lateinit var tvTitle: TextView
    lateinit var recycler: RecyclerView
    lateinit var tvDescription: TextView
    lateinit var tvPlacesTitle: TextView

    private val adapter = PlacesAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        (activity as? MainActivity)?.setStatusBarTransparency(true)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.setStatusBarTransparency(false)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_legend_detail, container, false)

        return view
    }


    override fun bindView(view: View?) {
        if (view == null) return

        img = view.findViewById(R.id.img)
        ivClose = view.findViewById(R.id.ivClose)
        tvTitle = view.findViewById(R.id.tvTitle)
        recycler = view.findViewById(R.id.recycler)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvPlacesTitle = view.findViewById(R.id.tvPlacesTitle)

        model.imageUrl.let { image ->
            Picasso.get().load(image)
                .into(img)
        }

        ivClose.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        tvTitle.text = model.title
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvDescription.text = Html.fromHtml(model.content, Html.FROM_HTML_MODE_LEGACY)
        } else {
            tvDescription.text = Html.fromHtml(model.content)
        }

        tvDescription.movementMethod = LinkMovementMethod()

        val items = model.placesID.map { v -> PlacesRepository.shared.places.value.firstOrNull { p -> v == p.id } }.filterNotNull()

        if (items.size > 0) {
            adapter.items.clear()
            adapter.items.addAll(items)
            tvPlacesTitle.visibility = true.visibility()
        } else {
            tvPlacesTitle.visibility = false.visibility()
        }

        recycler.adapter = adapter
    }


    //  PLACE DELEGATE
    override fun onSelect(model: PlaceModel) {
//        model.fromModel?.let {
            val fragment = PlaceDetailFragment(model)
            fragmentManager?.beginTransaction()
                ?.addToBackStack(null)
                ?.replace(R.id.container, fragment)
                ?.commit()
//        }

    }

}