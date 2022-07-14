package dev.phystech.mipt.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import dev.phystech.mipt.models.SchedulerListShortItem
import kotlin.reflect.KClass

class LinearLayoutTyped: LinearLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    var model: SchedulerListShortItem? = null



}