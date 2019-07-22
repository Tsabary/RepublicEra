package com.republicera.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.algolia.search.saas.Client
import com.github.nisrulz.sensey.Sensey
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleCommunityOption
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.Community
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.CurrentUserViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_choose_community.*
import kotlinx.android.synthetic.main.fragment_my_communities.*
import org.json.JSONObject


class MyCommunitiesFragment : Fragment(), GeneralMethods {

    val db = FirebaseFirestore.getInstance()
    lateinit var index: com.algolia.search.saas.Index

    var topLevelUser: User? = null

    var currentCommunity = Community()

    lateinit var containerLogin: ConstraintLayout

    private val communitiesRecyclerAdapter = GroupAdapter<ViewHolder>()
    private lateinit var communityViewModel: CurrentCommunityViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_communities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            ViewModelProviders.of(activity).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity,
                Observer {
                    topLevelUser = it
                    if (topLevelUser != null) {
                        populateCommunities()
                    } else {
//                        activity.communitiesHomeFragment.viewPager.currentItem = 1
                    }
                })
        }

        val applicationID = activity.getString(R.string.algolia_application_id)
        val apiKey = activity.getString(R.string.algolia_api_key)

        val client = Client(applicationID, apiKey)
        index = client.getIndex("communities")

        val loginMessage = my_communities_login_message

        val recycler = my_comunities_recycler
        recycler.adapter = communitiesRecyclerAdapter
        recycler.layoutManager = LinearLayoutManager(this.context)
        val newCommunityInput = my_comunities_new_input
        val newCommunityButton = my_comunities_create_button

        loginMessage.setOnClickListener {
            activity.takeToRegister()
        }

        newCommunityInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length > 1) {
                    newCommunityButton.visibility = View.VISIBLE
                } else {
                    newCommunityButton.visibility = View.GONE
                }
            }

        })

//        newCommunityToggle.tag = "true"
//        newCommunityToggle.setOnClickListener {
//            if (newCommunityToggle.tag == "true") {
//                newCommunityToggle.tag = "false"
//                newCommunityToggle.text = getString(R.string.secret)
//                public = false
//            } else {
//                newCommunityToggle.tag = "true"
//                newCommunityToggle.text = "Pubic"
//                public = true
//            }
//        }


        activity.let {

            communityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)
            communityViewModel.currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                })
        }

        newCommunityButton.setOnClickListener {

            if (topLevelUser != null) {

                if (newCommunityInput.text.length > 1) {
                    val communityDoc = FirebaseFirestore.getInstance().collection("communities").document()
                    val title = newCommunityInput.text.toString()
                    val newCommunity =
                        Community(
                            communityDoc.id,
                            title,
                            "",
                            0,
                            0,
                            true,
                            "",
                            mutableListOf()
                        )
                    communityDoc.set(newCommunity).addOnSuccessListener {

                        val newCommunityJson = JSONObject()
                            .put("objectID", communityDoc.id)
                            .put("title", title)
                            .put("description", "")
                            .put("members", 0)

                        index.addObjectAsync(newCommunityJson, null)


                        communityViewModel.currentCommunity.postValue(newCommunity)
                        communitiesRecyclerAdapter.add(SingleCommunityOption(newCommunity))
                        activity.chooseCommunityFrame.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this.context, "Community name is too short", Toast.LENGTH_SHORT).show()
                }
            } else {
                activity.takeToRegister()
            }
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
        db.collection("users").document(topLevelUser!!.uid).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            if (user != null) {
                for (communityPath in user.communities_list) {
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


    companion object {
        fun newInstance(): MyCommunitiesFragment = MyCommunitiesFragment()
    }

}
