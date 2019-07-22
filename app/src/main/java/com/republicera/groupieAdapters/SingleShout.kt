package com.republicera.groupieAdapters

import android.app.Activity
import android.app.AlertDialog
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
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
import com.republicera.models.Shout
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.ShoutViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.shout_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleShout(val shout: Shout, val currentUser: CommunityProfile, val activity: MainActivity) : Item<ViewHolder>(),
    ShoutsMethods {

    lateinit var db: DocumentReference

    lateinit var currentProfile: CommunityProfile

    var shoutObject = shout

    lateinit var shoutViewModel: ShoutViewModel
    lateinit var currentCommunity: Community

    lateinit var menuSave: MenuItem
    lateinit var menuReport: MenuItem
    lateinit var menuEdit: MenuItem
    lateinit var menuDelete: MenuItem


    override fun getLayout(): Int = R.layout.shout_layout

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            currentProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            shoutViewModel = ViewModelProviders.of(it).get(ShoutViewModel::class.java)
            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { community ->
                    currentCommunity = community
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val authorName = viewHolder.itemView.shout_author_name
        val authorImage = viewHolder.itemView.shout_author_image
        val authorReputation = viewHolder.itemView.shout_author_reputation
        val timestamp = viewHolder.itemView.shout_timestamp
        val content = viewHolder.itemView.shout_content
        val contentEdit = viewHolder.itemView.shout_content_edit
        val contentSave = viewHolder.itemView.shout_save
        val card = viewHolder.itemView.shout_card
        val image = viewHolder.itemView.shout_image
        val likeButton = viewHolder.itemView.shout_like_button
        val likeCount = viewHolder.itemView.shout_like_count
        val commentCount = viewHolder.itemView.shout_comment_count
        val commentCta = viewHolder.itemView.shout_add_a_comment_cta
        val currentUserImage = viewHolder.itemView.shout_current_user_photo
        val menuButton = viewHolder.itemView.shout_menu

        val popup = PopupMenu(viewHolder.root.context, menuButton)
        popup.inflate(R.menu.shout)
        menuSave = popup.menu.getItem(0)
        menuReport = popup.menu.getItem(1)
        menuEdit = popup.menu.getItem(2)
        menuDelete = popup.menu.getItem(3)

        if (shoutObject.author_ID == currentProfile.uid) {
            menuSave.isVisible = false
            menuReport.isVisible = false
            menuEdit.isVisible = true
            menuDelete.isVisible = true
        } else {
            menuSave.isVisible = true
            menuReport.isVisible = true
            menuEdit.isVisible = false
            menuDelete.isVisible = false
        }

        menuButton.setOnClickListener {
            popup.show()
        }

        checkIfShoutSaved(0, activity)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.shout_save -> {
                    checkIfShoutSaved(1, activity)
                    true
                }

                R.id.shout_report -> {

                    db.collection("shouts").document(shout.id).update("reports", FieldValue.arrayUnion(currentUser.uid)).addOnSuccessListener {
                        Toast.makeText(activity, "Thank you for reporting", Toast.LENGTH_SHORT).show()
                    }

                    true
                }

                R.id.shout_edit -> {
                    contentEdit.setText(content.text)
                    content.visibility = View.GONE
                    contentEdit.visibility = View.VISIBLE
                    contentSave.visibility = View.VISIBLE
                    true
                }

                R.id.shout_delete -> {

                    AlertDialog.Builder(activity)
                        .setTitle("Remove shout")
                        .setMessage("Are you sure you want to remove this post?")
                        .setPositiveButton("Remove") { _, _ ->
                            db.collection("shouts").document(shout.id).delete().addOnSuccessListener {
                                activity.shoutsFragment.shoutsFollowingAdapter.removeGroup(position)
                                activity.shoutsFragment.shoutsFollowingAdapter.notifyDataSetChanged()
                            }
                        }
                        .show()
                    true
                }

                else -> {
                    true
                }
            }
        }

        contentSave.setOnClickListener {
            db.collection("shouts").document(shout.id)
                .set(mapOf("content" to contentEdit.text.toString()), SetOptions.merge()).addOnSuccessListener {
                    content.text = contentEdit.text.toString()
                    content.visibility = View.VISIBLE
                    contentEdit.visibility = View.GONE
                    contentSave.visibility = View.GONE
                }
        }

        authorName.text = shout.author_name
        Glide.with(viewHolder.root.context).load(
            if (shout.author_image.isEmpty()) {
                R.drawable.user_profile
            } else {
                shout.author_image
            }
        ).into(authorImage)

        timestamp.text = PrettyTime().format(Date(shout.timestamp))

        if (shout.content.isNotEmpty()) {
            content.visibility = View.VISIBLE
            content.text = shout.content
        } else {
            content.visibility = View.GONE
        }

        likeCount.text = numberCalculation(shout.likes.size.toLong())

        if (shout.likes.contains(currentUser.uid)) {
            likeButton.setImageResource(R.drawable.heart_active)
            likeButton.tag = "liked"
        } else {
            likeButton.setImageResource(R.drawable.heart)
            likeButton.tag = "notLiked"
        }

        commentCount.text = shout.comments.toString()

        Glide.with(viewHolder.root.context).load(
            if (currentUser.image.isEmpty()) {
                R.drawable.user_profile
            } else {
                currentUser.image
            }
        ).into(currentUserImage)

        if (shout.images.isNotEmpty()) {
            card.visibility = View.VISIBLE
            Glide.with(viewHolder.root.context).load(
                shout.images[0]
            ).into(image)
        } else {
            card.visibility = View.GONE
        }

        commentCount.text = shout.comments.toString()

        likeButton.setOnClickListener {
            if(shout.author_ID != currentUser.uid){
                executeShoutLike(
                    shout,
                    currentUser.uid,
                    currentUser.name,
                    currentUser.image,
                    likeCount,
                    likeButton,
                    shout.author_ID,
                    TextView(viewHolder.root.context),
                    activity,
                    currentCommunity.id
                )

                if (shoutObject.likes.contains(currentUser.uid)) {
                    shoutObject.likes.remove(currentUser.uid)
                } else {
                    shoutObject.likes.add(currentUser.uid)
                }
            }
        }

        commentCta.setOnClickListener {

            shoutViewModel.shoutObject.postValue(shoutObject)

            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.shoutExpendedFragment, "shoutExpendedFragment")
                .addToBackStack("shoutExpendedFragment").commit()
            activity.subActive = activity.shoutExpendedFragment
            activity.switchVisibility(1)
        }

        authorImage.setOnClickListener {
            goToProfile(activity, currentProfile)
        }

        authorName.setOnClickListener {
            goToProfile(activity, currentProfile)
        }
    }

    private fun goToProfile(activity: MainActivity, user: CommunityProfile) {
        if (user.uid != currentUser.uid) {
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

    private fun checkIfShoutSaved(event: Int, activity: Activity) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        db.collection("saved_shouts").document(currentProfile.uid).get().addOnSuccessListener {

            val doc = it.data
            if (doc != null) {

                val list = doc["saved_shouts"] as List<String>

                if (list.contains(shout.id)) {
                    if (event == 0) {
                        menuSave.title = "Saved"
                    } else {
                        db.collection("saved_shouts").document(currentProfile.uid)
                            .update("saved_shouts", FieldValue.arrayRemove(shout.id)).addOnSuccessListener {
                                menuSave.title = "Save"

                                firebaseAnalytics.logEvent("shout_unsaved", null)
                                (activity as MainActivity).savedQuestionFragment.listenToQuestions()
                            }
                    }

                } else {
                    if (event == 0) {
                        menuSave.title = "Save"
                    } else {
                        db.collection("saved_shouts").document(currentProfile.uid)
                            .update("saved_shouts", FieldValue.arrayUnion(shout.id))
                            .addOnSuccessListener {
                                menuSave.title = "Saved"

                                firebaseAnalytics.logEvent("shout_saved", null)
                                (activity as MainActivity).savedQuestionFragment.listenToQuestions()
                            }
                    }
                }
            }
        }
    }


}