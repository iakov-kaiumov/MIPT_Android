package dev.phystech.mipt.models.api

class PsyApplyResponseModel: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var result: Boolean? = null
    }
}