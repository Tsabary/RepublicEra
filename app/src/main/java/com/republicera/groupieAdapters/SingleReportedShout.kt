package com.republicera.groupieAdapters

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.ReportedShout
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.reported_shout_layout.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleReportedShout(val reportedShout: ReportedShout, val currentUser: User, val activity: MainActivity) :
    Item<ViewHolder>() {

    lateinit var db: DocumentReference

    override fun getLayout(): Int {
        return R.layout.reported_shout_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity, Observer { community ->
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(community.id)
                })
        }

        val authorName = viewHolder.itemView.reported_shout_author_name
        val authorImage = viewHolder.itemView.reported_shout_author_image
        val authorReputation = viewHolder.itemView.reported_shout_author_reputation
        val timestamp = viewHolder.itemView.reported_shout_timestamp
        val content = viewHolder.itemView.reported_shout_content
        val card = viewHolder.itemView.reported_shout_card
        val image = viewHolder.itemView.reported_shout_image
        val keep = viewHolder.itemView.reported_shout_keep
        val remove = viewHolder.itemView.reported_shout_remove


        authorName.text = reportedShout.author_name
        Glide.with(viewHolder.root.context).load(
            if (reportedShout.author_image.isEmpty()) {
                R.drawable.user_profile
            } else {
                reportedShout.author_image
            }
        ).into(authorImage)

        timestamp.text = PrettyTime().format(Date(reportedShout.timestamp))

        if (reportedShout.content.isNotEmpty()) {
            content.visibility = View.VISIBLE
            content.text = reportedShout.content
        } else {
            content.visibility = View.GONE
        }


        if (reportedShout.images.isNotEmpty()) {
            card.visibility = View.VISIBLE
            Glide.with(viewHolder.root.context).load(
                reportedShout.images[0]
            ).into(image)
        } else {
            card.visibility = View.GONE
        }

        if (reportedShout.keeps.contains(currentUser.uid)) {
            keep.setTextColor(activity.resources.getColor(R.color.green700))
        } else {
            keep.setTextColor(activity.resources.getColor(R.color.gray500))
        }

        if (reportedShout.removes.contains(currentUser.uid)) {
            keep.setTextColor(activity.resources.getColor(R.color.red700))
        } else {
            keep.setTextColor(activity.resources.getColor(R.color.gray500))
        }

        keep.setOnClickListener {
            if (reportedShout.keeps.contains(currentUser.uid)) {
                reportedShout.keeps.remove(currentUser.uid)
                keep.setTextColor(activity.resources.getColor(R.color.gray500))
                db.collection("reported_shouts").document(reportedShout.id)
                    .update("keeps", FieldValue.arrayRemove(currentUser.uid))
            } else {
                if (reportedShout.removes.contains(currentUser.uid)) {
                    reportedShout.removes.remove(currentUser.uid)
                    keep.setTextColor(activity.resources.getColor(R.color.gray500))
                    db.collection("reported_shouts").document(reportedShout.id)
                        .update("removes", FieldValue.arrayRemove(currentUser.uid))
                }

                reportedShout.keeps.add(currentUser.uid)
                keep.setTextColor(activity.resources.getColor(R.color.green700))
                db.collection("reported_shouts").document(reportedShout.id)
                    .update("keeps", FieldValue.arrayUnion(currentUser.uid))
            }
        }

        remove.setOnClickListener {
            if (reportedShout.removes.contains(currentUser.uid)) {
                reportedShout.removes.remove(currentUser.uid)
                remove.setTextColor(activity.resources.getColor(R.color.gray500))
                db.collection("reported_shouts").document(reportedShout.id)
                    .update("removes", FieldValue.arrayRemove(currentUser.uid))
            } else {

                if (reportedShout.keeps.contains(currentUser.uid)) {
                    reportedShout.keeps.remove(currentUser.uid)
                    keep.setTextColor(activity.resources.getColor(R.color.gray500))
                    db.collection("reported_shouts").document(reportedShout.id)
                        .update("keeps", FieldValue.arrayRemove(currentUser.uid))
                }

                reportedShout.removes.add(currentUser.uid)
                remove.setTextColor(activity.resources.getColor(R.color.red))
                db.collection("reported_shouts").document(reportedShout.id)
                    .update("removes", FieldValue.arrayUnion(currentUser.uid))
            }
        }
    }
}