package com.republicera.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.republicera.MainActivity

import com.republicera.R
import com.republicera.RegisterLoginActivity
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


        emptyMessage = communities_home_empty_message
        yourRepublics = communities_home_your_communities

        val activity = activity as MainActivity

        val userName = communities_home_name

        val communitiesRecycler = communities_home_recycler
        communitiesRecycler.adapter = communitiesRecyclerAdapter
        communitiesRecycler.layoutManager = LinearLayoutManager(this.context)


        val exploreButton = communities_home_search
        val addButton = communities_home_add
        val settingsButton = communities_home_settings
        val popup = PopupMenu(this.context, settingsButton)
        popup.inflate(R.menu.user_options)

        activity.let {
            ViewModelProviders.of(activity).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity, Observer { user ->
                    topLevelUser = user
                    if (topLevelUser != null) {
                        userName.text = if (user.first_name.isNotEmpty()) {
                            user.first_name + "."
                        } else {
                            "Friend."
                        }
                        populateCommunities()


                        settingsButton.setOnClickListener {
                            popup.show()
                        }
                    } else {
                        userName.text = "Friend."
                    }
                })

            communityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)
            communityViewModel.currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                })
        }







        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.user_edit_contact_details -> {
                    activity.userFm.beginTransaction().add(
                        R.id.user_home_frame_container,
                        activity.editContactDetailsFragment,
                        "editContactDetailsFragment"
                    ).addToBackStack("editContactDetailsFragment")
                        .commit()
                    activity.userActive = activity.editContactDetailsFragment
                    true
                }

                R.id.user_edit_language_preferences -> {
                    activity.userFm.beginTransaction().add(
                        R.id.user_home_frame_container,
                        activity.editLanguagePreferencesFragment,
                        "editLanguagePreferencesFragment"
                    ).addToBackStack("editLanguagePreferencesFragment")
                        .commit()
                    activity.userActive = activity.editLanguagePreferencesFragment
                    true
                }

                R.id.user_edit_basic_info -> {
                    activity.userFm.beginTransaction().add(
                        R.id.user_home_frame_container,
                        activity.editBasicInfoFragment,
                        "editBasicInfoFragment"
                    ).addToBackStack("editBasicInfoFragment")
                        .commit()
                    activity.userActive = activity.editBasicInfoFragment
                    true
                }

                R.id.user_logout -> {

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                    val uid = FirebaseAuth.getInstance().uid
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(uid).addOnSuccessListener {
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

                            val intent = Intent(this.context, RegisterLoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    true
                }

                else -> {
                    true
                }
            }
        }


        if (topLevelUser != null) {
            userName.text = topLevelUser!!.first_name + "."
        } else {
            userName.text = "Friend."
            emptyMessage.visibility = View.VISIBLE
            yourRepublics.visibility = View.GONE
        }

        addButton.setOnClickListener {
            if (topLevelUser != null) {
                activity.userFm.beginTransaction()
                    .add(
                        R.id.user_home_frame_container,
                        activity.newCommunityFragment,
                        "newCommunityFragment"
                    )
                    .addToBackStack("newCommunityFragment")
                    .commit()

                activity.userActive = activity.newCommunityFragment
            } else {
                activity.takeToRegister()
            }

        }

        exploreButton.setOnClickListener {
            activity.userFm.beginTransaction()
                .add(
                    R.id.user_home_frame_container,
                    activity.exploreCommunitiesFragment,
                    "exploreCommunitiesFragment"
                )
                .addToBackStack("exploreCommunitiesFragment")
                .commit()

            activity.userActive = activity.exploreCommunitiesFragment

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

                activity.userHomeFrame.visibility = View.GONE
            } else {
                activity.userHomeFrame.visibility = View.GONE
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
