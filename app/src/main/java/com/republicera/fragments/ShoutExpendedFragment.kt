package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleComment
import com.republicera.interfaces.ShoutsMethods
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Shout
import com.republicera.models.ShoutComment
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.ShoutViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_shout_expanded.*
import kotlinx.android.synthetic.main.shout_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class ShoutExpendedFragment : Fragment(), ShoutsMethods {

    lateinit var db: DocumentReference

    val commentsAdapter = GroupAdapter<ViewHolder>()

    lateinit var currentProfile: CommunityProfile

    private lateinit var shoutViewModel: ShoutViewModel
    private lateinit var currentCommunity: Community

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_shout_expanded, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            shoutViewModel = ViewModelProviders.of(it).get(ShoutViewModel::class.java)
            currentProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val authorName = shout_expended_author_name
        val authorImage = shout_expended_author_image
        val authorReputation = shout_expended_author_reputation
        val timestamp = shout_expended_timestamp
        val content = shout_expended_content
        val card = shout_expended_card
        val image = shout_expended_image
        val likeButton = shout_expended_like_button
        val likeCount = shout_expended_like_count
        val commentCount = shout_expended_comment_count
        val currentUserImage = shout_expended_comments_current_user_image
        val commentPost = shout_expended_comments_post_button
        val commentInput = shout_expended_comments_comment_input
        val commentsRecycler = shout_expended_comments_recycler

        commentsRecycler.adapter = commentsAdapter
        commentsRecycler.layoutManager = LinearLayoutManager(this.context)

        shoutViewModel.shoutObject.observe(this, Observer {
            it?.let { shout ->

                authorName.text = shout.author_name
                Glide.with(this.context!!).load(
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

                if (shout.likes.contains(currentProfile.uid)) {
                    likeButton.setImageResource(R.drawable.heart_active)
                    likeButton.tag = "liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart)
                    likeButton.tag = "notLiked"
                }

                commentCount.text = shout.comments.toString()

                Glide.with(this.context!!).load(
                    if (currentProfile.image.isEmpty()) {
                        R.drawable.user_profile
                    } else {
                        currentProfile.image
                    }
                ).into(currentUserImage)

                if (shout.images.isNotEmpty()) {
                    card.visibility = View.VISIBLE
                    Glide.with(this.context!!).load(
                        shout.images[0]
                    ).into(image)
                } else {
                    card.visibility = View.GONE
                }

                commentCount.text = shout.comments.toString()
                listenToComments(shout, commentsAdapter, currentProfile, activity, currentCommunity.id)

                likeButton.setOnClickListener {
                    executeShoutLike(
                        shout,
                        currentProfile.uid,
                        currentProfile.name,
                        currentProfile.image,
                        likeCount,
                        likeButton,
                        shout.author_ID,
                        TextView(this.context!!),
                        activity,
                        currentCommunity.id
                    )
                }

                commentPost.setOnClickListener {
                    if (commentInput.text.isNotEmpty()) {

                        val commentDoc = db.collection("shout_comments").document()

                        val comment = ShoutComment(
                            commentDoc.id,
                            currentProfile.uid,
                            commentInput.text.toString(),
                            System.currentTimeMillis(),
                            shout.id,
                            mutableListOf()
                        )

                        commentInput.text.clear()

                        commentDoc.set(comment).addOnSuccessListener {

                            db.collection("shouts").document(shout.id)
                                .set(mapOf("last_interaction" to System.currentTimeMillis()), SetOptions.merge())
                                .addOnSuccessListener {

                                    db.collection("shouts").document(shout.id)
                                        .update("comments", FieldValue.increment(1)).addOnSuccessListener {
                                            changeReputation(
                                                16,
                                                commentDoc.id,
                                                shout.id,
                                                currentProfile.uid,
                                                currentProfile.name,
                                                currentProfile.image,
                                                shout.author_ID,
                                                TextView(this.context),
                                                "shoutComment",
                                                activity,
                                                currentCommunity.id
                                            )

                                            commentsAdapter.add(SingleComment(comment, currentProfile, activity))

                                            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                                            firebaseAnalytics.logEvent("image_comment_added", null)
                                        }
                                }


                        }
                    }
                }

            }
        })



        authorImage.setOnClickListener {
            goToProfile(activity, currentProfile)
        }

        authorName.setOnClickListener {
            goToProfile(activity, currentProfile)
        }
    }

    private fun goToProfile(activity: MainActivity, user: CommunityProfile) {
        if (user.uid != currentProfile.uid) {
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


    fun listenToComments(
        shout: Shout,
        adapter: GroupAdapter<ViewHolder>,
        currentUser: CommunityProfile,
        activity: MainActivity,
        currentCommunity: String
    ) {
        adapter.clear()
        db.collection("shout_comments").whereEqualTo("shout_ID", shout.id).get().addOnSuccessListener {
            //            commentCount.text = if (it.size() > 0) {
//                it.size().toString()
//            } else {
//                "0"
//            }

            for (doc in it) {

                val shoutComment = doc.toObject(ShoutComment::class.java)

                adapter.add(SingleComment(shoutComment, currentUser, activity))
            }

        }
    }

}
