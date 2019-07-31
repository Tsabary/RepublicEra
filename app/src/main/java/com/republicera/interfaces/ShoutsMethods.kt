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
        userReputationView: TextView,
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


    /*
    fun executeShoutLike(
        shout: Shout,
        initiatorId: String,
        initiatorName: String,
        initiatorImage: String,
        likeCount: TextView,
        likeButton: ImageButton,
        receiverId: String,
        userReputationView: TextView,
        activity: Activity
    ) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        val db = FirebaseFirestore.getInstance().collection("shout_likes").document(shout.id)
        val shoutDB = FirebaseFirestore.getInstance().collection("shouts").document(shout.id)

        executeLikeForFastResponse(likeButton, likeCount)

        db.get().addOnSuccessListener {
            val doc = it.data
            if (doc != null) {
                val likesList = doc["likes_list"] as MutableList<String>
                if (likesList.contains(initiatorId)) {
                    db.update("likes_list", FieldValue.arrayRemove(initiatorId)).addOnSuccessListener {

                        shoutDB.update("likes", FieldValue.increment(-1)).addOnSuccessListener {
                            changeReputation(
                                13,
                                shout.id,
                                shout.id,
                                initiatorId,
                                initiatorName,
                                initiatorImage,
                                receiverId,
                                userReputationView,
                                "shoutLike",
                                activity
                            )

                            firebaseAnalytics.logEvent("shout_unliked", null)
                        }
                    }

                } else {
                    db.update("likes_list", FieldValue.arrayUnion(initiatorId)).addOnSuccessListener {

                        shoutDB.update("likes", FieldValue.increment(1)).addOnSuccessListener {
                            changeReputation(
                                12,
                                shout.id,
                                shout.id,
                                initiatorId,
                                initiatorName,
                                initiatorImage,
                                receiverId,
                                userReputationView,
                                "shoutLike",
                                activity
                            )

                            firebaseAnalytics.logEvent("shout_liked", null)
                        }
                    }
                }
            } else {
                db.set(mapOf("likes_list" to listOf(initiatorId))).addOnSuccessListener {

                    shoutDB.update("likes", FieldValue.increment(1)).addOnSuccessListener {
                        changeReputation(
                            12,
                            shout.id,
                            shout.id,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            receiverId,
                            userReputationView,
                            "shoutLike",
                            activity
                        )

                        firebaseAnalytics.logEvent("shout_liked", null)
                    }
                }

            }
        }
    }
    */


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


    /*
        fun executeCommentLike(
            comment: ShoutComment,
            initiatorId: String,
            initiatorName: String,
            initiatorImage: String,
            likeCount: TextView,
            likeButton: ImageButton,
            receiverId: String,
            userReputationView: TextView,
            activity: Activity
        ) {

            val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

            val db = FirebaseFirestore.getInstance().collection("shout_comments_likes").document(comment.id)
            val commentDB = FirebaseFirestore.getInstance().collection("shout_comments").document(comment.id)

            executeLikeForFastResponse(likeButton, likeCount)

            db.get().addOnSuccessListener {
                val doc = it.data
                if (doc != null) {
                    val likesList = doc["likes_list"] as MutableList<String>
                    if (likesList.contains(initiatorId)) {
                        db.update("likes_list", FieldValue.arrayRemove(initiatorId)).addOnSuccessListener {

                            commentDB.update("likes", FieldValue.increment(-1)).addOnSuccessListener {
                                changeReputation(
                                    17,
                                    comment.id,
                                    comment.shout_ID,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    userReputationView,
                                    "shoutCommentLike",
                                    activity
                                )

                                firebaseAnalytics.logEvent("shout_unliked", null)
                            }
                        }
                    } else {
                        db.update("likes_list", FieldValue.arrayUnion(initiatorId)).addOnSuccessListener {

                            commentDB.update("likes", FieldValue.increment(1)).addOnSuccessListener {
                                changeReputation(
                                    16,
                                    comment.id,
                                    comment.shout_ID,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    receiverId,
                                    userReputationView,
                                    "shoutCommentLike",
                                    activity
                                )

                                firebaseAnalytics.logEvent("shout_liked", null)
                            }
                        }
                    }
                } else {
                    db.set(mapOf("likes_list" to listOf(initiatorId))).addOnSuccessListener {

                        commentDB.update("likes", FieldValue.increment(1)).addOnSuccessListener {
                            changeReputation(
                                6,
                                comment.id,
                                comment.shout_ID,
                                initiatorId,
                                initiatorName,
                                initiatorImage,
                                receiverId,
                                userReputationView,
                                "shoutCommentLike",
                                activity
                            )

                            firebaseAnalytics.logEvent("shout_liked", null)
                        }
                    }
                }
            }
        }
    */
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

//    fun listenToLikeCount(shout: Shout, currentProfile: User, likeButton: ImageButton, likeCount: TextView) {
//        val db = FirebaseFirestore.getInstance()
//
//        db.collection("shout_likes").document(shout.id).get().addOnSuccessListener {
//            val doc = it.data
//
//            if (doc != null) {
//                val likeList = doc["likes_list"] as List<String>
//
//                if (likeList.contains(currentProfile.uid)) {
//                    likedView(likeButton, likeCount)
//                } else {
//                    notLikedView(likeButton, likeCount)
//                }
//
//                likeCount.text = if (likeList.isNotEmpty()) {
//                    likeList.size.toString()
//                } else {
//                    "0"
//                }
//            }
//        }
//    }


}