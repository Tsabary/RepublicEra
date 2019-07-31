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
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Question
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.QuestionViewModel
import com.republicera.viewModels.RandomUserViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class AdminsSearchFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()

    private lateinit var sharedViewModelQuestion: QuestionViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel
    private lateinit var currentCommunity: Community


    lateinit var applicationID : String
    lateinit var searchAPI: String
    lateinit var searcher: Searcher
    lateinit var helper: InstantSearch


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applicationID = (activity as MainActivity).getString(R.string.algolia_application_id)
        searchAPI = (activity as MainActivity).getString(R.string.algolia_search_key)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                })
        }

        val askNewQuestionButton = search_ask_button

        askNewQuestionButton.setOnClickListener {
           activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.adminsNewQuestionFragment, "adminsNewQuestionFragment")
               .addToBackStack("adminsNewQuestionFragment").commit()
            activity.subActive = activity.adminsNewQuestionFragment
            activity.switchVisibility(1)
        }

        val hitsView = search_recycler

        searcher = Searcher.create(applicationID, searchAPI, "${currentCommunity.id}_admins_questions")
        helper = InstantSearch(activity as MainActivity, searcher)
        helper.search()

        hitsView.setOnItemClickListener { _, position, _ ->
            val question = hitsView.get(position)

            db.collection("admin_questions").document(question["objectID"].toString()).get().addOnSuccessListener {
                val questionObject = it.toObject(Question::class.java)
                if (questionObject != null) {
                    sharedViewModelQuestion.questionObject.postValue(questionObject)

                    activity.subFm.beginTransaction()
                        .add(
                            R.id.feed_subcontents_frame_container,
                            activity.adminsOpenedQuestionFragment,
                            "adminsOpenedQuestionFragment"
                        )
                        .addToBackStack("adminsOpenedQuestionFragment").commit()

                    db.collection("profiles").document(questionObject.author_ID).get().addOnSuccessListener { documentSnapshot ->
                        val profile = documentSnapshot.toObject(CommunityProfile::class.java)

                        if (profile != null) {
                            sharedViewModelRandomUser.randomUserObject.postValue(profile)
                            activity.subActive = activity.adminsOpenedQuestionFragment
                            activity.isOpenedQuestionActive = true
                            activity.switchVisibility(1)
                        }
                    }
                }
            }
        }
    }


}
