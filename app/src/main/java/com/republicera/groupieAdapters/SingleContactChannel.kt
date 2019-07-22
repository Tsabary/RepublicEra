package com.republicera.groupieAdapters

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProviders
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.ContactChannel
import com.republicera.models.ContactInfo
import com.republicera.viewModels.ContactDetailsViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.contact_channel_layout.view.*


class SingleContactChannel(
    val contactChannel: ContactChannel,
    val activity: MainActivity,
    val content: String,
    val existingIdentifier : String?
) : Item<ViewHolder>() {

    lateinit var contactDetailsViewModel: ContactDetailsViewModel
    lateinit var contactDetailsList : Map<String, ContactInfo>

    lateinit var emailInput: EditText
    lateinit var webAddressInput: EditText
    lateinit var numbersInput: EditText

    var info = content
    var case = contactChannel.case
    var identifier :String? = existingIdentifier


    val expectedType = getExpectedType(contactChannel.case)

    val prefix = getPrefix(contactChannel.case)

    override fun getLayout(): Int {
        return R.layout.contact_channel_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.setIsRecyclable(false)

        val activity = activity

        emailInput = viewHolder.itemView.contact_channel_input_email
        webAddressInput = viewHolder.itemView.contact_channel_input_web_address
        numbersInput = viewHolder.itemView.contact_channel_input_numbers
        val removeChannel = viewHolder.itemView.contact_channel_remove

        removeChannel.setOnClickListener {
            activity.editContactDetailsFragment.contactDetailsAdapter.removeGroup(position-1)
        }


        activity.let {
            contactDetailsViewModel = ViewModelProviders.of(it).get(ContactDetailsViewModel::class.java)
//            contactDetailsViewModel.contactMap.observe(activity, Observer { list ->
//                contactDetailsList = list
//            })
        }



        when (expectedType) {
            0 -> {
                emailInput.visibility = View.VISIBLE
                webAddressInput.visibility = View.GONE
                numbersInput.visibility = View.GONE
                emailInput.setText(info)
                webAddressInput.setText("")
                numbersInput.setText("")
            }

            1 -> {
                emailInput.visibility = View.GONE
                webAddressInput.visibility = View.VISIBLE
                numbersInput.visibility = View.GONE
                emailInput.setText("")
                webAddressInput.setText(info)
                numbersInput.setText("")

            }

            2 -> {
                emailInput.visibility = View.GONE
                webAddressInput.visibility = View.GONE
                numbersInput.visibility = View.VISIBLE
                emailInput.setText("")
                webAddressInput.setText("")
                numbersInput.setText(info)
            }
        }


        val title = viewHolder.itemView.contact_channel_title
        val prefixView = viewHolder.itemView.contact_channel_prefix

        emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                contactDetailsList[contactChannel.case.toString()] = s.toString()
//                contactDetailsViewModel.contactMap.postValue(contactDetailsList)

                info = s.toString()
            }
        })


        webAddressInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                contactDetailsList[contactChannel.case.toString()] = s.toString()
//                contactDetailsViewModel.contactMap.postValue(contactDetailsList)

                info = s.toString()
            }
        })

        numbersInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                contactDetailsList[contactChannel.case.toString()] = s.toString()
//                contactDetailsViewModel.contactMap.postValue(contactDetailsList)

                info = s.toString()
            }
        })

        title.text = contactChannel.title

        if (prefix != null) {
            prefixView.visibility = View.VISIBLE
            prefixView.text = prefix
        } else {
            prefixView.visibility = View.GONE
        }
    }

    private fun getExpectedType(case : Int) : Int?{
        return when (case) {
            0 -> 0
            1 -> 2
            2 -> 2
            3 -> 1
            4 -> 1
            5 -> 1
            6 -> 1
            7 -> 1
            8 -> 1
            9 -> 1
            10 -> 1
            else -> null
        }
    }

    private fun getPrefix(case : Int) : String? {
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

    //    cases:
    //    email = 0
    //    phone = 1
    //    whatsApp = 2
    //    twitter = 3
    //    instagram = 4
    //    website = 5
    //    linkedin = 6
    //    facebook = 7
    //    medium = 8
    //    youtube = 9
    //    snapchat = 10
}