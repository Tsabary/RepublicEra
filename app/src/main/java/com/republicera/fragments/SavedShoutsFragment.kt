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
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleShout
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Shout
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.RandomUserViewModel
import com.republicera.viewModels.ShoutViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.toolbar_without_search.*
import kotlinx.android.synthetic.main.fragment_saved_items.*


class SavedShoutsFragment : Fragment() {

    lateinit var db : DocumentReference

    private val shoutsRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var currentProfile: CommunityProfile
    private lateinit var currentCommunity: Community

    lateinit var sharedViewModelForShout: ShoutViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_saved_items, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            currentProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)

            sharedViewModelForShout = ViewModelProviders.of(it).get(ShoutViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                    listenToShouts()
                })
        }

        val backButton = saved_items_back_button

        backButton.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.savedShoutsFragment)
                .commit()
            activity.subActive = activity.boardFragment
            activity.switchVisibility(0)

        }

        val title = saved_items_title
        title.text = "Saved shouts"

        saved_items_swipe_refresh.setOnRefreshListener {
            listenToShouts()
            saved_items_swipe_refresh.isRefreshing = false
        }

        val shoutsRecycler = saved_items_recycler

        val shoutsRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        shoutsRecycler.adapter = shoutsRecyclerAdapter
        shoutsRecycler.layoutManager = shoutsRecyclerLayoutManager

//        val shoutsNotificationIcon = toolbar_without_search_notifications_icon
//        val shoutsNotificationsBadge = toolbar_without_search_notifications_badge
//        val shoutsSavedShoutsIcon = toolbar_without_search_saved_icon
//
//        activity.shoutsNotificationsCount.observe(this, Observer {
//            it?.let { notCount ->
//                shoutsNotificationsBadge.setNumber(notCount)
//            }
//        })
//
//        shoutsSavedShoutsIcon.setImageResource(R.drawable.bookmark_active)
//
//        shoutsNotificationIcon.setOnClickListener {
//            activity.subFm.beginTransaction().hide(activity.savedShoutsFragment)
//                .show(activity.shoutsNotificationsFragment)
//                .commit()
//            activity.subActive = activity.shoutsNotificationsFragment
//        }
//
//        shoutsNotificationsBadge.setOnClickListener {
//            activity.subFm.beginTransaction().hide(activity.savedQuestionFragment)
//                .show(activity.boardNotificationsFragment)
//                .commit()
//            activity.subActive = activity.boardNotificationsFragment
//        }
//
//        shoutsSavedShoutsIcon.setImageResource(R.drawable.bookmark_active)
//        shoutsSavedShoutsIcon.setOnClickListener {
//            listenToShouts()
//        }


        shoutsRecyclerAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleShout
            sharedViewModelForShout.shoutObject.postValue(row.shout)
        }

    }


    fun listenToShouts() {

        shoutsRecyclerAdapter.clear()

        db.collection("saved_shouts").document(currentProfile.uid).get().addOnSuccessListener {
            val doc = it.data
            if(doc != null){
                val list = doc["saved_shouts"] as List<String>

                for (shout in list){
                    db.collection("shouts").document(shout).get().addOnSuccessListener { snapshot ->
                        val singleShoutObjectFromDB = snapshot.toObject(Shout::class.java)
                        if (singleShoutObjectFromDB != null) {
                            shoutsRecyclerAdapter.add(SingleShout(singleShoutObjectFromDB, currentProfile, activity as MainActivity))
                        }
                    }
                }
            }
        }
    }
}
