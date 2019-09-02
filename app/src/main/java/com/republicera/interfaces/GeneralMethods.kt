package com.republicera.interfaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.RegisterLoginActivity
import com.republicera.models.CommunityProfile
import com.republicera.models.Notification
import com.republicera.models.ReputationScore
import com.republicera.models.User
import com.republicera.services.FCMMethods.sendMessageTopic
import com.republicera.services.FCMMethods.sendNotification
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

interface GeneralMethods {


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
                        for (doc in it) {
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
                        for (doc in it) {
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
                        for (doc in it) {
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 4)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for (doc in it) {
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
                        for (doc in it) {
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 6)
                    .whereEqualTo("collection", "board").get().addOnSuccessListener {
                        for (doc in it) {
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
                        for (doc in it) {
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
                        for (doc in it) {
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
                        for (doc in it) {
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
                        for (doc in it) {
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
                        "admins",
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
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
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
                        "admins",
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
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
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
                        "admins",
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
                            "admins",
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
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 0)
                    .whereEqualTo("scenario_type", 20)
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
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
                        "admins",
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
                            "admins",
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
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
                            doc.reference.delete()
                        }
                    }

                queryBase
                    .whereEqualTo("receiver_ID", initiatorId)
                    .whereEqualTo("post_type", 1)
                    .whereEqualTo("scenario_type", 22)
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
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
                        "admins",
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
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
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
                        "admins",
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
                    .whereEqualTo("collection", "admins").get().addOnSuccessListener {
                        for (doc in it) {
                            doc.reference.delete()
                        }
                    }
            }


        }


    }


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


    fun setUpDrawerNav(activity: MainActivity, user: User, profile: CommunityProfile, source: Int) {





        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String?) {
                Glide.with(imageView.context).load(uri).placeholder(placeholder).into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Glide.with(imageView.context).clear(imageView)
            }

            override fun placeholder(ctx: Context, tag: String?): Drawable {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                return when (tag) {
                    DrawerImageLoader.Tags.PROFILE.name -> DrawerUIUtils.getPlaceHolder(ctx)
                    DrawerImageLoader.Tags.ACCOUNT_HEADER.name -> IconicsDrawable(ctx).iconText(" ").backgroundColor(
                        IconicsColor.colorRes(com.mikepenz.materialdrawer.R.color.primary)
                    ).size(IconicsSize.dp(56))
                    "customUrlItem" -> IconicsDrawable(ctx).iconText(" ").backgroundColor(IconicsColor.colorRes(R.color.md_red_500)).size(
                        IconicsSize.dp(56)
                    )
                    //we use the default one for
                    //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()
                    else -> super.placeholder(ctx, tag)
                }
            }
        })








        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)


        val headerResult = if(profile.image.isNotEmpty()){
            AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.color.white)
                .addProfiles(
                    ProfileDrawerItem().withName(profile.name)
                        .withIcon(profile.image)
                )
                .withSelectionListEnabled(false)
                .withCurrentProfileHiddenInList(true)
                .build()
        } else {
            AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.color.white)
                .addProfiles(
                    ProfileDrawerItem().withName(profile.name)
                        .withIcon(R.drawable.user_profile)
                )
                .withSelectionListEnabled(false)
                .withCurrentProfileHiddenInList(true)
                .build()
        }

        val languageItems = mutableListOf<ISubItem<*>>()
        var lanIdentifier: Long = 100

        val sharedPref = activity.getSharedPreferences(activity.getString(R.string.package_name), Context.MODE_PRIVATE)

        for (language in user.lang_list) {
            languageItems.add(
                SecondaryDrawerItem().withIdentifier(lanIdentifier).withName(
                    languageCodeToName(language)
                ).withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                    override fun onItemClick(
                        view: View?,
                        position: Int,
                        drawerItem: IDrawerItem<*>
                    ): Boolean {
                        val editor = sharedPref.edit()
                        editor.putString("last_language", language)
                        editor.apply()
                        activity.boardFragment.listenToQuestions(language)

                        return true
                    }
                })
            )
            lanIdentifier++
        }

        val communityTitle =
            PrimaryDrawerItem().withName(activity.currentCommunity!!.title).withSelectable(false).withEnabled(false)
                .withSelected(false).withDisabledTextColor(R.color.mainColor600)

        val chooseCurrentLanguage = PrimaryDrawerItem().withIdentifier(1).withName("Current language").withSubItems(
            languageItems
        ).withSelectable(false)

