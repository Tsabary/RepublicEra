package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.mikepenz.materialdrawer.Drawer
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleNotification
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.*
import com.republicera.viewModels.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.toolbar_notifications.*


class NotificationsFragment : Fragment(), GeneralMethods {

    lateinit var db: DocumentReference

    private val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var sharedViewModelQuestion: QuestionViewModel
    private lateinit var sharedViewModelShout: ShoutViewModel
    private lateinit var sharedViewModelRandomUser: RandomUserViewModel

    lateinit var communityProfile: CommunityProfile
    lateinit var topLevelUser: User

    lateinit var currentCommunity: Community
    lateinit var result: Drawer


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_notifications, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            sharedViewModelShout = ViewModelProviders.of(it).get(ShoutViewModel::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            communityProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            ViewModelProviders.of(it).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity,
                Observer { user ->
                    topLevelUser = user
                })

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        setUpDrawerNav(activity, topLevelUser, communityProfile, 2)

        val menuIcon = toolbar_notifications_menu
        menuIcon.setOnClickListener {
            result.openDrawer()
        }

//        val boardNotificationBadge = toolbar_without_search_notifications_badge
//
//        activity.boardNotificationsCount.observe(this, Observer {
//            it?.let { notCount ->
//                boardNotificationBadge.setNumber(notCount)
//            }
//        })

        notifications_swipe_refresh.setOnRefreshListener {
            listenToNotifications(communityProfile)
            notifications_swipe_refresh.isRefreshing = false
        }

        val notificationsRecycler = notifications_recycler
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = LinearLayoutManager(this.context)

//        val boardNotificationIcon = toolbar_without_search_notifications_icon
//        val boardSavedQuestionIcon = toolbar_without_search_saved_icon

        listenToNotifications(communityProfile)

//        boardNotificationIcon.setImageResource(
//            R.drawable.notification_bell_active
//        )
//
//        boardNotificationIcon.setOnClickListener {
//            listenToNotifications(currentProfile)
//        }

//        boardNotificationBadge.setOnClickListener {
//            listenToNotifications(currentProfile)
//        }

//        boardSavedQuestionIcon.setOnClickListener {
//            activity.subFm.beginTransaction().hide(activity.boardNotificationsFragment)
//                .show(activity.savedQuestionFragment).commit()
//            activity.subActive = activity.savedQuestionFragment
//        }


        toolbar_notifications_mark_as_read.setOnClickListener {

            db.collection("notifications").document(communityProfile.uid).collection("all").whereEqualTo("seen", 0)
                .get()
                .addOnSuccessListener {
                    for (doc in it) {
                        doc.reference.update(mapOf("seen" to 1))
                    }
                    notificationsRecyclerAdapter.clear()
                    listenToNotifications(communityProfile)
                }
        }

        notificationsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val notification = item as SingleNotification

