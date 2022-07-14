package dev.phystech.mipt.ui.fragments.navigator.place_detail

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFade
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.ConvenienceAdapter
import dev.phystech.mipt.adapters.LegendsAdapter
import dev.phystech.mipt.adapters.PlaceImagesAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.interfaces.FragmentNavigation
import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.ui.fragments.navigator.NavigatorPresenter
import dev.phystech.mipt.ui.fragments.navigator.images_fullscreen.ImagesPlaceFullscreenFragment
import dev.phystech.mipt.ui.fragments.navigator.legend_detail.LegendDetailFragment
import dev.phystech.mipt.utils.visibility

class PlaceDetailFragment(private val model: PlaceModel): BaseFragment(), PlaceDetailContract.View {

    private val presenter: PlaceDetailContract.Presenter = PlaceDetailPresenter(model)

    private lateinit var ivBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var imageRecycler: RecyclerView
    private lateinit var tvPlaceType: TextView
    private lateinit var tvAddress: TextView
    private lateinit var llNeedPass: LinearLayout
    private lateinit var recyclerConvenience: RecyclerView
    private lateinit var tvDescription: TextView
    private lateinit var tvLegendTitle: TextView
    private lateinit var rvLegend: RecyclerView


    //  LIFE CIRCLE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_detail, container, false)
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

        ivBack = view.findViewById(R.id.ivBack)
        tvTitle = view.findViewById(R.id.tvTitle)
        imageRecycler = view.findViewById(R.id.imageRecycler)
        tvPlaceType = view.findViewById(R.id.tvPlaceType)
        tvAddress = view.findViewById(R.id.tvAddress)
        llNeedPass = view.findViewById(R.id.llNeedPass)
        recyclerConvenience = view.findViewById(R.id.recyclerConvenience)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvLegendTitle = view.findViewById(R.id.tvLegendTitle)
        rvLegend = view.findViewById(R.id.rvLegend)

        ivBack.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        tvAddress.setOnClickListener {
            val lat = model.lat ?: return@setOnClickListener
            val lng = model.lng ?: return@setOnClickListener

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng"))
            startActivity(intent)
        }

        setContent(model)
    }


    //  OTHERS
    private fun setContent(model: PlaceModel) {
        tvTitle.text = model.name
//        tvPlaceType.text = "" TODO
        tvAddress.text = model.address
        llNeedPass.visibility = (model.needPermit).visibility()
        tvDescription.text = Html.fromHtml(model.description)


        rvLegend.visibility = (model.postsID.size > 0).visibility()

    }


    //  CONTRACT VIEW
    override fun setImagesAdapter(adapter: PlaceImagesAdapter) {
        imageRecycler.doOnPreDraw {
            adapter.width = it.width
            adapter.notifyDataSetChanged()
            imageRecycler.adapter = adapter

        }

    }

    override fun setTagsAdapter(adapter: ConvenienceAdapter) {
        recyclerConvenience.adapter = adapter
    }

    override fun setLegendAdapter(adapter: LegendsAdapter) {
        rvLegend.adapter = adapter
        tvLegendTitle.visibility = (adapter.items.size > 0).visibility()
    }

    override fun showLegend(view: View, withModel: LegendModel) {
        val fragment = LegendDetailFragment(withModel)
        fragment.sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
        }
        fragment.exitTransition = MaterialFade()

        activity?.supportFragmentManager?.let {
            it.beginTransaction()
                .addSharedElement(view, "legend")
                .replace(R.id.container, fragment)
                .addToBackStack("")
                .commit()
        }
    }

    override fun setPlaceType(type: String) {
        tvPlaceType.text = type
    }

    override fun showImageFullscreen(withStartPosition: Int) {
        val id = model.id ?: return
        val fragment = ImagesPlaceFullscreenFragment.getInstanse(id, withStartPosition)

        (activity as? FragmentNavigation.Presenter)?.let {
            it.pushFragment(fragment, true)
        }
    }
}