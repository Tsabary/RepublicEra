package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.algolia.instantsearch.core.helpers.Searcher
import com.algolia.instantsearch.ui.helpers.InstantSearch
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Question
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.QuestionViewModel
import com.republicera.viewModels.RandomUserViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class SearchProfilesFragment : Fragment() {

    lateinit var db: DocumentReference

    lateinit var sharedViewModelRandomUser: RandomUserViewModel
    private lateinit var currentCommunity: Community


    lateinit var applicationID: String
    lateinit var searchAPI: String
    lateinit var searcher: Searcher
    lateinit var helper: InstantSearch

lateinit var currentProfile : CommunityProfile

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_profiles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applicationID = (activity as MainActivity).getString(R.string.algolia_application_id)
        searchAPI = (activity as MainActivity).getString(R.string.algolia_search_key)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            currentProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject
            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }


        val hitsView = search_recycler

        searcher = Searcher.create(applicationID, searchAPI, "${currentCommunity.id}_profiles")
        helper = InstantSearch(activity, searcher)
        helper.search()

        hitsView.setOnItemClickListener { _, position, _ ->
            val profile = hitsView.get(position)

            db.collection("profiles").document(profile["objectID"].toString()).get().addOnSuccessListener {
                val profileObject = it.toObject(CommunityProfile::class.java)
                if (profileObject != null) {
                    goToProfile(profileObject)
                }
            }


        }
    }

    private fun goToProfile(randomCommunityProfile : CommunityProfile) {
        val activity = activity as MainActivity
        if (currentProfile.uid != randomCommunityProfile.uid) {
            sharedViewModelRandomUser.randomUserObject.postValue(randomCommunityProfile)
            activity.subFm.beginTransaction()
                .add(
                    R.id.feed_subcontents_frame_container,
                    activity.profileRandomUserFragment,
                    "profileRandomUserFragment"
                ).addToBackStack("profileRandomUserFragment")
                .commit()
            activity.subActive = activity.profileRandomUserFragment
        } else {
            activity.navigateToProfile()
        }
    }

}
