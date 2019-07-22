package com.republicera.groupieAdapters

import com.republicera.R
import com.republicera.models.ContactChannelOption
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.contact_channel_option_layout.view.*

class SingleContactChannelOption(val contactChannel: ContactChannelOption) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.contact_channel_option_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.contact_channel_option_text.text = contactChannel.title
    }
}