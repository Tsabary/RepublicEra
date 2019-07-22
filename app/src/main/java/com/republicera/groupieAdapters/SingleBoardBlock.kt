package com.republicera.groupieAdapters

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Question
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_single_block.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleBoardBlock(
    val question: Question, val activity: MainActivity
) : Item<ViewHolder>() {

    lateinit var db : DocumentReference

    lateinit var currentUser : CommunityProfile
    private lateinit var currentCommunity: Community


    override fun getLayout(): Int = R.layout.board_single_block

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject
            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        Glide.with(viewHolder.root.context).load(if (currentUser.image.isNotEmpty()){currentUser.image}else{R.drawable.user_profile}).into(viewHolder.itemView.board_block_current_user_photo)

        val date = PrettyTime().format(Date(question.timestamp))
        viewHolder.itemView.board_block_timestamp.text = date
        viewHolder.itemView.board_block_question.text = question.title
        viewHolder.itemView.board_block_tags.text = question.tags.joinToString()
        viewHolder.itemView.board_block_content.text = question.details
        viewHolder.itemView.board_block_author_name.text = question.author_name

        Glide.with(viewHolder.root.context).load(if(question.author_image.isNotEmpty()){question.author_image}else{R.drawable.user_profile})
            .into(viewHolder.itemView.board_block_author_image)

        db.collection("answers").whereEqualTo("question_ID", question.id).get().addOnSuccessListener {
            viewHolder.itemView.board_block_answers.text = if (it.size()>0){
                "${it.size()} answers"
            } else{
                "0 answers"
            }
        }

    }
}