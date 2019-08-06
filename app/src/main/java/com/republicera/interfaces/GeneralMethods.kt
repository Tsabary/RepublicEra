package com.republicera.interfaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.models.Notification
import com.republicera.models.ReputationScore
import com.republicera.services.FCMMethods.sendMessageTopic
import com.republicera.services.FCMMethods.sendNotification
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

interface GeneralMethods {


//    fun sendNotification(
//        postType: Int,
//        scenarioType: Int,
//        initiatorId: String,
//        initiatorName: String,
//        initiatorImage: String,
//        mainPostId: String,
//        specificPostId: String,
//        receiverId: String,
//        currentCommunity: String,
//        collection: String
//    ): Task<Void> {
//        val db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity)
//
//        val docRef = db.collection("notifications").document(receiverId).collection(collection)
//            .document(mainPostId + specificPostId + initiatorId + scenarioType)
//
//        val newNotification = Notification(
//            docRef.id,
//            postType,
//            scenarioType,
//            initiatorId,
//            initiatorName,
//            initiatorImage,
//            mainPostId,
//            specificPostId,
//            System.currentTimeMillis(),
//            0
//        )
//
//        return docRef.set(newNotification)
//    }


    fun changeReputation(
        scenarioType: Int,
        specificPostId: String,
        mainPostId: String,
        initiatorId: String,
        initiatorName: String,
        initiatorImage: String,
        receiverId: String,
        action: String,
        activity: Activity,
        currentCommunity: String
    ) {

        val db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity)

        val reputationDoc = db.collection("reputation_events").document()

        val queryBase = db.collection("reputation_events")
            .whereEqualTo("initiator_ID", initiatorId)
            .whereEqualTo("main_post_ID", mainPostId)
            .whereEqualTo("specific_post_ID", specificPostId)


        when (scenarioType) {


            //0: question upvote +5 to receiver +notification // type 0
            0 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        0,
                        0,
                        "board",
                        5,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //1: question upvote is removed -5 to receiver
            1 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 0)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //2: answer upvoted +10 to receiver +notification  // type 1
            2 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        1,
                        2,
                        "board",
                        10,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //3: answer upvote is removed -10 to receiver
            3 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 2)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }



            //4: question downvote -2 for receiver -1 for initiator +notification without initiator  // type 0
            4 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        0,
                        4,
                        "board",
                        -2,
                        FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    db.collection("reputation_events").document().set(
                        ReputationScore(
                            initiatorId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            0,
                            4,
                            "board",
                            -1,
                            FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            //5: question downvote is removed +2 for receiver +1 for initiator
            5 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 4)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 4)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //6: answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 1
            6 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        1,
                        6,
                        "board",
                        -2,
                        FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    db.collection("reputation_events").document().set(
                        ReputationScore(
                            initiatorId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            1,
                            6,
                            "board",
                            -1,
                            FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            //7: answer downvote is removed +2 for receiver +1 for initiator
            7 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 6)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 6)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //8: answer given +2 to initiator
            8 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        1,
                        8,
                        "board",
                        2,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //9: answer removed -2 to initiator
            9 -> {
                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 8)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //10: question bookmark +5 to receiver +notification  // type 0
            10 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        0,
                        10,
                        "board",
                        5,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //11: question unsaved -5 to receiver
            11 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 10)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }


            //12: shout liked
            12 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        2,
                        12,
                        "shouts",
                        1,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //13: shout like removed
            13 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 2)
                    .whereEqualTo("scenario_type", 12)
                    .whereEqualTo("collection", "shouts").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }


            //14: comment on a shout liked
            14 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        3,
                        14,
                        "shouts",
                        1,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //15: comment on a shout like removed
            15 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 3)
                    .whereEqualTo("scenario_type", 14)
                    .whereEqualTo("collection", "shouts").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }


            //16: admin question upvote +5 to receiver +notification // type 0
            16 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        0,
                        16,
                        "admins_board",
                        5,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //17: admin question upvote is removed -5 to receiver
            17 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 16)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //18: admin answer upvoted +10 to receiver +notification  // type 1
            18 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        1,
                        18,
                        "admins_board",
                        10,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //19: admin answer upvote is removed -10 to receiver
            19 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 18)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }



            //20: admin question downvote -2 for receiver -1 for initiator +notification without initiator  // type 0
            20 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        0,
                        20,
                        "admins_board",
                        -2,
                        FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    db.collection("reputation_events").document().set(
                        ReputationScore(
                            initiatorId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            0,
                            20,
                            "admins_board",
                            -1,
                            FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            //21: admin question downvote is removed +2 for receiver +1 for initiator
            21 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 20)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 20)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //22: admin answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 1
            22 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        1,
                        22,
                        "admins_board",
                        -2,
                        FieldValue.serverTimestamp()
                    )
                ).addOnSuccessListener {
                    db.collection("reputation_events").document().set(
                        ReputationScore(
                            initiatorId,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            1,
                            22,
                            "admins_board",
                            -1,
                            FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            //23: admin answer downvote is removed +2 for receiver +1 for initiator
            23 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 22)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 22)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //24: admin answer given +2 to initiator
            24 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        1,
                        24,
                        "admins_board",
                        2,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //25: admin answer removed -2 to initiator
            25 -> {
                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 24)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }

            //26: admin question bookmark +5 to receiver +notification  // type 0
            26 -> {
                reputationDoc.set(
                    ReputationScore(
                        receiverId,
                        initiatorId,
                        initiatorName,
                        initiatorImage,
                        mainPostId,
                        specificPostId,
                        0,
                        26,
                        "admins_board",
                        5,
                        FieldValue.serverTimestamp()
                    )
                )
            }

            //27: admin question unsaved -5 to receiver
            27 -> {
                queryBase
                    .whereEqualTo("receiver_ID", receiverId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 26)
                    .whereEqualTo("collection", "admins_board").get().addOnSuccessListener {
                        for(doc in it){
                            doc.reference.delete()
                        }
                    }
            }


        }



    }


