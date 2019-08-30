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


    fun executeVoteAdminQuestion(
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
        db.collection("admins_questions").document(mainPostId)
            .set(mapOf("score_items" to mapOf(initiatorId to vote)), SetOptions.merge()).addOnSuccessListener {

                when (vote) {

                    -1 -> {
                        changeReputation(
                            20,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
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
                                    21,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    "vote",
                                    activity,
                                    currentCommunity
                                )
                                firebaseAnalytics.logEvent("question_downvote_cancelled", null)
                            }
                            1 -> {
                                changeReputation(
                                    17,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
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
                            16,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
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


    fun executeVoteAdminAnswer(
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
        db.collection("admins_answers").document(specificPostId)
            .set(mapOf("score_items" to mapOf(initiatorId to vote)), SetOptions.merge()).addOnSuccessListener {

                when (vote) {

                    -1 -> {
                        changeReputation(
                            22,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
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
                                    23,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    "vote",
                                    activity,
                                    currentCommunity
                                )
                                firebaseAnalytics.logEvent("answer_downvote_cancelled", null)
                            }
                            1 -> {
                                changeReputation(
                                    19,
                                    specificPostId,
                                    mainPostId,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
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
                            18,
                            specificPostId,
                            mainPostId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
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


}
