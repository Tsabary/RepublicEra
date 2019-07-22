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
import com.republicera.groupieAdapters.SingleBoardRow
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Question
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.QuestionViewModel
import com.republicera.viewModels.RandomUserViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_toolbar_notifications.*
import kotlinx.android.synthetic.main.fragment_saved_questions.view.*


class SavedQuestionsFragment : Fragment() {

    lateinit var db : DocumentReference

    private val questionsRecyclerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var currentProfile: CommunityProfile
    private lateinit var currentCommunity: Community

    lateinit var sharedViewModelForQuestion: QuestionViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_saved_questions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            currentProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)

            sharedViewModelForQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }


        val questionsRecycler = view.saved_questions_recycler

        val questionRecyclerLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        questionsRecycler.adapter = questionsRecyclerAdapter
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        val boardNotificationIcon = board_toolbar_notifications_notifications_icon
        val boardNotificationsBadge = board_toolbar_notifications_notifications_badge
        val boardSavedQuestionIcon = board_toolbar_notifications_saved_questions_icon

        boardNotificationIcon.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.savedQuestionFragment)
                .show(activity.boardNotificationsFragment)
                .commit()
            activity.subActive = activity.boardNotificationsFragment
        }

        boardNotificationsBadge.setOnClickListener {
            activity.subFm.beginTransaction().hide(activity.savedQuestionFragment)
                .show(activity.boardNotificationsFragment)
                .commit()
            activity.subActive = activity.boardNotificationsFragment
        }

        boardSavedQuestionIcon.setImageResource(R.drawable.bookmark_active)
        boardSavedQuestionIcon.setOnClickListener {
            listenToQuestions()
        }

        listenToQuestions()

//        questionsRecyclerAdapter.setOnItemClickListener { item, _ ->
//            val row = item as SingleBoardRow
//            sharedViewModelForQuestion.questionObject.postValue(row.question)
//        }


        questionsRecyclerAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleBoardRow
            val author = row.question.author_ID
            val question = row.question

            sharedViewModelForQuestion.questionObject.postValue(question)

            openQuestion(author, activity)
        }
    }

    private fun openQuestion(author: String, activity: MainActivity) {

        activity.subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, activity.openedQuestionFragment, "openedQuestionFragment")
            .addToBackStack("openedQuestionFragment").commit()

        db.collection("profiles").document(author).get().addOnSuccessListener {
            val user = it.toObject(CommunityProfile::class.java)

            if (user != null) {
                sharedViewModelRandomUser.randomUserObject.postValue(user)
                activity.subActive = activity.openedQuestionFragment
                activity.isOpenedQuestionActive = true
                activity.switchVisibility(1)
            }
        }
    }

    fun listenToQuestions() {

        questionsRecyclerAdapter.clear()

        db.collection("saved_questions").document(currentProfile.uid).get().addOnSuccessListener {
            val doc = it.data
            if(doc != null){
                val list = doc["saved_questions"] as List<String>

                for (question in list){
                    db.collection("questions").document(question).get().addOnSuccessListener {
                        val singleQuestionObjectFromDB = it.toObject(Question::class.java)
                        if (singleQuestionObjectFromDB != null) {
                            questionsRecyclerAdapter.add(SingleBoardRow(singleQuestionObjectFromDB, activity as MainActivity))
                        }
                    }
                }
            }
        }
    }
}
