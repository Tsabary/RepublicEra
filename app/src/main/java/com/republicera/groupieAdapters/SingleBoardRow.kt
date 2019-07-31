package com.republicera.groupieAdapters

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.Question
import com.republicera.viewModels.CurrentCommunityViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.board_single_row.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleBoardRow(
    val question: Question,
    val activity : MainActivity
) : Item<ViewHolder>() {

    lateinit var db : DocumentReference

    private lateinit var currentCommunity: Community


    override fun getLayout(): Int = R.layout.board_single_row

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }


        val date = PrettyTime().format(question.timestamp)

        viewHolder.itemView.board_question.text = question.title
        viewHolder.itemView.board_tags.text = question.tags.joinToString()
        viewHolder.itemView.board_timestamp.text = date
        viewHolder.itemView.board_answers.text = question.answers.toString()
    }
}