package dev.phystech.mipt.ui.fragments.navigator.images_fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.utils.exception.NoImageIDException

/** Экран, отображающий фотографии "Места" [PlaceModel]
 * Создание: [ImagesPlaceFullscreenFragment.getInstanse]

 * Параметры:
 * * ID Модели места
 * * Position - с какой фотографии начать отображение
 *
 */
class ImagesPlaceFullscreenFragment: BaseFragment(), ImagesPlaceFullscreenContract.View {

    lateinit var ivBack: ImageView
    lateinit var tvPagination: TextView
    lateinit var tvTitle: TextView
    lateinit var viewPager: ViewPager2

    var presenter: ImagesPlaceFullscreenPresenter? = null

    //  LIFE CIRCLE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_fullscreen, container, false)
    }

    override fun onStart() {
        if (presenter == null) {
            val fromPosition = arguments?.getInt(START_IMAGE_POSITION, 0) ?: 0
            val modelId = arguments?.getString(MODEL_ID) ?: throw NoImageIDException()
            presenter = ImagesPlaceFullscreenPresenter(modelId, fromPosition)
        }

        super.onStart()
        presenter?.attach(this)
    }

    override fun onStop() {
        presenter?.detach()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        navigationPresenter.setBottomNavigationVisibility(false)
    }

    override fun onPause() {
        super.onPause()
        navigationPresenter.setBottomNavigationVisibility(true)
    }


    //  BASE FRAGMENT
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)
        tvPagination = view.findViewById(R.id.tvPagination)
        tvTitle = view.findViewById(R.id.tvTitle)
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                presenter?.onPageUpdate(position)
            }
        })

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }
    }


    //  MVP VIEW
    override fun setTitle(title: String) {
        tvTitle.text = title
    }

    override fun setPaginationTitle(text: String) {
        tvPagination.text = text
    }

    override fun <VH : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<VH>) {
        val startPosition = arguments?.getInt(START_IMAGE_POSITION, 0) ?: 0
        viewPager.adapter = adapter
        viewPager.currentItem = startPosition
    }


    companion object {
        const val MODEL_ID = "place_model_id"
        const val START_IMAGE_POSITION = "start_image_position"

        fun getInstanse(placeModelId: String, beginFromPosition: Int = 0): ImagesPlaceFullscreenFragment {
            val fragment = ImagesPlaceFullscreenFragment()
            val bundle = Bundle()
            bundle.putString(MODEL_ID, placeModelId)
            bundle.putInt(START_IMAGE_POSITION, beginFromPosition)

            fragment.arguments = bundle
            return fragment
        }
    }
}