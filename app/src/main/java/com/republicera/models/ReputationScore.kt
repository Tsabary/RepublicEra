package com.republicera.models

class ReputationScore(
    val case : Int,
    val post_ID: String,
    val receiver_ID: String,
    val initiator_ID: String,
    val points: Int,
    val timestamp: Long
) {
    constructor() : this(0,"", "", "", 0, 0)
}