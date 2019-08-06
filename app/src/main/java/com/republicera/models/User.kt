package com.republicera.models

import android.os.Parcelable
import com.google.firebase.firestore.FieldValue
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.HashMap

class User(
    val uid: String,
    val first_name: String,
    val last_name: String,
    val communities_list: MutableList<String>,
    val lang_list: MutableList<String>,
    val birth_day: Date,
    val reputation: Long,
    val join_date: Date,
    val last_activity: Date
) {
    constructor() : this("", "", "", mutableListOf(), mutableListOf(), Date(), 0, Date(), Date())
}
