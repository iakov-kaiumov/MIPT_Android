package dev.phystech.mipt.models.api

class BaseWithValue: BaseApiEntity() {

    var data: Data? = null

    fun getValue(): String? {
        return data?.setting?.value
    }

    class Data {
        val setting: Settings? = null
    }

    class Settings {
        var value: String? = null
    }
}