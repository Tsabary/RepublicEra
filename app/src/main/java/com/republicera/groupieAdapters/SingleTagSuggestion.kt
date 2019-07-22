package com.republicera.groupieAdapters

import com.republicera.R
import com.republicera.models.SingleTagForList
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.tag_auto_complete.view.*


class SingleTagSuggestion(val tag: SingleTagForList) : Item<ViewHolder>() {
    override fun getLayout(): Int = R.layout.tag_auto_complete


    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tag_name.text = tag.tagString
        viewHolder.itemView.tag_count.text = tag.tagCount.toString()
    }
}