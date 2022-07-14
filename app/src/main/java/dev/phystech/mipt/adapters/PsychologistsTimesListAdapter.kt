package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.scheduler_event.SchedulerEventTitleModel
import dev.phystech.mipt.models.api.PsyTimeResponseModel
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.models.api.UsersResponseModel
import dev.phystech.mipt.ui.utils.AvatarColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PsychologistsTimesListAdapter: RecyclerView.Adapter<PsychologistsTimesListAdapter.ViewHolder>() {

    var items: ArrayList<PsyTimeResponseModel.PsyInfoModel> = arrayListOf()
    var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_psychologist_time, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val mainLayout: LinearLayout = view.findViewById(R.id.mainLayout)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val llAvatar: LinearLayout = view.findViewById(R.id.llAvatar)
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)

        val tvFirstDate: TextView = view.findViewById(R.id.tvFirstDate)
        val tvLastDate: TextView = view.findViewById(R.id.tvLastDate)

        fun bind(model: PsyTimeResponseModel.PsyInfoModel) {

            val img = if (model.user_psy?.image != null && !model.user_psy?.image!!.signatures.isNullOrEmpty()) {
                model.user_psy?.image!!.signatures.firstOrNull()
            } else null
            if (img != null) {
                llAvatar.visibility = View.GONE
                Picasso.get()
                    .load(NetworkUtils.getImageUrl(img.id, img.dir, img.path))
                    // .error(R.drawable.ic_filter_dield_example)
                    .into(ivAvatar)
            } else {

                val colorRandomPosition = model.user_psy?.name.hashCode() % 6
                val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
                llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))

                tvAvatar.text = model.user_psy?.getAvatarName()
                llAvatar.visibility = View.VISIBLE
            }

            tvName.text = model.user_psy?.name

            val dateFormat = SimpleDateFormat("HH:mm")
            model.startTime?.getFormatDate()?.let { startTime ->
                tvFirstDate.text = dateFormat.format(startTime)
            }
            model.endTime?.getFormatDate()?.let { endTime ->
                tvLastDate.text = dateFormat.format(endTime)
            }

            mainLayout.setOnClickListener {
                onClickListener?.onClick(model)
            }
        }
    }

    class OnClickListener(val clickListener: (item: PsyTimeResponseModel.PsyInfoModel) -> Unit) {
        fun onClick(item: PsyTimeResponseModel.PsyInfoModel) = clickListener(item)
    }
}