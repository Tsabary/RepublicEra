package com.republicera.models

class Notification(
    val id: String,
    val post_type: Int,
    val scenario_type: Int,
    val initiator_ID: String,
    val initiator_name: String,
    val initiator_image: String,
    val main_post_ID: String,
    val specific_post_ID: String,
    val timestamp: HashMap<String, String>,
    val seen: Int
) {
    constructor() : this("",0, 0, "", "", "", "", "", hashMapOf(), 0)
}