//        val expandedQuestion = SecondaryToggleDrawerItem().withName("Expand Questions")
//            .withOnCheckedChangeListener(object : OnCheckedChangeListener {
//                override fun onCheckedChanged(
//                    drawerItem: IDrawerItem<*>,
//                    buttonView: CompoundButton,
//                    isChecked: Boolean
//                ) {
//                    val editor = sharedPref.edit()
//
//                    if(isChecked){
//                        firebaseAnalytics.logEvent("board_block_layout", null)
//                        questionsRecycler.adapter = questionsBlockLayoutAdapter
//
//                        editor.putInt("last_layout", 1)
//                    } else {
//                        firebaseAnalytics.logEvent("board_row_layout", null)
//                        questionsRecycler.adapter = questionsRowLayoutAdapter
//
//                        editor.putInt("last_layout", 0)
//                    }
//                    editor.apply()
//
//                }
//
//            })
//
//        val expandQuestionTwo = SecondarySwitchDrawerItem().withName("Expand Questions").


        val rowsLayout = SecondaryDrawerItem().withName(
            "Rows"
        )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {
                    firebaseAnalytics.logEvent("board_row_layout", null)
                    activity.boardFragment.questionsRecycler.adapter = activity.boardFragment.questionsRowLayoutAdapter

                    val editor = sharedPref.edit()
                    editor.putInt("last_layout", 0)
                    editor.apply()

                    return true
                }
            }).withSelectable(false)


        val blockLayout = SecondaryDrawerItem().withName(
            "Blocks"
        )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {

                    firebaseAnalytics.logEvent("board_block_layout", null)
                    activity.boardFragment.questionsRecycler.adapter =
                        activity.boardFragment.questionsBlockLayoutAdapter

                    val editor = sharedPref.edit()
                    editor.putInt("last_layout", 1)
                    editor.apply()

                    return true
                }
            }).withSelectable(false)


        val boardLayout = PrimaryDrawerItem().withIdentifier(1).withName("Board layout").withSubItems(
            rowsLayout, blockLayout
        ).withSelectable(false).withSelected(false)
//            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
//                override fun onItemClick(
//                    view: View?,
//                    position: Int,
//                    drawerItem: IDrawerItem<*>
//                ): Boolean {
//                    Toast.makeText(activity, "clickk", Toast.LENGTH_LONG).show()
//
//                    when (position) {
//
//                        0 -> {
//                            val editor = sharedPref.edit()
//                            firebaseAnalytics.logEvent("board_row_layout", null)
//                            questionsRecycler.adapter = questionsRowLayoutAdapter
//                            editor.putInt("last_layout", 0)
//                            editor.apply()
//
//                            blockLayout.isSelected = false
//                            Toast.makeText(activity, "board", Toast.LENGTH_LONG).show()
//
//                        }
//
//                        1 -> {
//                            val editor = sharedPref.edit()
//                            firebaseAnalytics.logEvent("board_block_layout", null)
//                            questionsRecycler.adapter = questionsBlockLayoutAdapter
//                            editor.putInt("last_layout", 1)
//                            editor.apply()
//
//                            rowsLayout.isSelected = false
//
//                            Toast.makeText(activity, "row", Toast.LENGTH_LONG).show()
//
//                        }
//
//
//                    }
//
//                    return true
//                }
//            })

        val savedQuestions = PrimaryDrawerItem().withIdentifier(3).withName("Saved Questions")
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    goToSavedQuestions(activity)
                    return false
                }
            }).withSelectable(false)

        val saveShouts = PrimaryDrawerItem().withIdentifier(4).withName("Saved Shouts")
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    goToSavedShouts(activity)
                    return false
                }
            }).withSelectable(false)

        val editInterests =
            PrimaryDrawerItem().withIdentifier(5).withName("Edit interests")
                .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                        goToInterests(activity)
                        return false
                    }
                })
                .withSelectable(false)

        val editProfile =
            PrimaryDrawerItem().withIdentifier(5).withName("Edit profile")
                .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                        goToEditProfile(activity)
                        return false
                    }
                })
                .withSelectable(false)


        val languagePreferences =
            PrimaryDrawerItem().withIdentifier(5).withName("Language preferences")
                .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                        goToLanguagePreferences(activity)
                        return false
                    }
                })
                .withSelectable(false)


        val contactInfo = PrimaryDrawerItem().withIdentifier(6).withName("Contact Information")
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    goToContactInfo(activity)
                    return false
                }
            })
            .withSelectable(false)

        val basicInfo = PrimaryDrawerItem().withIdentifier(6).withName("Basic Information")
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    goToBasicInfo(activity)
                    return false
                }
            })
            .withSelectable(false)


        val switchRepublic = PrimaryDrawerItem().withIdentifier(7).withName("Switch republic")
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {

                    if (activity.subActive == activity.searchFragment) {
                        activity.subFm.popBackStack("searchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    }

                    activity.userFm.beginTransaction()
                        .add(
                            R.id.user_home_frame_container,
                            activity.communitiesHomeFragment,
                            "communitiesHomeFragment"
                        ).addToBackStack("communitiesHomeFragment")
                        .commit()
                    activity.userActive = activity.communitiesHomeFragment
                    activity.switchVisibility(2)

                    return false
                }
            })
            .withSelectable(false)


        val logOut = PrimaryDrawerItem().withIdentifier(6).withName("Logout")
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    logout(activity)
                    return false
                }
            })
            .withSelectable(false)


