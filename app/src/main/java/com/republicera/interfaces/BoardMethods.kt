package com.republicera.interfaces

import android.app.Activity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.R

interface BoardMethods : GeneralMethods {

    fun executeVoteQuestion(
        vote: Int,
        mainPostId: String,
        initiatorId: String,
        initiatorName: String,
        initiatorImage: String,
        receiverId: String,
        votesView: TextView,
        upvoteView: ImageButton,
        downvoteView: ImageButton,
        specificPostId: String,
        userReputationView: TextView,
        activity: Activity,
        previousVote: Int,
        currentCommunity: String
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        val db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity)
        db.collection("questions").document(mainPostId)
            .set(mapOf("score_items" to mapOf(initiatorId to vote)), SetOptions.merge()).addOnSuccessListener {

                when (vote) {

                    -1 -> {
                        changeReputation(
                            4,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
                            userReputationView,
                            "vote",
                            activity,
                            currentCommunity
                        )
                        downView(upvoteView, downvoteView)
                        firebaseAnalytics.logEvent("question_downvote", null)
                    }

                    0 -> {
                        when (previousVote) {
                            -1 -> {
                                changeReputation(
                                    5,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    userReputationView,
                                    "vote",
                                    activity,
                                    currentCommunity
                                )
                                firebaseAnalytics.logEvent("question_downvote_cancelled", null)
                            }
                            1 -> {
                                changeReputation(
                                    1,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    userReputationView,
                                    "vote",
                                    activity,
                                    currentCommunity
                                )

                                firebaseAnalytics.logEvent("question_upvote_cancelled", null)
                            }
                        }
                        defaultView(upvoteView, downvoteView)
                    }

                    1 -> {
                        changeReputation(
                            0,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
                            userReputationView,
                            "vote",
                            activity,
                            currentCommunity
                        )
                        upView(upvoteView, downvoteView)
                        firebaseAnalytics.logEvent("question_upvote", null)
                    }
                }
            }
    }


    fun executeVoteAnswer(
        vote: Int,
        mainPostId: String,
        initiatorId: String,
        initiatorName: String,
        initiatorImage: String,
        receiverId: String,
        votesView: TextView,
        upvoteView: ImageButton,
        downvoteView: ImageButton,
        specificPostId: String,
        userReputationView: TextView,
        activity: Activity,
        previousVote: Int,
        currentCommunity: String
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        val db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity)
        db.collection("answers").document(specificPostId)
            .set(mapOf("score_items" to mapOf(initiatorId to vote)), SetOptions.merge()).addOnSuccessListener {

                when (vote) {

                    -1 -> {
                        changeReputation(
                            6,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
                            userReputationView,
                            "vote",
                            activity,
                            currentCommunity
                        )
                        downView(upvoteView, downvoteView)
                        firebaseAnalytics.logEvent("question_downvote", null)
                    }

                    0 -> {
                        when (previousVote) {
                            -1 -> {
                                changeReputation(
                                    7,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    userReputationView,
                                    "vote",
                                    activity,
                                    currentCommunity
                                )
                                firebaseAnalytics.logEvent("answer_downvote_cancelled", null)
                            }
                            1 -> {
                                changeReputation(
                                    3,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    userReputationView,
                                    "vote",
                                    activity,
                                    currentCommunity
                                )

                                firebaseAnalytics.logEvent("answer_upvote_cancelled", null)
                            }
                        }
                        defaultView(upvoteView, downvoteView)
                    }

                    1 -> {
                        changeReputation(
                            2,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
                            userReputationView,
                            "vote",
                            activity,
                            currentCommunity
                        )
                        upView(upvoteView, downvoteView)
                        firebaseAnalytics.logEvent("answer_upvote", null)
                    }
                }
            }

    }


    fun upView(upvoteView: ImageView, downvoteView: ImageView) {
        upvoteView.setImageResource(R.drawable.arrow_up_active)
        upvoteView.tag = "active"
        downvoteView.setImageResource(R.drawable.arrow_down_default)
        downvoteView.tag = "unactive"
    }

    fun defaultView(upvoteView: ImageView, downvoteView: ImageView) {
        upvoteView.setImageResource(R.drawable.arrow_up_default)
        upvoteView.tag = "unactive"
        downvoteView.setImageResource(R.drawable.arrow_down_default)
        downvoteView.tag = "unactive"
    }

    fun downView(upvoteView: ImageButton, downvoteView: ImageButton) {
        upvoteView.setImageResource(R.drawable.arrow_up_default)
        upvoteView.tag = "unactive"
        downvoteView.setImageResource(R.drawable.arrow_down_active)
        downvoteView.tag = "active"
    }

    fun setQuestionLanguage(code: String): String {

        return when (code) {
            "af" -> "Afrikaans"
            "am" -> "Amharic"
            "ar" -> "Arabic"
            "ar-Latn" -> "Arabic"
            "az" -> "Azerbaijani"
            "be" -> "Belarusian"
            "bg" -> "Bulgarian"
            "bg-Latn" -> "Bulgarian"
            "bn" -> "Bengali"
            "bs" -> "Bosnian"
            "ca" -> "Catalan"
            "ceb" -> "Cebuano"
            "co" -> "Corsican"
            "cs" -> "Czech"
            "cy" -> "Welsh"
            "da" -> "Danish"
            "de" -> "German"
            "el" -> "Greek"
            "el-Latn" -> "Greek"
            "en" -> "English"
            "eo" -> "Esperanto"
            "es" -> "Spanish"
            "et" -> "Estonian"
            "eu" -> "Basque"
            "fa" -> "Persian"
            "fi" -> "Finnish"
            "fil" -> "Filipino"
            "fr" -> "French"
            "fy" -> "Western Frisian"
            "ga" -> "Irish"
            "gd" -> "Scots Gaelic"
            "gl" -> "Galician"
            "gu" -> "Gujarati"
            "ha" -> "Hausa"
            "haw" -> "Hawaiian"
            "hi" -> "Hindi"
            "hi-Latn" -> "Hindi"
            "hmn" -> "Hmong"
            "hr" -> "Croatian"
            "ht" -> "Haitian"
            "hu" -> "Hungarian"
            "hy" -> "Armenian"
            "id" -> "Indonesian"
            "ig" -> "Igbo"
            "is" -> "Icelandic"
            "it" -> "Italian"
            "iw" -> "Hebrew"
            "ja" -> "Japanese"
            "ja-Latn" -> "Japanese"
            "jv" -> "Javanese"
            "ka" -> "Georgian"
            "kk" -> "Kazakh"
            "km" -> "Khmer"
            "kn" -> "Kannada"
            "ko" -> "Korean"
            "ku" -> "Kurdish"
            "ky" -> "Kyrgyz"
            "la" -> "Latin"
            "lb" -> "Luxembourgish"
            "lo" -> "Lao"
            "lt" -> "Lithuanian"
            "lv" -> "Latvian"
            "mg" -> "Malagasy"
            "mi" -> "Maori"
            "mk" -> "Macedonian"
            "ml" -> "Malayalam"
            "mn" -> "Mongolian"
            "mr" -> "Marathi"
            "ms" -> "Malay"
            "mt" -> "Maltese"
            "my" -> "Burmese"
            "ne" -> "Nepali"
            "nl" -> "Dutch"
            "no" -> "Norwegian"
            "ny" -> "Nyanja"
            "pa" -> "Punjabi"
            "pl" -> "Polish"
            "ps" -> "Pashto"
            "pt" -> "Portuguese"
            "ro" -> "Romanian"
            "ru" -> "Russian"
            "ru-Latn" -> "Russian"
            "sd" -> "Sindhi"
            "si" -> "Sinhala"
            "sk" -> "Slovak"
            "sl" -> "Slovenian"
            "sm" -> "Samoan"
            "sn" -> "Shona"
            "so" -> "Somali"
            "sq" -> "Albanian"
            "sr" -> "Serbian"
            "st" -> "Sesotho"
            "su" -> "Sundanese"
            "sv" -> "Swedish"
            "sw" -> "Swahili"
            "ta" -> "Tamil"
            "te" -> "Telugu"
            "tg" -> "Tajik"
            "th" -> "Thai"
            "tr" -> "Turkish"
            "uk" -> "Ukrainian"
            "ur" -> "Urdu"
            "uz" -> "Uzbek"
            "vi" -> "Vietnamese"
            "xh" -> "Xhosa"
            "yi" -> "Yiddish"
            "yo" -> "Yoruba"
            "zh" -> "Chinese"
            "zh-Latn" -> "Chinese"
            "zu" -> "Zulu"
            else -> "Please choose"
        }
    }
}