/*
    fun changeReputation(
        scenarioType: Int,
        specificPostId: String,
        mainPostId: String,
        initiatorId: String,
        initiatorName: String,
        initiatorImage: String,
        receiverId: String,
        userReputationView: TextView,
        action: String,
        activity: Activity,
        currentCommunity: String
    ) {

        val db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity)

        val receiverReputationDoc =
            db.collection("reputation_events").document(specificPostId + initiatorId + action)
        val initiatorReputationDoc =
            db.collection("reputation_events").document(specificPostId + action)

        val receiverReputationCounter = db.collection("reputation_counter").document(receiverId)
        val initiatorReputationCounter = db.collection("reputation_counter").document(receiverId)

        val boardCollection =
            db.collection("notifications").document(receiverId).collection("board")
        val shoutsCollection =
            db.collection("notifications").document(receiverId).collection("shouts")


        when (scenarioType) {


            //0: question upvote +5 to receiver +notification // type 0
            0 -> {
                receiverReputationDoc.set(
                    ReputationScore(
                        0,
                        specificPostId,
                        receiverId,
                        initiatorId,
                        5,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    incrementCounter(receiverReputationCounter, 5).addOnSuccessListener {
                        sendNotification(
                            0,
                            0,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            receiverId,
                            currentCommunity,
                            "board"
                        ).addOnSuccessListener {
                            sendMessageTopic(
                                receiverId,
                                initiatorId,
                                mainPostId,
                                activity,
                                "upvoted your question",
                                initiatorName
                            )
                        }
                    }
                }
            }

            //1: question upvote is removed -5 to receiver
            1 -> {
                boardCollection.document(mainPostId + specificPostId + initiatorId + "0").delete()
                    .addOnSuccessListener {
                        receiverReputationDoc.delete().addOnSuccessListener {
                            incrementCounter(receiverReputationCounter, -5)
                        }
                    }
            }

            //2: answer upvoted +10 to receiver +notification  // type 1
            2 -> {
                receiverReputationDoc.set(
                    ReputationScore(
                        1,
                        specificPostId,
                        receiverId,
                        initiatorId,
                        10,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    incrementCounter(receiverReputationCounter, 10).addOnSuccessListener {
                        sendNotification(
                            1,
                            2,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            receiverId,
                            currentCommunity,
                            "board"
                        ).addOnSuccessListener {
                            sendMessageTopic(
                                receiverId,
                                initiatorId,
                                mainPostId,
                                activity,
                                "upvoted your answer",
                                initiatorName
                            )
                        }
                    }
                }
            }

            //3: answer upvote is removed -10 to receiver
            3 -> {
                boardCollection.document(mainPostId + specificPostId + initiatorId + "2").delete()
                    .addOnSuccessListener {
                        receiverReputationDoc.delete().addOnSuccessListener {
                            incrementCounter(receiverReputationCounter, -10)
                        }
                    }
            }

            //4: question downvote -2 for receiver -1 for initiator +notification without initiator  // type 0
            4 -> {
                receiverReputationDoc.set(
                    ReputationScore(
                        0,
                        specificPostId,
                        receiverId,
                        initiatorId,
                        -2,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    initiatorReputationDoc.set(
                        ReputationScore(
                            0,
                            specificPostId,
                            initiatorId,
                            initiatorId,
                            -1,
                            System.currentTimeMillis()
                        )
                    ).addOnSuccessListener {
                        incrementCounter(receiverReputationCounter, -2).addOnSuccessListener {
                            incrementCounter(initiatorReputationCounter, -1).addOnSuccessListener {
                                sendNotification(
                                    0,
                                    4,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    mainPostId,
                                    specificPostId,
                                    receiverId,
                                    currentCommunity,
                                    "board"
                                ).addOnSuccessListener {
                                    sendMessageTopic(
                                        receiverId,
                                        initiatorId,
                                        mainPostId,
                                        activity,
                                        "downvoted your question",
                                        initiatorName
                                    )
                                }
                            }
                        }
                    }
                }
            }

            //5: question/answer downvote is removed +2 for receiver +1 for initiator
            5 -> {
                boardCollection.document(mainPostId + specificPostId + initiatorId + "4").delete()
                    .addOnSuccessListener {
                        receiverReputationDoc.delete().addOnSuccessListener {
                            initiatorReputationDoc.delete().addOnSuccessListener {
                                incrementCounter(receiverReputationCounter, 2).addOnSuccessListener {
                                    incrementCounter(initiatorReputationCounter, 1)
                                }
                            }
                        }
                    }
            }

            //6: answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 1
            6 -> {
                receiverReputationDoc.set(
                    ReputationScore(
                        1,
                        specificPostId,
                        receiverId,
                        initiatorId,
                        -2,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    initiatorReputationDoc.set(
                        ReputationScore(
                            1,
                            specificPostId,
                            initiatorId,
                            initiatorId,
                            -1,
                            System.currentTimeMillis()
                        )
                    ).addOnSuccessListener {
                        incrementCounter(receiverReputationCounter, -2).addOnSuccessListener {
                            incrementCounter(initiatorReputationCounter, -1).addOnSuccessListener {
                                sendNotification(
                                    1,
                                    6,
                                    initiatorId,
                                    initiatorName,
                                    initiatorImage,
                                    mainPostId,
                                    specificPostId,
                                    receiverId,
                                    currentCommunity,
                                    "board"
                                ).addOnSuccessListener {
                                    sendMessageTopic(
                                        receiverId,
                                        initiatorId,
                                        mainPostId,
                                        activity,
                                        "downvoted your answer",
                                        initiatorName
                                    )
                                }
                            }
                        }
                    }
                }
            }

            //7: question/answer downvote is removed +2 for receiver +1 for initiator
            7 -> {
                boardCollection.document(mainPostId + specificPostId + initiatorId + "4").delete()
                    .addOnSuccessListener {
                        receiverReputationDoc.delete().addOnSuccessListener {
                            initiatorReputationDoc.delete().addOnSuccessListener {
                                incrementCounter(receiverReputationCounter, 2).addOnSuccessListener {
                                    incrementCounter(initiatorReputationCounter, 1)
                                }
                            }
                        }
                    }
            }

            //8: answer given +2 to initiator
            8 -> {
                initiatorReputationDoc.set(
                    ReputationScore(
                        1,
                        specificPostId,
                        initiatorId,
                        initiatorId,
                        2,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    incrementCounter(initiatorReputationCounter, 2).addOnSuccessListener {
                        sendNotification(
                            1,
                            8,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            receiverId,
                            currentCommunity,
                            "board"
                        ).addOnSuccessListener {
                            sendMessageTopic(
                                receiverId,
                                initiatorId,
                                mainPostId,
                                activity,
                                "answered your question",
                                initiatorName
                            )
                        }
                    }
                }
            }

            //9: answer removed -2 to initiator
            9 -> {
                boardCollection.document(mainPostId + specificPostId + initiatorId + "6").delete()
                    .addOnSuccessListener {
                        initiatorReputationDoc.delete().addOnSuccessListener {
                            incrementCounter(initiatorReputationCounter, -2)
                        }
                    }
            }

            //10: question bookmark +5 to receiver +notification  // type 0
            10 -> {
                receiverReputationDoc.set(
                    ReputationScore(
                        0,
                        specificPostId,
                        receiverId,
                        initiatorId,
                        5,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    incrementCounter(receiverReputationCounter, 5).addOnSuccessListener {
                        sendNotification(
                            0,
                            10,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            receiverId,
                            currentCommunity,
                            "board"
                        )
                    }
                }
            }

            //11: question unsaved -5 to receiver
            11 -> {
                boardCollection.document(mainPostId + specificPostId + initiatorId + "8").delete()
                    .addOnSuccessListener {
                        receiverReputationDoc.delete().addOnSuccessListener {
                            incrementCounter(receiverReputationCounter, -5)
                        }
                    }
            }

            //12: answer receives a comment
            12 -> {
                sendNotification(
                    1,
                    12,
                    initiatorId,
                    initiatorName,
                    initiatorImage, mainPostId,
                    specificPostId,
                    receiverId,
                    currentCommunity,
                    "board"
                ).addOnSuccessListener {
                    sendMessageTopic(
                        receiverId,
                        initiatorId,
                        mainPostId,
                        activity,
                        "commented on your answer",
                        initiatorName
                    )
                }
            }

            //13: comment on answer removed // needs to fix. causes a problem with the id of the comment as all other actions could be prformed only once, but here it might overwrite it
            13 -> {
                boardCollection.document(mainPostId + specificPostId + initiatorId + "10").delete()
            }

            //14: shout liked
            14 -> {
                receiverReputationDoc.set(
                    ReputationScore(
                        2,
                        specificPostId,
                        receiverId,
                        initiatorId,
                        1,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    incrementCounter(receiverReputationCounter, 1).addOnSuccessListener {
                        sendNotification(
                            2,
                            14,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            receiverId,
                            currentCommunity,
                            "shouts"
                        ).addOnSuccessListener {
                            sendMessageTopic(
                                receiverId,
                                initiatorId,
                                mainPostId,
                                activity,
                                "liked your shout",
                                initiatorName
                            )
                        }
                    }
                }
            }

            //15: shout like removed
            15 -> {
                shoutsCollection.document(mainPostId + specificPostId + initiatorId + "12").delete()
                    .addOnSuccessListener {
                        receiverReputationDoc.delete().addOnSuccessListener {
                            incrementCounter(receiverReputationCounter, -1)
                        }
                    }
            }

            //16: comment on a shout
            16 -> {
                sendNotification(
                    2,
                    16,
                    initiatorId,
                    initiatorName,
                    initiatorImage,
                    mainPostId,
                    specificPostId,
                    receiverId,
                    currentCommunity,
                    "shouts"
                ).addOnSuccessListener {
                    sendMessageTopic(
                        receiverId,
                        initiatorId,
                        mainPostId,
                        activity,
                        "commented on your shout",
                        initiatorName
                    )
                }
            }

            //17: comment on a shout removed
            17 -> {
                shoutsCollection.document(mainPostId + specificPostId + initiatorId + "14").delete()
            }

            //18: comment on a shout liked
            18 -> {
                receiverReputationDoc.set(
                    ReputationScore(
                        3,
                        specificPostId,
                        receiverId,
                        initiatorId,
                        1,
                        System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    incrementCounter(receiverReputationCounter, 1).addOnSuccessListener {
                        sendNotification(
                            3,
                            18,
                            initiatorId,
                            initiatorName,
                            initiatorImage,
                            mainPostId,
                            specificPostId,
                            receiverId,
                            currentCommunity,
                            "shouts"
                        ).addOnSuccessListener {
                            sendMessageTopic(
                                receiverId,
                                initiatorId,
                                mainPostId,
                                activity,
                                "liked your comment",
                                initiatorName
                            )
                        }
                    }
                }
            }

            //19: comment on a shout like removed
            19 -> {
                shoutsCollection.document(mainPostId + specificPostId + initiatorId + "16").delete()
                    .addOnSuccessListener {
                        receiverReputationDoc.delete().addOnSuccessListener {
                            incrementCounter(receiverReputationCounter, -1)
                        }
                    }
            }

            //20: profile got a follow
            20 -> {
                sendNotification(
                    4,
                    20,
                    initiatorId,
                    initiatorName,
                    initiatorImage,
                    mainPostId,
                    specificPostId,
                    receiverId,
                    currentCommunity,
                    "shouts"
                ).addOnSuccessListener {
                    sendMessageTopic(
                        receiverId,
                        initiatorId,
                        mainPostId,
                        activity,
                        "started following you",
                        initiatorName
                    )
                }
            }

            //21: profile follow removed
            21 -> {
                shoutsCollection.document(mainPostId + specificPostId + initiatorId + "20").delete()
            }
        }
    }
    */


    //0: question upvote +5 to receiver +notification // type 0
    //1: question upvote is removed -5 to receiver
    //2: answer upvoted +10 to receiver +notification  // type 1
    //3: answer upvote is removed -10 to receiver
    //4: question downvote -2 for receiver -1 for initiator +notification without initiator  // type 0 or 1
    //5: question/answer downvote is removed +2 for receiver +1 for initiator
    //6: answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 0 or 1
    //7: question/answer downvote is removed +2 for receiver +1 for initiator
    //8: answer given +2 to initiator
    //9: answer removed -2 to initiator
    //10: question bookmark +5 to receiver +notification  // type 0
    //11: question unsaved -5 to receiver
    //12: answer receives a comment
    //13: comment on answer removed // needs to fix. causes a problem with the id of the comment as all other actions could be prformed only once, but here it might overwrite it
    //14: shout liked
    //15: shout like removed
    //16: comment on a shout
    //17: comment on a shout removed
    //18: comment on a shout liked
    //19: comment on a shout like removed
    //20: profile got a follow
    //21: profile follow removed


