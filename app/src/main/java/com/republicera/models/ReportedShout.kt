package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ReportedShout(
    val id: String,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val content: String,
    val images: List<String>,
    val timestamp: Long,
    val keeps: MutableList<String>,
    val removes: MutableList<String>
) : Parcelable {
    constructor() : this("", "", "", "", "", listOf(), 0, mutableListOf(), mutableListOf())
}