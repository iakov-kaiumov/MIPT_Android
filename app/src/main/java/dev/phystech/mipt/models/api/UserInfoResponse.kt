package dev.phystech.mipt.models.api

import android.app.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.models.User
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class UserInfoResponse (
    val status: String,
    val data: ArrayList<Member> = ArrayList<Member>()
)