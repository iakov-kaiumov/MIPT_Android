package dev.phystech.mipt.ui.fragments.study.new_auditory

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.models.Building
import dev.phystech.mipt.models.SchedulePlaceCreateModel
import dev.phystech.mipt.repositories.SchedulePlaceRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

/** Создание новой аудитории.
 *
 * При успешном создании отправляется Notification через [LocalBroadcastManager] с полями:
 * * ACTION
 * * PLACE_ID
 *
 */
class NewAuditoryFragment: BaseFragment() {

    lateinit var tvName: TextView
    lateinit var tvBuilding: MaterialAutoCompleteTextView
    lateinit var tvType: MaterialAutoCompleteTextView
    lateinit var tvFloor: MaterialAutoCompleteTextView

    lateinit var wrapName: TextInputLayout
    lateinit var wrapBuilding: TextInputLayout
    lateinit var wrapType: TextInputLayout
    lateinit var wrapFloor: TextInputLayout
    lateinit var ivBack: ImageView

    lateinit var ivEdit: ImageView

    private var buildings: List<Building> = emptyList()
    private var types: List<Building> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_auditory, container, false)
    }


    override fun bindView(view: View?) {
        if (view == null) return

        tvName = view.findViewById(R.id.tvName)
        tvBuilding = view.findViewById(R.id.tvBuilding)
        tvType = view.findViewById(R.id.tvType)
        tvFloor = view.findViewById(R.id.tvFloor)

        wrapName = view.findViewById(R.id.wrapName)
        wrapBuilding = view.findViewById(R.id.wrapBuilding)
        wrapType = view.findViewById(R.id.wrapType)
        wrapFloor = view.findViewById(R.id.wrapFloor)

        ivEdit = view.findViewById(R.id.ivEdit)
        ivBack = view.findViewById(R.id.ivBack)

        tvName.addTextChangedListener(BaseTextWatcher {
            wrapName.error = null
        })
        tvBuilding.addTextChangedListener(BaseTextWatcher {
            wrapBuilding.error = null
        })
        tvType.addTextChangedListener(BaseTextWatcher {
            wrapType.error = null
        })
        tvFloor.addTextChangedListener(BaseTextWatcher {
            wrapFloor.error = null
        })
        ivEdit.setOnClickListener {
            SchedulePlaceCreateModel().apply {
                name
                type
                building
                floor
            }
            navigationPresenter.popFragment()
        }

        tvBuilding.dropDownHorizontalOffset = 16.px
        tvType.dropDownHorizontalOffset = 16.px

        SchedulePlaceRepository.shared.places
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                types = it.mapNotNull { v -> v.type }.distinct()
                buildings = it.mapNotNull { v -> v.building }.distinct()

                tvBuilding.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, buildings.mapNotNull { v -> v.name }.distinct()))
                tvType.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, types.mapNotNull { v -> v.name }.distinct()))

            }, {})

        ivEdit.setOnClickListener {
            var errors = 0
            if (tvName.text.isNullOrEmpty()) {
                wrapName.error = "Заполните поле"
                ++errors
            }

            if (tvBuilding.text.isNullOrEmpty() && false) {
                wrapBuilding.error = "Заполните поле"
                ++errors
            }

            if (tvType.text.isNullOrEmpty() && false) {
                wrapType.error = "Заполните поле"
                ++errors
            }

            if (tvFloor.text.isNullOrEmpty() && false) {
                wrapFloor.error = "Заполните поле"
                ++errors
            }

            if (errors > 0) return@setOnClickListener

            val model = SchedulePlaceCreateModel().apply {
                name = getName()
                type = getType()
                building = getBuilding()
                floor = getFloor()
            }

            SchedulePlaceRepository.shared.createAuditory(model) { isSuccess, newModel ->
                if (isSuccess) {
                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(
                        Intent(ACTION).apply {
                            putExtra(PLACE_ID, newModel?.id ?: String.empty())
                        }
                    )
                    navigationPresenter.popFragment()
                } else {
                    showMessage(R.string.hane_not_permission)
                }
            }
        }

        ivBack.setOnClickListener {
            navigationPresenter.popFragment()
        }

    }

    //  OTHERS
    fun getName(): String? {
        return tvName.text.toString()
    }

    fun getBuilding(): String? {
        val buildingValue = tvBuilding.text.toString()
        if (buildingValue.isNullOrBlank()) return null

        return buildings.firstOrNull{ v -> v.name == buildingValue }?.id
    }

    fun getType(): String? {
        val typeValue = tvType.text.toString()
        if (typeValue.isNullOrBlank()) return null

        return types.firstOrNull{ v -> v.name == typeValue }?.id
    }

    fun getFloor(): String? {
        return tvFloor.text.toString()
    }


    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    class BaseTextWatcher(val someEdit: (() -> Unit)? = null): TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            someEdit?.invoke()
        }
    }

    companion object {
        const val ACTION = "NEW_AUDITORY_CREATED"
        const val PLACE_ID = "KEY.PLACE_ID"
    }
}
