package com.republicera.models

import com.google.firebase.firestore.FieldValue
import java.util.*

class ShoutComment(
    val id: String,
    val author_ID: String,
    val author_name: String,
    val author_image: String,
    val content: String,
    val timestamp: Date,
    val shout_ID: String,
    val likes: MutableList<String>
) {
    constructor() : this("", "","","", "", Date(), "", mutableListOf())
}
