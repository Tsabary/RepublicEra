package com.republicera.groupieAdapters

import com.republicera.R
import com.republicera.models.Community
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.community_option_layout.view.*

class SingleCommunityOption(val community: Community) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.community_option_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val title = viewHolder.itemView.community_option_title
        val description = viewHolder.itemView.community_option_description
        val memberCount = viewHolder.itemView.community_option_members_count
        title.text = community.title
        description.text = community.description
        memberCount.text = "${community.members}"
    }
}