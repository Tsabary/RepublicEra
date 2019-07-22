package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Shout(
    val id: String,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val content: String,
    val images: List<String>,
    val timestamp: Long,
    val last_interaction: Long,
    val reports: List<String>,
    val visible: Boolean,
    val likes: MutableList<String>,
    val comments: Int,
    val xfollowers: MutableList<String>
) : Parcelable {
    constructor() : this("", "", "", "", "", listOf(), 0, 0, listOf(), true, mutableListOf(), 0, mutableListOf())
}