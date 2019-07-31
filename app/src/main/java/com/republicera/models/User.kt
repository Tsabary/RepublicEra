package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.HashMap

@Parcelize
class User(
    val uid: String,
    val first_name: String,
    val last_name: String,
    val communities_list: MutableList<String>,
    val lang_list: MutableList<String>,
    val reputation: Long,
    val join_date: HashMap<String,String>,
    val last_activity: HashMap<String,String>
) : Parcelable {
    constructor() : this("","", "", mutableListOf(), mutableListOf(), 0, hashMapOf(), hashMapOf())
}
