package dev.phystech.mipt.models.chat

import java.util.*

class BotMessageModel {
    var text: String? = null
    var time: String? = null


    class Builder {
        val model: BotMessageModel = BotMessageModel()

        init {
            val calendar = GregorianCalendar(Locale.getDefault())
            val hour = calendar.get(GregorianCalendar.HOUR_OF_DAY).toString().padStart(2, '0')
            val min = calendar.get(GregorianCalendar.MINUTE).toString().padStart(2, '0')

            model.time = "$hour:$min"
        }

        fun setText(value: String): Builder {
            model.text = value
            return this
        }

        fun setTime(value: String): Builder {
            model.time = value
            return this
        }

        fun build(): BotMessageModel = model
    }
}