package com.republicera.models

import android.os.Parcelable
import com.google.firebase.firestore.FieldValue
import kotlinx.android.parcel.Parcelize
import java.util.*


class Question(
    val id: String,
    val title: String,
    val details: String,
    val language: String,
    val tags: MutableList<String>,
    val timestamp: Date,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val answers: Int,
    val last_interaction: Date,
    val score_items: Map<String, Int>
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        mutableListOf<String>(),
        Date(),
        "",
        "",
        "",
        0,
        Date(),
        mapOf()
    )
}
