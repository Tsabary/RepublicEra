package com.republicera.groupieAdapters

import android.app.Activity
import com.bumptech.glide.Glide
import com.republicera.R
import com.republicera.interfaces.GeneralMethods
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.stax.view.*

class SingleContactDetailsIcon(val case: Int, val data: String, val activity: Activity) : Item<ViewHolder>(),
    GeneralMethods {

    override fun getLayout(): Int = R.layout.stax

    override fun bind(viewHolder: ViewHolder, position: Int) {

        Glide.with(viewHolder.root.context).load(loadIcon()).into(viewHolder.itemView.stax_icon)

        viewHolder.itemView.setOnClickListener {
            onClickStak(case, data, activity)
        }
    }

    private fun loadIcon(): Int {
        when (case) {
            0 -> {
                return R.drawable.email
            }
            1 -> {
                return R.drawable.phone
            }
            2 -> {
                return R.drawable.whatsapp
            }
            3 -> {
                return R.drawable.twitter
            }
            4 -> {
                return R.drawable.instagram
            }
            5 -> {
                return R.drawable.website
            }
            6 -> {
                return R.drawable.linkedin
            }
            7 -> {
                return R.drawable.facebook
            }
            8 -> {
                return R.drawable.medium
            }
            9 -> {
                return R.drawable.youtube
            }
            10 -> {
                return R.drawable.snapchat
            }

            else -> {
                return R.drawable.website
            }
        }
    }

}

/*

expected types:
0 = email
1 = web address
2 = Int

cases:
email = 0
phone = 1
whatsApp = 2
twitter = 3
instagram = 4
website = 5
linkedin = 6
facebook = 7
medium = 8
youtube = 9
snapchat = 10

*/