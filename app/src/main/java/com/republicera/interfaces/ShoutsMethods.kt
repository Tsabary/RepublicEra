package com.republicera.interfaces

import android.app.Activity
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.R
import com.republicera.models.Shout
import com.republicera.models.ShoutComment

interface ShoutsMethods : GeneralMethods {

    fun executeShoutLike(
        shout: Shout,
        initiatorId: String,
        initiatorName: String,
        initiatorImage: String,
        likeCount: TextView,
        likeButton: ImageButton,
        receiverId: String,
        activity: Activity,
        currentCommunity: String
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        val db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity)
            .collection("shouts").document(shout.id)

        executeLikeForFastResponse(likeButton, likeCount)

        if (shout.likes.contains(initiatorId)) {

                db.update("likes", FieldValue.arrayRemove(initiatorId))
                    .addOnSuccessListener {
                        changeReputation(
                            13,
                            shout.id,
                            shout.id,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
                            "shoutLike",
                            activity,
                            currentCommunity
                        )

                        firebaseAnalytics.logEvent("shout_unliked", null)

                    }




        } else {
                db.update("likes", FieldValue.arrayUnion(initiatorId))
                    .addOnSuccessListener {

                        changeReputation(
                            12,
                            shout.id,
                            shout.id,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
                            "shoutLike",
                            activity,
                            currentCommunity
                        )

                        firebaseAnalytics.logEvent("shout_liked", null)
                    }

        }
    }



    fun executeCommentLike(
        comment: ShoutComment,
        initiatorId: String,
        initiatorName: String,
        initiatorImage: String,
        likeCount: TextView,
        likeButton: ImageButton,
        receiverId: String,
        userReputationView: TextView,
        activity: Activity,
        currentCommunity: String
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        val db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity)
            .collection("shout_comments").document(comment.id)

        executeLikeForFastResponse(likeButton, likeCount)

        if (comment.likes.contains(initiatorId)) {
            db.update("likes", FieldValue.arrayRemove(initiatorId)).addOnSuccessListener {
                changeReputation(
                    15,
                    comment.id,
                    comment.shout_ID,
                    initiatorId,
                    initiatorName,
                    initiatorImage,
                    receiverId,
                    "shoutCommentLike",
                    activity,
                    currentCommunity
                )

                firebaseAnalytics.logEvent("shout_unliked", null)
            }
        } else {
            db.update("likes", FieldValue.arrayUnion(initiatorId)).addOnSuccessListener {
                changeReputation(
                    14,
                    comment.id,
                    comment.shout_ID,
                    initiatorId,
                    initiatorName,
                    initiatorImage,
                    receiverId,
                    "shoutCommentLike",
                    activity,
                    currentCommunity
                )

                firebaseAnalytics.logEvent("shout_liked", null)
            }
        }
    }


    fun executeLikeForFastResponse(likeButton: ImageButton, likeCount: TextView) {
        if (likeButton.tag == "liked") {
            notLikedView(likeButton, likeCount)
        } else {
            likedView(likeButton, likeCount)
        }
    }


    fun likedView(likeButton: ImageButton, likeCount: TextView) {
        likeButton.setImageResource(R.drawable.heart_active)
        likeButton.tag = "liked"

        val currentLikeCount = if (likeCount.text.isNotEmpty()) {
            likeCount.text.toString().toInt()
        } else {
            0
        }

        if (currentLikeCount < 999) {
            val updatedLikeCount = currentLikeCount + 1
            likeCount.text = updatedLikeCount.toString()
        }
    }

    fun notLikedView(likeButton: ImageButton, likeCount: TextView) {
        likeButton.setImageResource(R.drawable.heart)
        likeButton.tag = "notLiked"

        val currentLikeCount = if (likeCount.text.isNotEmpty()) {
            likeCount.text.toString().toInt()
        } else {
            0
        }

        if (currentLikeCount < 999) {
            val updatedLikeCount = currentLikeCount - 1
            likeCount.text = updatedLikeCount.toString()
        }
    }
}