package com.republicera.groupieAdapters

import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.interfaces.BoardMethods
import com.republicera.models.Answer
import com.republicera.models.AnswerComment
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.viewModels.AnswerViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.RandomUserSecondViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.answer_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleAnswer(
    val answer: Answer, val currentUser: CommunityProfile, val activity: MainActivity
) : Item<ViewHolder>(), BoardMethods {

    lateinit var db : DocumentReference

    private val commentsAdapter = GroupAdapter<ViewHolder>()
    val imagesAdapter = GroupAdapter<ViewHolder>()

    lateinit var sharedViewModelSecondRandomUser: RandomUserSecondViewModel
    private lateinit var sharedViewModelAnswer: AnswerViewModel
    private lateinit var currentCommunity: Community


    var isUpvoted = false
    var isDownvoted = false

    lateinit var user: CommunityProfile

    override fun getLayout(): Int = R.layout.answer_layout

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelAnswer = ViewModelProviders.of(it).get(AnswerViewModel::class.java)
            sharedViewModelSecondRandomUser = ViewModelProviders.of(it).get(RandomUserSecondViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val userImage = viewHolder.itemView.single_answer_author_image
        val votesView = viewHolder.itemView.single_answer_votes
        val upvote = viewHolder.itemView.single_answer_upvote
        val downvote = viewHolder.itemView.single_answer_downvote
        val commentButton = viewHolder.itemView.single_answer_comment_button
        val commentInput = viewHolder.itemView.single_answer_comment_input
        val edit = viewHolder.itemView.single_answer_edit
        val delete = viewHolder.itemView.single_answer_delete
        val deleteBox = viewHolder.itemView.single_answer_delete_box
        val cancel = viewHolder.itemView.single_answer_cancel
        val remove = viewHolder.itemView.single_answer_remove

        val commentsRecycler = viewHolder.itemView.single_answer_comments_recycler
        commentsRecycler.adapter = commentsAdapter
        commentsRecycler.layoutManager = LinearLayoutManager(viewHolder.root.context)



        if (answer.author_ID == currentUser.uid) {
            delete.visibility = View.VISIBLE
            edit.visibility = View.VISIBLE
        } else {
            delete.visibility = View.GONE
            edit.visibility = View.GONE
        }



        edit.setOnClickListener {
            sharedViewModelAnswer.sharedAnswerObject.postValue(answer)
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.editAnswerFragment, "editAnswerFragment")
                .addToBackStack("editAnswerFragment")
                .commit()
            activity.subActive = activity.editAnswerFragment
            activity.isEditAnswerActive = true
        }

        delete.setOnClickListener {
            deleteBox.visibility = View.VISIBLE
        }

        cancel.setOnClickListener {
            deleteBox.visibility = View.GONE
        }

        remove.setOnClickListener {
            db.collection("answers").document(answer.answer_ID).delete().addOnSuccessListener {
                activity.openedQuestionFragment.answersAdapter.removeGroup(position)
                activity.openedQuestionFragment.listenToAnswers()
                deleteBox.visibility = View.GONE

                val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.itemView.context)
                firebaseAnalytics.logEvent("question_answer_removed", null)
            }
        }



        db.collection("profiles").document(answer.author_ID).get().addOnSuccessListener {

            user = it.toObject(CommunityProfile::class.java)!!

            val date = PrettyTime().format(Date(answer.timestamp))
            Glide.with(viewHolder.root.context).load(
                if (user.image.isNotEmpty()) {
                    user.image
                } else {
                    R.drawable.user_profile
                }
            )
                .into(userImage)

            viewHolder.itemView.single_answer_author_name.text = user.name
            viewHolder.itemView.single_answer_content.text = answer.content
            viewHolder.itemView.single_answer_timestamp.text = date
            viewHolder.itemView.single_answer_author_reputation.text =
                "(${numberCalculation(user.reputation)})"

            var finalVote = 0
            for (item in answer.score_items){
                finalVote += item.value
            }
            votesView.text = finalVote.toString()

            if (answer.score_items.containsKey(currentUser.uid)) {
                when (answer.score_items[currentUser.uid]) {
                    -1 -> {
                        downView(upvote, downvote)
                        isDownvoted = true
                    }
                    0 -> {
                        defaultView(upvote, downvote)
                    }

                    1 -> {
                        upView(upvote, downvote)
                        isUpvoted = true
                    }
                }
            }

            userImage.setOnClickListener {
                if (currentUser.uid != answer.author_ID) {
                    sharedViewModelSecondRandomUser.randomUserObject.postValue(user)
                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.profileSecondRandomUserFragment,
                        "profileSecondRandomUserFragment"
                    ).addToBackStack("profileSecondRandomUserFragment")
                        .commit()
                    activity.subActive = activity.profileSecondRandomUserFragment
                    activity.isSecondRandomUserProfileActive = true
                } else {
                    activity.navigateToProfile()
                }
            }

        }


        listenToComments()

        upvote.setOnClickListener {
            if (currentUser.uid != answer.author_ID) {

                when {
                    isUpvoted -> return@setOnClickListener
                    isDownvoted -> {
                        votesView.text = (votesView.text.toString().toInt() +1).toString()

                        executeVoteAnswer(
                            0,
                            answer.question_ID,
                            currentUser.uid,
                            currentUser.name,
                            currentUser.image,
                            answer.author_ID,
                            votesView,
                            upvote,
                            downvote,
                            answer.answer_ID,
                            viewHolder.itemView.single_answer_author_reputation,
                            activity,
                            -1,
                            currentCommunity.id
                        )

                        isDownvoted = false
                    }
                    else -> {
                        votesView.text = (votesView.text.toString().toInt() +1).toString()

                        executeVoteAnswer(
                            1,
                            answer.question_ID,
                            currentUser.uid,
                            currentUser.name,
                            currentUser.image,
                            answer.author_ID,
                            votesView,
                            upvote,
                            downvote,
                            answer.answer_ID,
                            viewHolder.itemView.single_answer_author_reputation,
                            activity,
                            0,
                            currentCommunity.id
                        )

                        isUpvoted = true
                    }
                }

            }
        }

        downvote.setOnClickListener {
            if (currentUser.uid != answer.author_ID) {

                when {
                    isDownvoted -> return@setOnClickListener
                    isUpvoted -> {
                        votesView.text = (votesView.text.toString().toInt() -1).toString()

                        executeVoteAnswer(
                            0,
                            answer.question_ID,
                            currentUser.uid,
                            currentUser.name,
                            currentUser.image,
                            answer.author_ID,
                            votesView,
                            upvote,
                            downvote,
                            answer.answer_ID,
                            viewHolder.itemView.single_answer_author_reputation,
                            activity,
                            1,
                            currentCommunity.id
                        )

                        isUpvoted = false
                    }
                    else -> {
                        votesView.text = (votesView.text.toString().toInt() -1).toString()

                        executeVoteAnswer(
                            -1,
                            answer.question_ID,
                            currentUser.uid,
                            currentUser.name,
                            currentUser.image,
                            answer.author_ID,
                            votesView,
                            upvote,
                            downvote,
                            answer.answer_ID,
                            viewHolder.itemView.single_answer_author_reputation,
                            activity,
                            0,
                            currentCommunity.id
                        )

                        isDownvoted = true
                    }
                }

            }
        }

        commentButton.setOnClickListener {

            val timestamp = System.currentTimeMillis()

            val commentDoc = db.collection("answer_comments").document()

            val newComment =
                AnswerComment(commentDoc.id, answer.answer_ID, answer.question_ID, commentInput.text.toString(), timestamp, currentUser.uid, currentUser.name, currentUser.image)

            commentDoc.set(newComment).addOnSuccessListener {

                changeReputation(
                    12,
                    answer.answer_ID,
                    answer.question_ID,
                    currentUser.uid,
                    currentUser.name,
                    currentUser.image,
                    answer.author_ID,
                    TextView(viewHolder.root.context),
                    "answercomment",
                    activity,
                    currentCommunity.id
                )

                commentInput.text.clear()

                db.collection("questions").document(answer.question_ID).update("last_interaction", timestamp)
                    .addOnSuccessListener {

                        listenToComments()

                        closeKeyboard(activity)

                        val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.root.context)
                        firebaseAnalytics.logEvent("question_answer_comment_added", null)
                    }
            }
        }

    }

    private fun listenToComments() {

        commentsAdapter.clear()

        db.collection("answer_comments").whereEqualTo("answer_ID", answer.answer_ID).get().addOnSuccessListener {
            for (doc in it) {
                val answerObject = doc.toObject(AnswerComment::class.java)
                commentsAdapter.add(SingleAnswerComment(answerObject, activity, currentUser))
            }
        }
    }
}
