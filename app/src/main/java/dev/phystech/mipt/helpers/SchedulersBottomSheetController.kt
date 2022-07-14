package dev.phystech.mipt.helpers

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import dev.phystech.mipt.R
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.repositories.Storage
import dev.phystech.mipt.repositories.UserRepository

class SchedulersBottomSheetController(private val view: View) {

    interface Delegate {
        fun clickAddLesson()
        fun clickAddEvent()
        fun clickAddDeadline()
        fun clickNotifyError()
        fun clickBackChanges()
        fun breaksEnabled(isEnabled: Boolean)
        fun toEvents()
    }

    private val llAddLesson: LinearLayout = view.findViewById(R.id.llAddLesson)
    private val llAddEvent: LinearLayout = view.findViewById(R.id.llAddEvent)
    private val llAddDeadline: LinearLayout = view.findViewById(R.id.llAddDeadline)
    private val llNotifyError: LinearLayout = view.findViewById(R.id.llNotifyError)
    private val llBackChanges: LinearLayout = view.findViewById(R.id.llBackChanges)
    private val switchBreak: SwitchMaterial = view.findViewById(R.id.switchBreak)
    private val tvToEvents: View = view.findViewById(R.id.tvToEvents)

    var delegate: Delegate? = null

    init {
        llAddLesson.setOnClickListener { delegate?.clickAddLesson() }
        llAddEvent.setOnClickListener { delegate?.clickAddEvent() }
        llAddDeadline.setOnClickListener { delegate?.clickAddDeadline() }
        llNotifyError.setOnClickListener { delegate?.clickNotifyError() }
        llBackChanges.setOnClickListener { delegate?.clickBackChanges() }
        tvToEvents.setOnClickListener { delegate?.toEvents() }
        switchBreak.setOnCheckedChangeListener { _, isChecked -> delegate?.breaksEnabled(isChecked) }
    }

}