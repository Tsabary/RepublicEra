package com.republicera.fragments


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.*
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleAdminAnswer
import com.republicera.groupieAdapters.SingleAnswer
import com.republicera.interfaces.BoardMethods
import com.republicera.models.Answer
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Question
import com.republicera.viewModels.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.fragment_opened_question.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class AdminsOpenedQuestionFragment : Fragment(), BoardMethods {

    lateinit var db: DocumentReference

    private lateinit var sharedViewModelQuestion: QuestionViewModel
    private lateinit var sharedViewModelRandomUser: RandomUserViewModel
    private lateinit var sharedViewModelInterests: InterestsViewModel

    private lateinit var currentCommunity: Community

    lateinit var questionObject: Question
    private lateinit var currentUserObject: CommunityProfile

    lateinit var menuSave: MenuItem
    lateinit var menuEdit: MenuItem
    lateinit var menuDelete: MenuItem
    lateinit var authorReputation: TextView
    private lateinit var deleteBox: ConstraintLayout


    private lateinit var buo: BranchUniversalObject
    private lateinit var lp: LinkProperties

    private val answersAdapter = GroupAdapter<ViewHolder>()


    private var isUpvoted = false
    private var isDownvoted = false

    var interestsList: MutableList<String> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_opened_question, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            currentUserObject = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            sharedViewModelInterests = ViewModelProviders.of(it).get(InterestsViewModel::class.java)
            sharedViewModelInterests.interestList.observe(activity, Observer { currentInterestsList ->
                interestsList = currentInterestsList
            })

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val answerButton = opened_question_answer_btn
        val answersRecycler = opened_question_answers_recycler
        val title = opened_question_title
        val content = opened_question_content
        val timestamp = opened_question_timestamp
        val tags = opened_question_tags
        val upVote = opened_question_upvote
        val downVote = opened_question_downvote
        val votesCount = opened_question_votes
        val authorImage = opened_question_author_image
        val authorName = opened_question_author_name
        deleteBox = opened_question_delete_box
        val removeButton = opened_question_remove
        val cancelButton = opened_question_cancel
        authorReputation = opened_question_author_reputation
        val menuButton = opened_question_options_button

        val popup = PopupMenu(this.context, menuButton)
        popup.inflate(R.menu.open_question)
        menuSave = popup.menu.getItem(0)
        menuEdit = popup.menu.getItem(1)
        menuDelete = popup.menu.getItem(2)

        menuButton.setOnClickListener {
            popup.show()
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.opened_question_save -> {
                    checkIfQuestionSaved(1, activity)
                    true
                }

                R.id.opened_question_menu_edit -> {
                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.adminsEditQuestionFragment,
                        "adminsEditQuestionFragment"
                    ).addToBackStack("adminsEditQuestionFragment")
                        .commit()
                    activity.subActive = activity.adminsEditQuestionFragment
                    true
                }

                R.id.opened_question_menu_delete -> {
                    deleteBox.visibility = View.VISIBLE
                    true
                }

                else -> {
                    true
                }
            }
        }



        sharedViewModelQuestion.questionObject.observe(this, Observer {
            it?.let { question ->
                questionObject = question
                answersAdapter.clear()
                checkIfQuestionSaved(0, activity)

                title.text = questionObject.title
                content.text = questionObject.details
                timestamp.text = PrettyTime().format(questionObject.timestamp)
                tags.text = questionObject.tags.joinToString()
                var finalVote = 0
                for (item in questionObject.score_items) {
                    finalVote += item.value
                }
                votesCount.text = finalVote.toString()

                listenToAnswers()

                if (question.author_ID == currentUserObject.uid) {
                    menuSave.isVisible = false
                    menuEdit.isVisible = true
                    menuDelete.isVisible = true

                } else {
                    menuSave.isVisible = true
                    menuEdit.isVisible = false
                    menuDelete.isVisible = false
                }


                removeButton.setOnClickListener {
                    db.collection("admins_questions").document(question.id).delete().addOnSuccessListener {
                        activity.subFm.popBackStack("adminsOpenedQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

                        activity.switchVisibility(0)
                        activity.adminsFragment.listenToQuestions()

                        firebaseAnalytics.logEvent("question_removed", null)
                    }
                }

                cancelButton.setOnClickListener {
                    deleteBox.visibility = View.GONE
                }

                if (questionObject.score_items.containsKey(currentUserObject.uid)) {
                    when (questionObject.score_items[currentUserObject.uid]) {
                        -1 -> {
                            downView(upVote, downVote)
                            isDownvoted = true
                        }
                        0 -> {
                            defaultView(upVote, downVote)
                        }

                        1 -> {
                            upView(upVote, downVote)
                            isUpvoted = true
                        }
                    }
                }
            }
        }
        )



        sharedViewModelRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                Glide.with(this).load(
                    if (user.image.isNotEmpty()) {
                        user.image
                    } else {
                        R.drawable.user_profile
                    }
                ).into(authorImage)
                authorName.text = user.name

                authorReputation.text = "(${numberCalculation(user.reputation)})"

                authorImage.setOnClickListener {
                    goToProfile(activity, user)
                }

                authorName.setOnClickListener {
                    goToProfile(activity, user)
                }
            }
        }
        )


        upVote.setOnClickListener {
            if (currentUserObject.uid != questionObject.author_ID) {

                when {
                    isUpvoted -> return@setOnClickListener
                    isDownvoted -> {
                        votesCount.text = (votesCount.text.toString().toInt() + 1).toString()

                        executeVoteAdminQuestion(
                            0,
                            questionObject.id,
                            currentUserObject.uid,
                            currentUserObject.name,
                            currentUserObject.image,
                            questionObject.author_ID,
                            votesCount,
                            upVote,
                            downVote,
                            questionObject.id,
                            authorReputation,
                            activity,
                            -1,
                            currentCommunity.id
                        )

                        isDownvoted = false
                    }
                    else -> {
                        votesCount.text = (votesCount.text.toString().toInt() + 1).toString()

                        executeVoteAdminQuestion(
                            1,
                            questionObject.id,
                            currentUserObject.uid,
                            currentUserObject.name,
                            currentUserObject.image,
                            questionObject.author_ID,
                            votesCount,
                            upVote,
                            downVote,
                            questionObject.id,
                            authorReputation,
                            activity,
                            0,
                            currentCommunity.id
                        )

                        isUpvoted = true
                    }
                }

            }
        }

        downVote.setOnClickListener {
            if (currentUserObject.uid != questionObject.author_ID) {

                when {
                    isDownvoted -> return@setOnClickListener
                    isUpvoted -> {
                        votesCount.text = (votesCount.text.toString().toInt() - 1).toString()

                        executeVoteAdminQuestion(
                            0,
                            questionObject.id,
                            currentUserObject.uid,
                            currentUserObject.name,
                            currentUserObject.image,
                            questionObject.author_ID,
                            votesCount,
                            upVote,
                            downVote,
                            questionObject.id,
                            authorReputation,
                            activity,
                            1,
                            currentCommunity.id
                        )
                        isUpvoted = false
                    }
                    else -> {
                        votesCount.text = (votesCount.text.toString().toInt() - 1).toString()

                        executeVoteAdminQuestion(
                            -1,
                            questionObject.id,
                            currentUserObject.uid,
                            currentUserObject.name,
                            currentUserObject.image,
                            questionObject.author_ID,
                            votesCount,
                            upVote,
                            downVote,
                            questionObject.id,
                            authorReputation,
                            activity,
                            0,
                            currentCommunity.id
                        )
                        isDownvoted = true
                    }
                }
            }
        }


        answerButton.setOnClickListener {

            db.collection("admins_answers").whereEqualTo("question_ID", questionObject.id)
                .whereEqualTo("author_ID", currentUserObject.uid).get().addOnSuccessListener {

                    if (it.size() == 0) {
                        activity.subFm.beginTransaction()
                            .add(
                                R.id.feed_subcontents_frame_container,
                                activity.adminsAnswerFragment,
                                "adminsAnswerFragment"
                            )
                            .addToBackStack("adminsAnswerFragment")
                            .commit()
                        activity.subActive = activity.adminsAnswerFragment
                    } else {
                        Toast.makeText(
                            activity,
                            "You've already answered this question, please edit your answer instead",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
        }

        answersRecycler.adapter = answersAdapter
        answersRecycler.layoutManager = LinearLayoutManager(this.context)
    }


    private fun goToProfile(activity: MainActivity, user: CommunityProfile) {
        if (user.uid != currentUserObject.uid) {
            activity.subFm.beginTransaction().add(
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

    fun listenToAnswers() {
        answersAdapter.clear()

        db.collection("admins_answers").whereEqualTo("question_ID", questionObject.id)
            .orderBy("total_score", Query.Direction.DESCENDING).get()
            .addOnSuccessListener {

                for (document in it) {
                    val answerObject = document.toObject(Answer::class.java)
                    answersAdapter.add(
                        SingleAdminAnswer(
                            answerObject,
                            currentUserObject,
                            activity as MainActivity
                        )
                    )
                }

            }
    }

    private fun checkIfQuestionSaved(event: Int, activity: Activity) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        db.collection("admins_saved_questions").document(currentUserObject.uid).get().addOnSuccessListener {

            val docSnapshot = it.data
            if(docSnapshot != null){
                val list = docSnapshot["saved_questions"] as List<String>

                if (list.contains(questionObject.id)) {
                    if (event == 0) {
                        menuSave.title = "Saved"
                    } else {
                        db.collection("admins_saved_questions").document(currentUserObject.uid)
                            .update("saved_questions", FieldValue.arrayRemove(questionObject.id)).addOnSuccessListener {
                                menuSave.title = "Save"


                                changeReputation(
                                    27,
                                    questionObject.id,
                                    questionObject.id,
                                    currentUserObject.uid,
                                    currentUserObject.name,
                                    currentUserObject.image,
                                    questionObject.author_ID,
                                    "questionsave",
                                    activity,
                                    currentCommunity.id
                                )

                                firebaseAnalytics.logEvent("question_unsaved", null)
                                (activity as MainActivity).savedQuestionFragment.listenToQuestions()
                            }
                    }

                } else {
                    if (event == 0) {
                        menuSave.title = "Save"
                    } else {
                        db.collection("admins_saved_questions").document(currentUserObject.uid)
                            .set(mapOf("saved_questions" to FieldValue.arrayUnion(questionObject.id)))
                            .addOnSuccessListener {
                                menuSave.title = "Saved"

                                for (tag in questionObject.tags) {

                                    db.collection("interests").document(currentUserObject.uid)
                                        .update("interests_list", FieldValue.arrayUnion(tag))


                                    interestsList.add(tag)
                                    sharedViewModelInterests.interestList.postValue(interestsList)
                                }
                            }

                        changeReputation(
                            26,
                            questionObject.id,
                            questionObject.id,
                            currentUserObject.uid,
                            currentUserObject.name,
                            currentUserObject.image,
                            questionObject.author_ID,
                            "questionsave",
                            activity,
                            currentCommunity.id
                        )

                        firebaseAnalytics.logEvent("question_saved", null)
                        (activity as MainActivity).savedQuestionFragment.listenToQuestions()
                    }
                }
            } else {
                if(event == 1){
                    db.collection("admins_saved_questions").document(currentUserObject.uid)
                        .set(mapOf("saved_questions" to FieldValue.arrayUnion(questionObject.id)))
                        .addOnSuccessListener {
                            menuSave.title = "Saved"

                            for (tag in questionObject.tags) {

                                db.collection("interests").document(currentUserObject.uid)
                                    .update("interests_list", FieldValue.arrayUnion(tag))


                                interestsList.add(tag)
                                sharedViewModelInterests.interestList.postValue(interestsList)
                            }
                        }

                    changeReputation(
                        26,
                        questionObject.id,
                        questionObject.id,
                        currentUserObject.uid,
                        currentUserObject.name,
                        currentUserObject.image,
                        questionObject.author_ID,
                        "questionsave",
                        activity,
                        currentCommunity.id
                    )

                    firebaseAnalytics.logEvent("question_saved", null)
                    (activity as MainActivity).savedQuestionFragment.listenToQuestions()
                }
            }

        }
    }
}

