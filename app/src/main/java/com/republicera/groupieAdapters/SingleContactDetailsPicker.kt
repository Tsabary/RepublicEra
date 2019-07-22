package com.republicera.groupieAdapters

import com.republicera.R
import com.republicera.models.ContactInfo
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.public_contact_picker.view.*

class SingleContactDetailsPicker(val itemID: String, val contactInfo: ContactInfo, val currentList: List<String>) :
    Item<ViewHolder>() {

    var isChecked = false

    override fun getLayout(): Int {
        return R.layout.public_contact_picker
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val checkBox = viewHolder.itemView.public_contact_picker_checkbox
        val prefix = viewHolder.itemView.public_contact_picker_prefix
        val info = viewHolder.itemView.public_contact_picker_info

        prefix.text = getPrefix(contactInfo.case)
        info.text = contactInfo.info

        checkBox.isChecked = currentList.contains(itemID)
        isChecked = currentList.contains(itemID)

        checkBox.setOnClickListener {
            isChecked = checkBox.isChecked
        }
    }

    private fun getPrefix(case: Int): String? {
        return when (case) {
            0 -> null
            1 -> "+"
            2 -> "+"
            3 -> "twitter.com/"
            4 -> "instagram.com/"
            5 -> "https://"
            6 -> "linkedin.com/in/"
            7 -> "facebook.com/"
            8 -> "medium.com/@"
            9 -> "youtube.com/channel/"
            10 -> "snapchat.com/"
            else -> null
        }
    }

}