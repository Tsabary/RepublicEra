package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleNotification
import com.republicera.models.*
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.RandomUserViewModel
import com.republicera.viewModels.ShoutViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.toolbar_without_search.*
import kotlinx.android.synthetic.main.fragment_notifications.*


class ShoutsNotificationsFragment : Fragment() {

    lateinit var db: DocumentReference

    private val notificationsRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var sharedViewModelShout: ShoutViewModel
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
            sharedViewModelShout = ViewModelProviders.of(it).get(ShoutViewModel::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity, Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val shoutsNotificationBadge = toolbar_without_search_notifications_badge

        activity.shoutsNotificationsCount.observe(this, Observer {
            it?.let { notCount ->
                shoutsNotificationBadge.setNumber(notCount)
            }
        })

        notifications_swipe_refresh.setOnRefreshListener {
            listenToNotifications(currentUser)
            notifications_swipe_refresh.isRefreshing = false
        }

        val notificationsRecycler = notifications_recycler
        notificationsRecycler.adapter = notificationsRecyclerAdapter
        notificationsRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)

        val shoutsNotificationIcon = toolbar_without_search_notifications_icon
        val shoutsSavedQuestionIcon = toolbar_without_search_saved_icon

        listenToNotifications(currentUser)

        shoutsNotificationIcon.setImageResource(
            R.drawable.notification_bell_active
        )

        shoutsNotificationIcon.setOnClickListener {
            listenToNotifications(currentUser)
        }

        shoutsNotificationBadge.setOnClickListener {
            listenToNotifications(currentUser)
        }

        shoutsSavedQuestionIcon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.shoutsNotificationsFragment)
                .show(activity.savedShoutsFragment).commit()
            activity.subActive = activity.savedShoutsFragment
        }


        notifications_mark_all_as_read.setOnClickListener {
                db.collection("notifications").document(currentUser.uid).collection("shouts").whereEqualTo("seen", 0)
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

            db.collection("shouts").document(notification.notification.main_post_ID).get().addOnSuccessListener {

                val shout = it.toObject(Shout::class.java)

                if (shout != null) {

                    sharedViewModelShout.shoutObject.postValue(shout)

                    db.collection("profiles").document(shout.author_ID).get()
                        .addOnSuccessListener { documentSnapshot ->
                            val user = documentSnapshot.toObject(CommunityProfile::class.java)
                            if (user != null) {
                                sharedViewModelRandomUser.randomUserObject.postValue(user)
                                db.collection("notifications").document(currentUser.uid).collection("shouts")
                                    .document(notification.notification_ID).set(mapOf("seen" to 1), SetOptions.merge())
                                    .addOnSuccessListener {
                                        activity.shoutsNotificationsFragment.listenToNotifications(
                                            currentUser
                                        )

                                        activity.subFm.beginTransaction().add(
                                            R.id.feed_subcontents_frame_container,
                                            activity.shoutExpendedFragment,
                                            "shoutExpendedFragment"
                                        ).addToBackStack("shoutExpendedFragment").commit()

                                        activity.subActive = activity.shoutExpendedFragment
                                    }
                            }
                        }
                }
            }
        }
    }


    fun listenToNotifications(currentUser: CommunityProfile) {
        val activity = activity as MainActivity

        notificationsRecyclerAdapter.clear()

        db.collection("notifications").document(currentUser.uid).collection("shouts")
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

                    var shoutNotCount = 0

                    if (notification.seen == 0) {
                        shoutNotCount++
                    }
                    activity.shoutsNotificationsCount.postValue(shoutNotCount)
                }
            }
    }
}


