package com.republicera.groupieAdapters

import com.republicera.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.language_picker.view.*
import kotlinx.android.synthetic.main.language_picker_board.view.*


class SingleLanguageOptionBoard(val language: String) : Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.language_picker_board
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val checkBox = viewHolder.itemView.language_picker_board
        checkBox.text = language
    }
}