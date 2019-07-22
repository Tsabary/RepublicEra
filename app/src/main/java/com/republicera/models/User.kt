package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val communities_list: MutableList<String>,
    val lang_list: List<String>,
    val reputation: Long,
    val join_date: Long,
    val last_activity: Long
) : Parcelable {
    constructor() : this("","", "", mutableListOf<String>(), listOf(), 0, 0, 0)
}
