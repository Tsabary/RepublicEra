package com.republicera.groupieAdapters

import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.AnswerComment
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.RandomUserSecondViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.answer_comment_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleAnswerComment(val comment: AnswerComment, val activity: MainActivity, val currentUser: CommunityProfile) :
    Item<ViewHolder>(), GeneralMethods {

    lateinit var db : DocumentReference

    lateinit var sharedViewModelSecondRandomUser: RandomUserSecondViewModel
    private lateinit var currentCommunity: Community


    lateinit var user: CommunityProfile

    override fun getLayout(): Int = R.layout.answer_comment_layout

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelSecondRandomUser = ViewModelProviders.of(it).get(RandomUserSecondViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.itemView.context)


        val commentContent = viewHolder.itemView.answer_comment_comment
        val commentContentEditable = viewHolder.itemView.answer_comment_comment_editable
        val commentContentTimestamp = viewHolder.itemView.answer_comment_timestamp
        val edit = viewHolder.itemView.answer_comment_edit
        val save = viewHolder.itemView.answer_comment_save
        val delete = viewHolder.itemView.answer_comment_delete
        val deleteBox = viewHolder.itemView.answer_comment_delete_box
        val cancel = viewHolder.itemView.answer_comment_cancel
        val remove = viewHolder.itemView.answer_comment_remove
        val authorImage = viewHolder.itemView.answer_comment_author_image

        commentContent.text = comment.content
        commentContentEditable.setText(comment.content)
        commentContentTimestamp.text = PrettyTime().format(Date(comment.timestamp))

        db.collection("profiles").document(comment.author_ID).get().addOnSuccessListener {

            user = it.toObject(CommunityProfile::class.java)!!

            viewHolder.itemView.answer_comment_author_name.text = user.name
            viewHolder.itemView.answer_comment_author_reputation.text =
                "(${numberCalculation(user.reputation)})"

            Glide.with(viewHolder.root.context).load(
                if (user.image.isNotEmpty()) {
                    user.image
                } else {
                    R.drawable.user_profile
                }
            )
                .into(authorImage)
        }


        if (comment.author_ID == currentUser.uid) {
            edit.visibility = View.VISIBLE
            delete.visibility = View.VISIBLE
        } else {
            edit.visibility = View.GONE
            delete.visibility = View.GONE
        }

        delete.setOnClickListener {
            deleteBox.visibility = View.VISIBLE
        }

        cancel.setOnClickListener {
            deleteBox.visibility = View.GONE
        }

        remove.setOnClickListener {
            db.collection("answer_comments").document(comment.comment_ID).delete().addOnSuccessListener {

                changeReputation(
                    13,
                    comment.answer_ID,
                    comment.question_ID,
                    currentUser.uid,
                    currentUser.name,
                    currentUser.image,
                    comment.author_ID,
                    TextView(viewHolder.root.context),
                    "answercomment",
                    activity,
                    currentCommunity.id
                )

                viewHolder.itemView.answer_comment_removed_filler_box.visibility = View.VISIBLE
                deleteBox.visibility = View.GONE
                delete.isClickable = false
                edit.isClickable = false

                firebaseAnalytics.logEvent("question_answer_comment_removed", null)
            }
        }

        edit.setOnClickListener {
            commentContent.visibility = View.GONE
            commentContentEditable.visibility = View.VISIBLE
            commentContentEditable.requestFocus()
            commentContentEditable.setSelection(commentContentEditable.text.length)
            delete.visibility = View.GONE
            edit.visibility = View.GONE
            save.visibility = View.VISIBLE
        }

        save.setOnClickListener {
            db.collection("answer_comments").document(comment.comment_ID)
                .set(mapOf("content" to commentContentEditable.text.toString()), SetOptions.merge())
                .addOnSuccessListener {
                    delete.visibility = View.VISIBLE
                    edit.visibility = View.VISIBLE
                    save.visibility = View.GONE

                    commentContent.visibility = View.VISIBLE
                    commentContentEditable.visibility = View.GONE

                    commentContent.text = commentContentEditable.text.toString()
                }
        }

        authorImage.setOnClickListener {
            if (currentUser.uid != comment.author_ID) {
                sharedViewModelSecondRandomUser.randomUserObject.postValue(user)
                activity.subFm.beginTransaction().add(
                    R.id.feed_subcontents_frame_container,
                    activity.profileSecondRandomUserFragment,
                    "profileSecondRandomUserFragment"
                ).addToBackStack("profileSecondRandomUserFragment")
                    .commit()
                activity.subActive = activity.profileSecondRandomUserFragment
            } else {
                activity.navigateToProfile()
            }
        }
    }
}