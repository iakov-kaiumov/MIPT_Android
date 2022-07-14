package dev.phystech.mipt.repositories

import android.content.Context
import dev.phystech.mipt.Application
import dev.phystech.mipt.models.api.ChairDataResponseModel
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ChairFilterStorage private constructor() {

    val preferences = Application.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val disabledChairs: BehaviorSubject<Set<String>> = BehaviorSubject.create()

    init {
        preferences.getStringSet(KEY_DISABLED_CHAIR, emptySet())?.let {
            disabledChairs.onNext(it)
        }
    }

    fun setChairEnabled(chair: ChairDataResponseModel.PaginatorItem, isEnabled: Boolean) {
        val set = preferences.getStringSet(KEY_DISABLED_CHAIR, mutableSetOf()) ?: return

        if (isEnabled) {
            if (set.contains(chair.id) == true) {
                set.remove(chair.id)
            }
        } else {
            set.add(chair.id)
        }

        preferences.edit().putStringSet(KEY_DISABLED_CHAIR, set).apply()
        disabledChairs.onNext(set)
    }



    companion object {
        const val PREF_NAME = "chair.filter"
        const val KEY_DISABLED_CHAIR = "chair.filter.disabled_set"

        val shared = ChairFilterStorage()
    }
}