package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.SportSectionModel

class SportSectionResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data {
        var paginator: HashMap<String, SportSectionModel> = hashMapOf()
    }
}