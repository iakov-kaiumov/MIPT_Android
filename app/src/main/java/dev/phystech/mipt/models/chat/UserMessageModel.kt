package dev.phystech.mipt.models.chat

import java.util.*

class UserMessageModel {
    var text: String? = null
    var deliveryStatus: MessageDeliveryStatus? = null
    var time: String? = null

    class Builder {
        val model: UserMessageModel = UserMessageModel()

        init {
            val calendar = GregorianCalendar()
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

        fun withDeliveryStatus(value: MessageDeliveryStatus): Builder {
            model.deliveryStatus = value
            return this
        }

        fun build(): UserMessageModel = model
    }
}