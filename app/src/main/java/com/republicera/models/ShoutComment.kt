package com.republicera.models

class ShoutComment(
    val id: String,
    val author_ID: String,
    val content: String,
    val timestamp: Long,
    val shout_ID: String,
    val likes: MutableList<String>
) {
    constructor() : this("", "", "", 0, "", mutableListOf())
}
