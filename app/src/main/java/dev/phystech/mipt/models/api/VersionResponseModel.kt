package dev.phystech.mipt.models.api

class VersionResponseModel: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var setting: Setting? = null

        class Setting {
            var id: String? = null
            var name: String? = null
            var key: String? = null
            var value: String? = null
        }
    }
}
