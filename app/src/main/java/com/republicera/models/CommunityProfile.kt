package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class CommunityProfile(
    val uid: String,
    val name: String,
    val image: String,
    val contact_info: List<String>,
    val tag_line: String,
    val reputation: Long,
    val followers: Long,
    val join_date: Long,
    val last_activity: Long
) : Parcelable {

    constructor() : this("", "",  "", listOf(), "", 0, 0, 0, 0)
}