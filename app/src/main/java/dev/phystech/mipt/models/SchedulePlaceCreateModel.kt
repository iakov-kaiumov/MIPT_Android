package dev.phystech.mipt.models

import com.google.gson.annotations.SerializedName

class SchedulePlaceCreateModel {
    @SerializedName("schedule-place-form[schedule-place][name]")
    var name: String? = null
    @SerializedName("schedule-place-form[schedule-place][type]")
    var type: String? = null
    @SerializedName("schedule-place-form[schedule-place][building]")
    var building: String? = null
    @SerializedName("schedule-place-form[schedule-place][floor]")
    var floor: String? = null
}