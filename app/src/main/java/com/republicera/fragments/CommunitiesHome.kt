package com.republicera.fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity

import com.republicera.R
import com.republicera.groupieAdapters.SingleCommunityOption
import com.republicera.models.Community
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.CurrentUserViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_communities_home.*


class CommunitiesHome : Fragment() {

    val db = FirebaseFirestore.getInstance()
    var currentCommunity = Community()

    var topLevelUser: User? = null

    private lateinit var communityViewModel: CurrentCommunityViewModel


    val communitiesRecyclerAdapter = GroupAdapter<ViewHolder>()

    lateinit var emptyMessage: TextView
    lateinit var yourRepublics: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_communities_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        val userName = communities_home_name

        activity.let {
            ViewModelProviders.of(activity).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity, Observer {
                    topLevelUser = it
                    if (topLevelUser != null) {
                        userName.text = it.firstName + "."
                        populateCommunities()
                    } else {
                        userName.text = "Friend."
                    }
                })

            communityViewModel =ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)
            communityViewModel.currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                })
        }



        val communitiesRecycler = communities_home_recycler
        communitiesRecycler.adapter = communitiesRecyclerAdapter
        communitiesRecycler.layoutManager = LinearLayoutManager(this.context)

        emptyMessage = communities_home_empty_message
        yourRepublics = communities_home_your_communities

        val exploreButton = communities_home_search
        val addButton = communities_home_add
        val settingsButton = communities_home_settings

        if (topLevelUser != null) {
            userName.text = topLevelUser!!.firstName + "."
        } else {
            userName.text = "Friend."
            emptyMessage.visibility = View.VISIBLE
            yourRepublics.visibility = View.GONE
        }

        addButton.setOnClickListener {
            if (topLevelUser != null) {
                activity.CommunitiesFm.beginTransaction()
                    .add(
                        R.id.feed_choose_community_frame_container,
                        activity.newCommunityFragment,
                        "newCommunityFragment"
                    )
                    .addToBackStack("newCommunityFragment")
                    .commit()
            } else {
                activity.takeToRegister()
            }

        }

        exploreButton.setOnClickListener {
            activity.CommunitiesFm.beginTransaction()
                .add(
                    R.id.feed_choose_community_frame_container,
                    activity.exploreCommunitiesFragment,
                    "exploreCommunitiesFragment"
                )
                .addToBackStack("exploreCommunitiesFragment")
                .commit()
        }


        communitiesRecyclerAdapter.setOnItemClickListener { item, _ ->
            val community = item as SingleCommunityOption

            if (currentCommunity != community.community) {

                communityViewModel.currentCommunity.postValue(community.community)

                val sharedPref =
                    activity.getSharedPreferences(activity.getString(R.string.package_name), Context.MODE_PRIVATE)


                val editor = sharedPref.edit()
                editor.putString("last_community", community.community.id)
                editor.apply()

                activity.chooseCommunityFrame.visibility = View.GONE
            } else {
                activity.chooseCommunityFrame.visibility = View.GONE
            }
        }

    }

    private fun populateCommunities() {
        communitiesRecyclerAdapter.clear()
        if (topLevelUser!!.communities_list.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            yourRepublics.visibility = View.GONE
        } else {
            emptyMessage.visibility = View.GONE
            yourRepublics.visibility = View.VISIBLE

            for (communityPath in topLevelUser!!.communities_list) {
                db.collection("communities").document(communityPath).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val community = documentSnapshot.toObject(Community::class.java)
                        if (community != null) {
                            communitiesRecyclerAdapter.add(SingleCommunityOption(community))
                        }
                    }
            }
        }
    }
}
