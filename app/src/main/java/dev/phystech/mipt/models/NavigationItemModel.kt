package dev.phystech.mipt.models

class NavigationItemModel {
    var title: String? = null
    var legendModel: LegendModel? = null
    var reference: ReferenceModel? = null

    var type: Type? = null

    enum class Type {
        Title,
        Place,
        Legend,
        Contact
    }

    companion object {
        fun withTitle(title: String): NavigationItemModel {
            return NavigationItemModel().apply {
                this.title = title
                this.type = Type.Title
            }
        }
    }
}