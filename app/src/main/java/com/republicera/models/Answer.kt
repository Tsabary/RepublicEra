package com.republicera.models

import android.os.Parcelable
import com.google.firebase.firestore.FieldValue
import kotlinx.android.parcel.Parcelize
import java.util.*

class Answer(
    val id: String,
    val question_ID: String,
    val content: String,
    val timestamp: Date,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val photos: MutableList<String>,
    val score_items: Map<String, Int>,
    val total_score: Int
) {
    constructor() : this("", "", "", Date(), "", "", "", mutableListOf<String>(), mapOf(),0)
}
