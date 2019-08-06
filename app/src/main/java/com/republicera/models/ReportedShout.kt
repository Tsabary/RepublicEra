package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

class ReportedShout(
    val id: String,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val content: String,
    val images: List<String>,
    val timestamp: Date,
    val keeps: MutableList<String>,
    val removes: MutableList<String>
) {
    constructor() : this("", "", "", "", "", listOf(), Date(), mutableListOf(), mutableListOf())
}