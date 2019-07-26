package com.republicera.models

class AnswerComment(
    val id: String,
    val answer_ID: String,
    val question_ID: String,
    val content: String,
    val timestamp: Long,
    val author_ID: String,
    val author_name: String,
    val author_image: String
) {
    constructor() : this("", "", "", "", 0, "", "", "")
}