package com.republicera.models

class ReputationScore(
    val receiver_ID: String,
    val initiator_ID: String,
    val initiator_name: String,
    val initiator_image: String,
    val main_post_ID: String,
    val specific_post_ID: String,
    val post_type: Int,
    val scenario_type: Int,
    val collection: String,
    val points: Int,
    val timestamp: Long
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        0,
        "",
        0,
        0
    )
}