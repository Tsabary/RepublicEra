package com.republicera.groupieAdapters

import com.republicera.R
import com.republicera.models.Answer
import com.republicera.models.Question
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.question_and_answer_single_row.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class SingleQuestionAndAnswerRow(
    val question : Question,
    val answer : Answer
) : Item<ViewHolder>() {
    override fun getLayout(): Int = R.layout.question_and_answer_single_row

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.question_and_answer_question_title.text = question.title
        viewHolder.itemView.question_and_answer_answer_content.text = answer.content
        val date = PrettyTime().format(Date(answer.timestamp))
        viewHolder.itemView.question_and_answer_answer_timestamp.text = date
    }
}