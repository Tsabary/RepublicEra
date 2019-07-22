package com.republicera.groupieAdapters

import com.republicera.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.language_picker.view.*


class SingleLanguageOption(val language: Pair<String, String>, private val currentList : List<String>) : Item<ViewHolder>() {

    var isChecked = false

    override fun getLayout(): Int {
        return R.layout.language_picker
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val checkBox = viewHolder.itemView.language_picker_checkbox
        checkBox.text = language.second

        if(currentList.contains(language.first)){
            isChecked = true
            checkBox.isChecked = true
        }else {
            isChecked = false
            checkBox.isChecked = false
        }

        checkBox.setOnClickListener {
            isChecked = checkBox.isChecked
        }
    }
}