//    fun incrementCounter(ref: DocumentReference, value: Long): Task<Void> {
//        val shardId = floor(Math.random() * 20).toInt()
//        val shardRef = ref.collection("shards").document(shardId.toString())
//
//        return shardRef.update("count", FieldValue.increment(value))
//    }


    fun onClickStak(case: Int, intentData: String, activity: Activity) {

        /*
cases:
email = 0
phone = 1
whatsApp = 2
twitter = 3
instagram = 4
website = 5
linkedin = 6
facebook = 7
medium = 8
youtube = 9
snapchat = 10
*/

        when (case) {
            0 -> {
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto:$intentData")
                activity.startActivity(emailIntent)
            }
            1 -> {
                activity.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$intentData")))
            }
            2 -> {
                val url = "https://api.whatsapp.com/send?phone=$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            3 -> {
                val url = "https://www.twitter.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            4 -> {
                val url = "https://www.instagram.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            5 -> {
                val url = "https://$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            6 -> {
                val url = "https://www.linkedin.com/in/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            7 -> {
                val url = "https://www.facebook.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            8 -> {
                val url = "https://medium.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            9 -> {
                val url = "https://www.youtube.com/user/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            10 -> {
                val url = "https://www.snapchat.com/$intentData"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }


/*
    fun updateUserFinalReputation(id: String, ref: DocumentReference, userReputationView: TextView) {

        FirebaseDatabase.getInstance().getReference("/members/$id/reputation")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    var count = 0

                    for (ds in p0.children) {
                        val score = ds.getValue(ReputationScore::class.java)
                        count += score!!.points
                    }

                    val userObject = FirebaseDatabase.getInstance().getReference("/members/$id/profile")

                    if (count < 0) {
                        userObject.updateChildren(mapOf("reputation" to "0"))
                        userReputationView.text = "(0)"
                    } else {
                        userObject.updateChildren(mapOf("reputation" to count.toLong()))
                        userReputationView.text = "(${numberCalculation(count.toLong())})"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }
*/

    fun closeKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    fun showKeyboard(activity: Activity) {

        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun numberCalculation(number: Long): String {
        if (number < 1000)
            return "" + number
        val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
        return String.format("%.1f %c", number / 1000.0.pow(exp.toDouble()), "kMBTPE"[exp - 1])
    }

}

/*
post types:

0: question
1: answer
2: shout
3: shout comment
4: profile



 */