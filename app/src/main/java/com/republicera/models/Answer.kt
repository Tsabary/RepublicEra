package com.republicera.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Answer(
    val id: String,
    val question_ID: String,
    val content: String,
    val timestamp: Long,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val photos: MutableList<String>,
    val score_items: Map<String, Int>
) : Parcelable {
    constructor() : this("", "", "", 0, "", "", "", mutableListOf<String>(), mapOf())
}
