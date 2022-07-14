package dev.phystech.mipt.adapters

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.ExclusionStrategy
import com.google.gson.GsonBuilder
import com.google.gson.internal.GsonBuildConfig
import com.squareup.picasso.Picasso
import dev.phystech.mipt.LocationServiceWrap
import dev.phystech.mipt.R
import dev.phystech.mipt.models.PlaceModel
import dev.phystech.mipt.models.api.TagsDataResponseModel
import dev.phystech.mipt.repositories.TagRepository
import dev.phystech.mipt.utils.GeoUtils
import dev.phystech.mipt.utils.visibility
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.converter.gson.GsonConverterFactory

class PlacesAdapter(val delegate: Delegate) :
        RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(model: PlaceModel)
    }

    private var currLocation: Location? = null
    val items: ArrayList<PlaceModel> = ArrayList()

    init {
        LocationServiceWrap.shared
            .coordinates
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                currLocation = it
                notifyDataSetChanged()
            }, {})

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_place, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(items[position])
    }

    override fun getItemCount() = items.size


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var model: PlaceModel
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        private val tvPlaceType: TextView = view.findViewById(R.id.tvPlaceType)
        private val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        private val ivImage: ImageView = view.findViewById(R.id.ivImage)

        init {
            view.setOnClickListener {
                delegate.onSelect(model)
            }
        }

        fun bind(model: PlaceModel) {
            this.model = model

            tvTitle.text = model.name
            tvDescription.text = model.address

            tvPlaceType.text = model.type

            model.tags.forEach { tag ->
                tag.id?.let { id ->
                    TagRepository.shared.getTag(id) {
                        if (it.tagType == TagsDataResponseModel.TagType.Use) {
                            tvPlaceType.text = it.name
                        }
                    }
                }
            }

            model.avatarImageUrl?.let { image ->
                Picasso.get().load(image)
                    .into(ivImage)
            }
            tvDistance.visibility = (currLocation != null).visibility()

            val placeLat = model.lat ?: return
            val placeLng = model.lng ?: return

            currLocation?.let { userLocation ->

                val distance = (GeoUtils.distanceInKm(
                    placeLat,
                    placeLng,
                    userLocation.latitude,
                    userLocation.longitude
                ) * 1000).toInt()

                if (distance < 1000) {
                    tvDistance.text = "$distance м."
                } else {
                    tvDistance.text = "${(distance / 1000).toInt()} км."
                }
            }

        }
    }
}

