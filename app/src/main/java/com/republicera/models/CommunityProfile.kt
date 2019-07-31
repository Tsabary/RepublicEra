package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class CommunityProfile(
    val uid: String,
    val name: String,
    val image: String,
    val contact_info: List<String>,
    val tag_line: String,
    val reputation: Long,
    val followers: Long,
    val answers: Long,
    val questions: Long,
    val shouts: Long,
    val join_date: Date,
    val last_activity: Date
) : Parcelable {

    constructor() : this("", "", "", listOf(), "", 0, 0, 0, 0, 0, Date(), Date())
}