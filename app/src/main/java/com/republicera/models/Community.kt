package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


class Community(
    val id: String,
    val title: String,
    val description: String,
    val image: String,
    val members: Int,
    val active_members: Int,
    val public: Boolean,
    val founder: String,
    val admins: MutableList<String>
) {
    constructor() : this("", "", "","", 0, 0, true, "", mutableListOf())
}
