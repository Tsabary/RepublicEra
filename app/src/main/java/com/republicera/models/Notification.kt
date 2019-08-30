package com.republicera.models

import java.util.*

class Notification(
    val receiver_ID:String,
    val post_type: Int,
    val scenario_type: Int,
    val initiator_ID: String,
    val initiator_name: String,
    val initiator_image: String,
    val main_post_ID: String,
    val specific_post_ID: String,
    val collection : String,
    val timestamp: Date,
    val seen: Int
) {
    constructor() : this("",0, 0, "", "", "", "", "", "", Date(), 0)
}