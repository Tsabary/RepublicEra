package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleNotification
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Notification
import com.republicera.models.Question
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.QuestionViewModel
import com.republicera.viewModels.RandomUserViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.toolbar_without_search.*
import kotlinx.android.synthetic.main.fragment_notifications.*


class BoardNotificationsFragment : Fragment() {

    lateinit var db: DocumentReference


    private val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var sharedViewModelQuestion: QuestionViewModel
    private lateinit var sharedViewModelRandomUser: RandomUserViewModel

    lateinit var currentUser: CommunityProfile

    lateinit var currentCommunity: Community


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_notifications, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val boardNotificationBadge = toolbar_without_search_notifications_badge

        activity.boardNotificationsCount.observe(this, androidx.lifecycle.Observer {
            it?.let { notCount ->
                boardNotificationBadge.setNumber(notCount)
            }
        })

        notifications_swipe_refresh.setOnRefreshListener {
            listenToNotifications(currentUser)
            notifications_swipe_refresh.isRefreshing = false
        }

        val notificationsRecycler = notifications_recycler
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)

        val boardNotificationIcon = toolbar_without_search_notifications_icon
        val boardSavedQuestionIcon = toolbar_without_search_saved_icon

        listenToNotifications(currentUser)

        boardNotificationIcon.setImageResource(
            R.drawable.notification_bell_active
        )

        boardNotificationIcon.setOnClickListener {
            listenToNotifications(currentUser)
        }

        boardNotificationBadge.setOnClickListener {
            listenToNotifications(currentUser)
        }

        boardSavedQuestionIcon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.boardNotificationsFragment)
                .show(activity.savedQuestionFragment).commit()
            activity.subActive = activity.savedQuestionFragment
        }


        notifications_mark_all_as_read.setOnClickListener {

            db.collection("notifications").document(currentUser.uid).collection("board").whereEqualTo("seen", 0)
                .get()
                .addOnSuccessListener {
                    for (doc in it) {
                        doc.reference.update(mapOf("seen" to 1))
                    }
                    notificationsRecyclerAdapter.clear()
                    listenToNotifications(currentUser)
                }
        }

        notificationsRecyclerAdapter.setOnItemClickListener { item, _ ->

            val notification = item as SingleNotification

            db.collection("questions").document(notification.notification.main_post_ID).get().addOnSuccessListener {

                val question = it.toObject(Question::class.java)

                if (question != null) {

                    sharedViewModelQuestion.questionObject.postValue(question)

                    db.collection("profiles").document(question.author_ID).get()
                        .addOnSuccessListener { documentSnapshot ->
                            val user = documentSnapshot.toObject(CommunityProfile::class.java)

                            if (user != null) {
                                sharedViewModelRandomUser.randomUserObject.postValue(user)
                                db.collection("notifications").document(currentUser.uid).collection("board")
                                    .document(notification.notification_ID).update(mapOf("seen" to 1))
                                    .addOnSuccessListener {
                                        activity.boardNotificationsFragment.listenToNotifications(currentUser)

                                        activity.subFm.beginTransaction().add(
                                            R.id.feed_subcontents_frame_container,
                                            activity.openedQuestionFragment,
                                            "openedQuestionFragment"
                                        ).addToBackStack("openedQuestionFragment").commit()

                                        activity.subActive = activity.openedQuestionFragment
                                    }
                            }
                        }
                } else {
                    db.collection("notifications").document(currentUser.uid).collection("board")
                        .document(notification.notification_ID).update(mapOf("seen" to 1)).addOnSuccessListener {
                            Toast.makeText(activity, "This post has been removed", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }
    }


    fun listenToNotifications(currentUser: CommunityProfile) {
        val activity = activity as MainActivity

        notificationsRecyclerAdapter.clear()

        db.collection("notifications").document(currentUser.uid).collection("board")
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


