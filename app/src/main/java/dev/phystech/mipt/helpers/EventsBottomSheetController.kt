package dev.phystech.mipt.helpers

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import dev.phystech.mipt.R

class EventsBottomSheetController(private val view: View) {

    interface Delegate {
        fun clickAddLesson()
        fun clickAddEvent()
        fun clickAddDeadline()
        fun clickNotifyError()
        fun clickBackChanges()
        fun toSchedulers()

    }

    private val llAddLesson: LinearLayout = view.findViewById(R.id.llAddLesson)
    private val llAddEvent: LinearLayout = view.findViewById(R.id.llAddEvent)
    private val llAddDeadline: LinearLayout = view.findViewById(R.id.llAddDeadline)
    private val llNotifyError: LinearLayout = view.findViewById(R.id.llNotifyError)
    private val llBackChanges: LinearLayout = view.findViewById(R.id.llBackChanges)
    private val tvToSchedulers: View = view.findViewById(R.id.tvToSchedulers)

    var delegate: Delegate? = null

    init {
        llAddLesson.setOnClickListener { delegate?.clickAddLesson() }
        llAddEvent.setOnClickListener { delegate?.clickAddEvent() }
        llAddDeadline.setOnClickListener { delegate?.clickAddDeadline() }
        llNotifyError.setOnClickListener { delegate?.clickNotifyError() }
        llBackChanges.setOnClickListener { delegate?.clickBackChanges() }
        tvToSchedulers.setOnClickListener { delegate?.toSchedulers() }
    }

}