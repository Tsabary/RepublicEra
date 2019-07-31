package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class Shout(
    val id: String,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val content: String,
    val images: List<String>,
    val language: String,
    val timestamp: Date,
    val last_interaction: Date,
    val reported: Boolean,
    val visible: Boolean,
    val likes: MutableList<String>,
    val comments: Int,
    val xfollowers: MutableList<String>
) : Parcelable {
    constructor() : this("", "", "", "", "", listOf(),"", Date(), Date(), false, true, mutableListOf(), 0, mutableListOf())
}