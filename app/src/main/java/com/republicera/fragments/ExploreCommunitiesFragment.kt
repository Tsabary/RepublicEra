package com.republicera.fragments


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.algolia.instantsearch.core.helpers.Searcher
import com.algolia.instantsearch.ui.helpers.InstantSearch
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.CurrentUserViewModel
import kotlinx.android.synthetic.main.fragment_explore_communities.*


class ExploreCommunitiesFragment : Fragment() {


    val db = FirebaseFirestore.getInstance()


    lateinit var applicationID: String
    lateinit var searchAPI: String
    lateinit var searcher: Searcher
    lateinit var helper: InstantSearch

    private lateinit var communityViewModel: CurrentCommunityViewModel
    private lateinit var currentUserViewModel: CurrentUserViewModel
    var topLevelUser: User? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_communities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applicationID = (activity as MainActivity).getString(R.string.algolia_application_id)
        searchAPI = (activity as MainActivity).getString(R.string.algolia_search_key)

        val activity = activity as MainActivity
        activity.let {
            communityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)
            currentUserViewModel = ViewModelProviders.of(it).get(CurrentUserViewModel::class.java)
            currentUserViewModel.currentUserObject.observe(it, Observer { user ->
                topLevelUser = user
            })
        }

        val hitsView = explore_communities_recycler

        searcher = Searcher.create(applicationID, searchAPI, "communities")
        helper = InstantSearch(activity, searcher)
        helper.search()
        hitsView.layoutManager = LinearLayoutManager(this.context)

        hitsView.setOnItemClickListener { _, position, _ ->

            if (topLevelUser != null) {
                val community = hitsView.get(position)

                AlertDialog.Builder(context)
                    .setTitle("Join tribe")
                    .setMessage("Would you like to join the ${community["title"]} tribe?")
                    .setPositiveButton("Join") { _, _ ->

                        db.collection("communities").document(community["objectID"].toString()).get()
                            .addOnSuccessListener {
                                val communityObject = it.toObject(Community::class.java)
                                if (communityObject != null) {
                                    communityViewModel.currentCommunity.postValue(communityObject)

                                    val sharedPref =
                                        activity.getSharedPreferences(
                                            activity.getString(R.string.package_name),
                                            Context.MODE_PRIVATE
                                        )

                                    val editor = sharedPref.edit()
                                    editor.putString("last_community", communityObject.id)
                                    editor.apply()

                                    activity.userHomeFrame.visibility = View.GONE
                                    Log.d("checkk", "01101111010")

                                } else {
                                    Log.d("checkk", "121212121212121")

                                }
                            }
                    }
                    .show()
            } else {
                activity.takeToRegister()
            }
        }
    }

    override fun onPause() {
        searcher.destroy()
        super.onPause()
    }


    fun createSearcher() {
        searcher = Searcher.create(applicationID, searchAPI, "communities")
        helper = InstantSearch(activity as MainActivity, searcher)
        helper.search()
    }

    fun destroySearcher() {
        searcher.destroy()
    }


    companion object {
        fun newInstance(): ExploreCommunitiesFragment = ExploreCommunitiesFragment()
    }

}
