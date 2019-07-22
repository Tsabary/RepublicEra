package com.republicera.groupieAdapters

import android.view.View
import android.widget.ImageButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.interfaces.ShoutsMethods
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.ShoutComment
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.RandomUserSecondViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.comment_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleComment(
    var comment: ShoutComment,
    val currentUser: CommunityProfile,
    val activity: MainActivity
) :
    Item<ViewHolder>(),
    ShoutsMethods {

    lateinit var db: DocumentReference


    lateinit var sharedViewModelSecondRandomUser: RandomUserSecondViewModel
    private lateinit var currentCommunity: Community

    lateinit var user: CommunityProfile
    lateinit var likeButton: ImageButton

    var likesList = mutableListOf<String>()

    override fun getLayout(): Int = R.layout.comment_layout

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

        val content = viewHolder.itemView.single_comment_content
        val timestamp = viewHolder.itemView.single_comment_timestamp
        val likeCount = viewHolder.itemView.single_comment_like_count
        likeButton = viewHolder.itemView.single_comment_like_button
        val author = viewHolder.itemView.single_comment_author
        val authorImage = viewHolder.itemView.single_comment_photo
        val authorReputation = viewHolder.itemView.single_comment_author_reputation
        val editButton = viewHolder.itemView.single_comment_edit
        val saveButton = viewHolder.itemView.single_comment_save
        val deleteButton = viewHolder.itemView.single_comment_delete
        val contentEditable = viewHolder.itemView.single_comment_content_editable
        val deleteContainer = viewHolder.itemView.single_comment_delete_container
        val cancelButton = viewHolder.itemView.single_comment_cancel
        val removeButton = viewHolder.itemView.single_comment_remove

        val date = PrettyTime().format(Date(comment.timestamp))

        content.text = comment.content
        contentEditable.setText(comment.content)
        timestamp.text = date

        likeCount.text = numberCalculation(comment.likes.size.toLong())

        if (comment.likes.contains(currentUser.uid)) {
            likeButton.setImageResource(R.drawable.heart_active)
            likeButton.tag = "liked"
        } else {
            likeButton.setImageResource(R.drawable.heart)
            likeButton.tag = "notLiked"
        }

        likeCount.text = comment.likes.size.toString()

//        listenToImageCommentLikeCount(likeCount, comment)


        likeButton.setOnClickListener {
            if (currentUser.uid != comment.author_ID) {
                executeCommentLike(
                    comment,
                    currentUser.uid,
                    currentUser.name,
                    currentUser.image,
                    likeCount,
                    likeButton,
                    comment.author_ID,
                    authorReputation,
                    activity,
                    currentCommunity.id
                )
            }
        }

        authorImage.setOnClickListener {
            goToProfile()
        }
        author.setOnClickListener {
            goToProfile()
        }
        authorReputation.setOnClickListener {
            goToProfile()
        }

        db.collection("profiles").document(comment.author_ID).get().addOnSuccessListener {
            if (it != null) {
                user = it.toObject(CommunityProfile::class.java)!!

                author.text = user.name
                authorReputation.text = "(${user.reputation})"

                Glide.with(viewHolder.root.context).load(
                    if (user.image.isNotEmpty()) {
                        user.image
                    } else {
                        R.drawable.user_profile
                    }
                ).into(authorImage)
            }

        }

        if (comment.author_ID == currentUser.uid) {
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE

            editButton.setOnClickListener {
                editButton.visibility = View.GONE
                saveButton.visibility = View.VISIBLE
                content.visibility = View.GONE
                contentEditable.visibility = View.VISIBLE
                contentEditable.requestFocus()
                contentEditable.setSelection(contentEditable.text.length)
            }

            saveButton.setOnClickListener {

                db.collection("shout_comments").document(comment.id)
                    .set(mapOf("content" to contentEditable.text.toString()), SetOptions.merge()).addOnSuccessListener {

                        content.text = contentEditable.text.toString()
                        editButton.visibility = View.VISIBLE
                        saveButton.visibility = View.GONE
                        content.visibility = View.VISIBLE
                        contentEditable.visibility = View.GONE

                        val newComment = ShoutComment(
                            comment.id,
                            comment.author_ID,
                            contentEditable.text.toString(),
                            comment.timestamp,
                            comment.shout_ID,
                            comment.likes
                        )
                        comment = newComment
                        closeKeyboard(activity)

                    }
            }

            deleteButton.setOnClickListener {
                deleteContainer.visibility = View.VISIBLE
            }

            cancelButton.setOnClickListener {
                deleteContainer.visibility = View.GONE
            }

            removeButton.setOnClickListener {

                db.collection("shout_comments").document(comment.id).delete().addOnSuccessListener {

                    db.collection("shouts").document(comment.shout_ID)
                        .update("comments", FieldValue.increment(-1)).addOnSuccessListener {
                            activity.shoutExpendedFragment.commentsAdapter.removeGroup(position)
                            activity.shoutExpendedFragment.commentsAdapter.notifyDataSetChanged()
                            deleteContainer.visibility = View.GONE

                            val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.itemView.context)
                            firebaseAnalytics.logEvent("shout_comment_removed", null)
                        }

                }
            }
        }
    }

//    private fun listenToImageCommentLikeCount(commentLikeCount: TextView, comment: ShoutComment) {
//
//        db.collection("shout_comment_likes").document(comment.id).get().addOnSuccessListener {
//
//            val doc = it.data
//            if (doc != null) {
//                likesList = doc["likes_list"] as MutableList<String>
//                commentLikeCount.text = numberCalculation(likesList.size.toLong())
//
//                if (likesList.contains(currentUser.uid)) {
//                    likeButton.setImageResource(R.drawable.heart_active)
//                    likeButton.tag = "liked"
//                } else {
//                    likeButton.setImageResource(R.drawable.heart)
//                    likeButton.tag = "notLiked"
//                }
//            }
//        }
//    }


    private fun goToProfile() {
        if (currentUser.uid != comment.author_ID) {
            sharedViewModelSecondRandomUser.randomUserObject.postValue(user)
            activity.subFm.beginTransaction()
                .add(
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
