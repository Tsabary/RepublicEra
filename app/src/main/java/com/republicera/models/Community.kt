package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Community(val id: String, val title: String, val description : String, val members: Int, val active_members: Int, val public: Boolean, val super_admin: String, val admins:MutableList<String>) :
    Parcelable {
    constructor() : this("", "", "", 0, 0, true,"", mutableListOf())
}
