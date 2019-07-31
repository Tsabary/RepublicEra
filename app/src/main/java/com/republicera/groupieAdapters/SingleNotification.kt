package com.republicera.groupieAdapters

import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Notification
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.QuestionViewModel
import com.republicera.viewModels.RandomUserViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.notification_single_row.view.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleNotification(
    val notification: Notification,
    val activity: MainActivity,
    val currentUser: CommunityProfile,
    val notification_ID : String
) :
    Item<ViewHolder>() {

    lateinit var db: DocumentReference

    lateinit var sharedViewModelQuestion: QuestionViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel
    lateinit var currentCommunity: Community

    override fun getLayout(): Int = R.layout.notification_single_row

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            sharedViewModelQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val notificationBox = viewHolder.itemView.notification_box

//        viewHolder.itemView.notification_timestamp.text = PrettyTime().format(Date(notification.timestamp.values))

        viewHolder.itemView.notification_content.text = when (notification.scenario_type) {

            0 -> {
                Spanner()
                    .append(notification.initiator_name)
                    .append(" upvoted your question", Spans.font("roboto_medium"))
            }

            2 -> {
                Spanner()
                    .append(notification.initiator_name)
                    .append(" upvoted your answer", Spans.font("roboto_medium"))
            }

            4 -> {

                Spanner()
                    .append("Someone")
                    .append(" downvoted your question ", Spans.font("roboto_medium"))
            }

            6 -> {
                Spanner()
                    .append("Someone")
                    .append(" downvoted your answer", Spans.font("roboto_medium"))
            }

            8 -> {

                Spanner()
                    .append(notification.initiator_name)
                    .append(" answered your question", Spans.font("roboto_medium"))
            }

            10 -> {
                Spanner()
                    .append(notification.initiator_name)
                    .append(" bookmarked your question", Spans.font("roboto_medium"))
            }

            12 -> {
                Spanner()
                    .append(notification.initiator_name)
                    .append(" liked your shout", Spans.font("roboto_medium"))
            }

            14 -> {
                Spanner()
                    .append(notification.initiator_name)
                    .append(" liked your comment", Spans.font("roboto_medium"))
            }

            994 -> {
                Spanner()
                    .append(notification.initiator_name)
                    .append(" commented on your shout", Spans.font("roboto_medium"))
            }

            996 -> {
            Spanner()
                .append(notification.initiator_name)
                .append(" commented on your answer", Spans.font("roboto_medium"))
        }

            998 -> {
                Spanner()
                    .append(notification.initiator_name)
                    .append(" started following you", Spans.font("roboto_medium"))
            }

            else -> "Notification failed to load"
        }


        viewHolder.itemView.notification_initiator_image.setOnClickListener {
            if (notification.scenario_type != 4 || notification.scenario_type != 6) {

                db.collection("profiles").document(notification.initiator_ID).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val user = documentSnapshot.toObject(CommunityProfile::class.java)
                        if (user != null) {
                            sharedViewModelRandomUser.randomUserObject.postValue(user)
                            activity.subFm.beginTransaction().add(
                                R.id.feed_subcontents_frame_container,
                                activity.profileRandomUserFragment,
                                "profileRandomUserFragment"
                            ).addToBackStack("profileRandomUserFragment").commit()

                            activity.subActive = activity.profileRandomUserFragment
                        }
                    }
            }
        }


        if (notification.seen == 0) {
            notificationBox.setBackgroundColor(ContextCompat.getColor(viewHolder.root.context, R.color.mainColor50))
        } else {
            notificationBox.setBackgroundColor(ContextCompat.getColor(viewHolder.root.context, R.color.white))
        }

        Glide.with(viewHolder.root.context).load(
            if (notification.scenario_type == 4 || notification.scenario_type == 6) {
                R.drawable.user_profile
            } else {
                if (notification.initiator_image.isNotEmpty()) {
                    notification.initiator_image
                } else {
                    R.drawable.user_profile
                }
            }
        )
            .into(viewHolder.itemView.notification_initiator_image)


    //0: question upvote +5 to receiver +notification // type 0
    //2: answer upvoted +10 to receiver +notification  // type 1
    //4: question downvote -2 for receiver -1 for initiator +notification without initiator  // type 0 or 1
    //6: answer downvote -2 for receiver -1 for initiator +notification without initiator  // type 0 or 1
    //8: answer given +2 to initiator
    //10: question bookmark +5 to receiver +notification  // type 0
    //12: answer receives a comment
    //14: shout liked
    //16: comment on a shout
    // 18: comment on a shout liked
    // 20: profile got a follow
    }
}