//var result = when(source){
//    0-> activity.boardFragment.result
//    1-> activity.shoutsFragment.result
//    2-> activity.notificationsFragment.result
//    3-> activity.adminsFragment.result
//    4-> activity.profileCurrentUserFragment.result
//    else -> return

//}
        val result = DrawerBuilder()
            .withActivity(activity)
            .withAccountHeader(headerResult)
            .addDrawerItems(
                communityTitle,
                DividerDrawerItem(),
                chooseCurrentLanguage,
//                expandedQuestion,
                boardLayout,
                DividerDrawerItem(),
                savedQuestions,
                saveShouts,
                DividerDrawerItem(),
                editInterests,
                editProfile,
                DividerDrawerItem(),
                languagePreferences,
                contactInfo,
                basicInfo,
                DividerDrawerItem(),
                switchRepublic,
                logOut
            )
//            .withMultiSelect(true)
            .withCloseOnClick(true)
            .withSelectedItem(20202)
            .build()



        when (source) {
            0 -> {
                activity.boardFragment.result = result
            }
            1 -> {
                activity.shoutsFragment.result = result
            }
            2 -> {
                activity.notificationsFragment.result = result
            }
            3 -> {
                activity.adminsFragment.result = result
            }
            4 -> {
                activity.profileCurrentUserFragment.result = result
            }
            else -> return
        }
    }


    private fun goToSavedQuestions(activity: MainActivity) {

        activity.bottomNavigation.setActiveItem(0)

        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.savedQuestionFragment,
                "savedQuestionFragment"
            )
            .addToBackStack("savedQuestionFragment").commit()

        activity.subActive = activity.savedQuestionFragment
        activity.switchVisibility(1)
        activity.isBoardNotificationsActive = true
    }

    private fun goToSavedShouts(activity: MainActivity) {

        activity.bottomNavigation.setActiveItem(1)

        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.savedShoutsFragment,
                "savedShoutsFragment"
            )
            .addToBackStack("savedShoutsFragment").commit()


        activity.subActive = activity.savedShoutsFragment
        activity.switchVisibility(1)
    }

    private fun goToInterests(activity: MainActivity) {

        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.editInterestsFragment,
                "editInterestsFragment"
            )
            .addToBackStack("editInterestsFragment").commit()


        activity.subActive = activity.editInterestsFragment
        activity.switchVisibility(1)

    }


    private fun goToEditProfile(activity: MainActivity) {

        activity.bottomNavigation.setActiveItem(3)


        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.editProfileFragment,
                "editProfileFragment"
            )
            .addToBackStack("editProfileFragment").commit()


        activity.subActive = activity.editProfileFragment
        activity.switchVisibility(1)

    }

    private fun goToLanguagePreferences(activity: MainActivity) {

        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.editLanguagePreferencesFragment,
                "editLanguagePreferencesFragment"
            )
            .addToBackStack("editLanguagePreferencesFragment").commit()


        activity.subActive = activity.editLanguagePreferencesFragment
        activity.switchVisibility(1)

    }


    private fun goToContactInfo(activity: MainActivity) {

        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.editContactDetailsFragment,
                "editContactDetailsFragment"
            )
            .addToBackStack("editContactDetailsFragment").commit()


        activity.subActive = activity.editContactDetailsFragment
        activity.switchVisibility(1)
    }

    private fun goToBasicInfo(activity: MainActivity) {

        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.editBasicInfoFragment,
                "editBasicInfoFragment"
            )
            .addToBackStack("editBasicInfoFragment").commit()


        activity.subActive = activity.editBasicInfoFragment
        activity.switchVisibility(1)
    }

    private fun logout(activity: MainActivity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

        val uid = FirebaseAuth.getInstance().uid
        FirebaseMessaging.getInstance().unsubscribeFromTopic(uid!!).addOnSuccessListener {
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(activity, gso).signOut().addOnSuccessListener {

                val sharedPref =
                    activity.getSharedPreferences(
                        activity.getString(R.string.package_name),
                        Context.MODE_PRIVATE
                    )


                val editor = sharedPref.edit()
                editor.putString("last_community", "default")
                editor.putString("last_language", "en")
                editor.putInt("last_layout", 0)
                editor.putString("last_feed", "board")
                editor.apply()

                val intent = Intent(activity, RegisterLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
        }
    }


    fun languageCodeToName(code: String): String {

        return when (code) {
            "af" -> "Afrikaans"
            "am" -> "Amharic"
            "ar" -> "Arabic"
//            "ar-Latn" -> "Arabic"
            "az" -> "Azerbaijani"
            "be" -> "Belarusian"
            "bg" -> "Bulgarian"
//            "bg-Latn" -> "Bulgarian"
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
//            "el-Latn" -> "Greek"
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
//            "hi-Latn" -> "Hindi"
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
//            "ja-Latn" -> "Japanese"
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
//            "ru-Latn" -> "Russian"
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
//            "zh-Latn" -> "Chinese"
            "zu" -> "Zulu"
            else -> "Please choose"
        }
    }


    fun languageNameToCode(name: String): String {

        return when (name) {
            "Afrikaans" -> "af"
            "Amharic" -> "am"
            "Arabic" -> "ar"
//            "Arabic" ->"ar-Latn"
            "Azerbaijani" -> "az"
            "Belarusian" -> "be"
            "Bulgarian" -> "bg"
//            "Bulgarian" -> "bg-Latn"
            "Bengali" -> "bn"
            "Bosnian" -> "bs"
            "Catalan" -> "ca"
            "Cebuano" -> "ceb"
            "Corsican" -> "co"
            "Czech" -> "cs"
            "Welsh" -> "cy"
            "Danish" -> "da"
            "German" -> "de"
            "Greek" -> "el"
//           "Greek" -> "el-Latn"
            "English" -> "en"
            "Esperanto" -> "eo"
            "Spanish" -> "es"
            "Estonian" -> "et"
            "Basque" -> "eu"
            "Persian" -> "fa"
            "Finnish" -> "fi"
            "Filipino" -> "fil"
            "French" -> "fr"
            "Western Frisian" -> "fy"
            "Irish" -> "ga"
            "Scots Gaelic" -> "gd"
            "Galician" -> "gl"
            "Gujarati" -> "gu"
            "Hausa" -> "ha"
            "Hawaiian" -> "haw"
            "Hindi" -> "hi"
//           "Hindi" -> "hi-Latn"
            "Hmong" -> "hmn"
            "Croatian" -> "hr"
            "Haitian" -> "ht"
            "Hungarian" -> "hu"
            "Armenian" -> "hy"
            "Indonesian" -> "id"
            "Igbo" -> "ig"
            "Icelandic" -> "is"
            "Italian" -> "it"
            "Hebrew" -> "iw"
            "Japanese" -> "ja"
//          "Japanese" ->   "ja-Latn"
            "Javanese" -> "jv"
            "Georgian" -> "ka"
            "Kazakh" -> "kk"
            "Khmer" -> "km"
            "Kannada" -> "kn"
            "Korean" -> "ko"
            "Kurdish" -> "ku"
            "Kyrgyz" -> "ky"
            "Latin" -> "la"
            "Luxembourgish" -> "lb"
            "Lao" -> "lo"
            "Lithuanian" -> "lt"
            "Latvian" -> "lv"
            "Malagasy" -> "mg"
            "Maori" -> "mi"
            "Macedonian" -> "mk"
            "Malayalam" -> "ml"
            "Mongolian" -> "mn"
            "Marathi" -> "mr"
            "Malay" -> "ms"
            "Maltese" -> "mt"
            "Burmese" -> "my"
            "Nepali" -> "ne"
            "Dutch" -> "nl"
            "Norwegian" -> "no"
            "Nyanja" -> "ny"
            "Punjabi" -> "pa"
            "Polish" -> "pl"
            "Pashto" -> "ps"
            "Portuguese" -> "pt"
            "Romanian" -> "ro"
            "Russian" -> "ru"
//           "Russian" ->  "ru-Latn"
            "Sindhi" -> "sd"
            "Sinhala" -> "si"
            "Slovak" -> "sk"
            "Slovenian" -> "sl"
            "Samoan" -> "sm"
            "Shona" -> "sn"
            "Somali" -> "so"
            "Albanian" -> "sq"
            "Serbian" -> "sr"
            "Sesotho" -> "st"
            "Sundanese" -> "su"
            "Swedish" -> "sv"
            "Swahili" -> "sw"
            "Tamil" -> "ta"
            "Telugu" -> "te"
            "Tajik" -> "tg"
            "Thai" -> "th"
            "Turkish" -> "tr"
            "Ukrainian" -> "uk"
            "Urdu" -> "ur"
            "Uzbek" -> "uz"
            "Vietnamese" -> "vi"
            "Xhosa" -> "xh"
            "Yiddish" -> "yi"
            "Yoruba" -> "yo"
            "Chinese" -> "zh"
//         "Chinese" ->    "zh-Latn"
            "Zulu" -> "zu"
            else -> "Please choose"
        }
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