            when (notification.notification.collection) {

                "board" -> {
                    db.collection("questions").document(notification.notification.main_post_ID).get()
                        .addOnSuccessListener {

                            val question = it.toObject(Question::class.java)

                            if (question != null) {

                                sharedViewModelQuestion.questionObject.postValue(question)

                                db.collection("profiles").document(question.author_ID).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val user = documentSnapshot.toObject(CommunityProfile::class.java)

                                        if (user != null) {
                                            sharedViewModelRandomUser.randomUserObject.postValue(user)

                                            activity.subFm.beginTransaction().add(
                                                R.id.feed_subcontents_frame_container,
                                                activity.openedQuestionFragment,
                                                "openedQuestionFragment"
                                            ).addToBackStack("openedQuestionFragment").commit()

                                            activity.subActive = activity.openedQuestionFragment
                                            activity.switchVisibility(1)

                                            db.collection("notifications").document(communityProfile.uid).collection("all")
                                                .document(notification.notification_ID).update(mapOf("seen" to 1))
                                                .addOnSuccessListener {
                                                    activity.notificationsFragment.listenToNotifications(communityProfile)
                                                }
                                        }
                                    }
                            } else {
                                db.collection("notifications").document(communityProfile.uid).collection("all")
                                    .document(notification.notification_ID).update(mapOf("seen" to 1))
                                    .addOnSuccessListener {
                                        Toast.makeText(activity, "This post has been removed", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                }

                "shouts" -> {
                    db.collection("shouts").document(notification.notification.main_post_ID).get()
                        .addOnSuccessListener {

                            val shout = it.toObject(Shout::class.java)

                            if (shout != null) {

                                sharedViewModelShout.shoutObject.postValue(shout)

                                db.collection("profiles").document(shout.author_ID).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val user = documentSnapshot.toObject(CommunityProfile::class.java)
                                        if (user != null) {
                                            sharedViewModelRandomUser.randomUserObject.postValue(user)

                                            activity.subFm.beginTransaction().add(
                                                R.id.feed_subcontents_frame_container,
                                                activity.shoutExpendedFragment,
                                                "shoutExpendedFragment"
                                            ).addToBackStack("shoutExpendedFragment").commit()

                                            activity.subActive = activity.shoutExpendedFragment
                                            activity.switchVisibility(1)

                                            db.collection("notifications").document(communityProfile.uid).collection("all")
                                                .document(notification.notification_ID)
                                                .set(mapOf("seen" to 1), SetOptions.merge())
                                                .addOnSuccessListener {
                                                    activity.notificationsFragment.listenToNotifications(
                                                        communityProfile
                                                    )
                                                }
                                        }
                                    }
                            } else {
                                db.collection("notifications").document(communityProfile.uid).collection("all")
                                    .document(notification.notification_ID).update(mapOf("seen" to 1))
                                    .addOnSuccessListener {
                                        Toast.makeText(activity, "This post has been removed", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                }

                "admins" -> {
                    if (currentCommunity.admins.contains(communityProfile.uid)) {

                        db.collection("admins_questions").document(notification.notification.main_post_ID).get()
                            .addOnSuccessListener {

                                val question = it.toObject(Question::class.java)

                                if (question != null) {

                                    sharedViewModelQuestion.questionObject.postValue(question)

                                    db.collection("profiles").document(question.author_ID).get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            val user = documentSnapshot.toObject(CommunityProfile::class.java)

                                            if (user != null) {
                                                sharedViewModelRandomUser.randomUserObject.postValue(user)

                                                activity.subFm.beginTransaction().add(
                                                    R.id.feed_subcontents_frame_container,
                                                    activity.adminsOpenedQuestionFragment,
                                                    "adminsOpenedQuestionFragment"
                                                ).addToBackStack("adminsOpenedQuestionFragment").commit()

                                                activity.subActive = activity.adminsOpenedQuestionFragment
                                                activity.switchVisibility(1)

                                                db.collection("notifications").document(communityProfile.uid)
                                                    .collection("admins")
                                                    .document(notification.notification_ID).update(mapOf("seen" to 1))
                                                    .addOnSuccessListener {
                                                        activity.notificationsFragment.listenToNotifications(communityProfile)
                                                    }
                                            }
                                        }
                                } else {
                                    db.collection("notifications").document(communityProfile.uid).collection("all")
                                        .document(notification.notification_ID).update(mapOf("seen" to 1))
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "This post has been removed", Toast.LENGTH_LONG)
                                                .show()
                                        }
                                }
                            }
                    } else {
                        Toast.makeText(activity, "Access limited to admins only", Toast.LENGTH_LONG)
                            .show()
                    }
                }

            }

        }
    }


    fun listenToNotifications(currentUser: CommunityProfile) {
        val activity = activity as MainActivity

        notificationsRecyclerAdapter.clear()

        db.collection("notifications").document(currentUser.uid).collection("all")
            .orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener {
                for (doc in it) {
                    val notification = doc.toObject(Notification::class.java)

                    notificationsRecyclerAdapter.add(
                        SingleNotification(
                            notification,
                            activity,
                            currentUser,
                            doc.id
                        )
                    )

                    var boardNotCount = 0

                    if (notification.seen == 0) {
                        boardNotCount++
                    }
                    activity.boardNotificationsCount.postValue(boardNotCount)
                }
            }
    }
}


