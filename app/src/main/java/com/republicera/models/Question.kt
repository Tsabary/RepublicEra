package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Question(
    val id: String,
    val title: String,
    val details: String,
    val language: String,
    val tags: MutableList<String>,
    val timestamp: Long,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val answers: Int,
    val last_interaction: Long,
    val score_items: Map<String, Int>
) : Parcelable {
    constructor() : this("", "", "", "", mutableListOf<String>(), 0, "", "", "", 0, 0, mapOf())
}
