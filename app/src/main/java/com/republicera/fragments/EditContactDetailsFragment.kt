package com.republicera.fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleContactChannel
import com.republicera.groupieAdapters.SingleContactChannelOption
import com.republicera.interfaces.ProfileMethods
import com.republicera.models.CommunityProfile
import com.republicera.models.ContactChannel
import com.republicera.models.ContactInfo
import com.republicera.viewModels.ContactDetailsViewModel
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_edit_contact_details.*
import java.util.*


class EditContactDetailsFragment : Fragment(), ProfileMethods {

    val db = FirebaseFirestore.getInstance()

    lateinit var currentUser: CommunityProfile
    lateinit var contactDetailsViewModel: ContactDetailsViewModel
//    var contactDetailsList = mutableListOf<ContactInfo>()

    val contactDetailsAdapter = GroupAdapter<ViewHolder>()
    val contactChannelsOptionsAdapter = GroupAdapter<ViewHolder>()
    lateinit var searchInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_contact_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity
        activity.let {
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject
            contactDetailsViewModel = ViewModelProviders.of(it).get(ContactDetailsViewModel::class.java)
//            contactDetailsViewModel.contactMap.observe(activity, Observer { list ->
//                contactDetailsList = list
//            })
        }

        searchInput = edit_contact_details_search

        val contactChannelOptionsRecycler = edit_contact_channels_options_recycler
        contactChannelOptionsRecycler.adapter = contactChannelsOptionsAdapter
        contactChannelOptionsRecycler.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)

        val contactDetailsRecycler = edit_contact_details_recycler
        contactDetailsRecycler.adapter = contactDetailsAdapter
        contactDetailsRecycler.layoutManager = LinearLayoutManager(this.context)

        loadContactChannelOptions(contactChannelsOptionsAdapter, "", activity)

        listenToExistingDetails()

        contactChannelsOptionsAdapter.setOnItemClickListener { item, _ ->

            val option = item as SingleContactChannelOption

            contactDetailsAdapter.add(
                SingleContactChannel(
                    ContactChannel(
                        option.contactChannel.case,
                        option.contactChannel.title
                    ),
                    activity,
                    "",
                    null
                )
            )

            contactDetailsRecycler.smoothScrollToPosition(contactDetailsAdapter.itemCount)
        }




//        loadContactChannels(contactDetailsAdapter, "", activity, contactDetailsList)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadContactChannelOptions(contactChannelsOptionsAdapter, s.toString(), activity)
            }
        })

        val saveButton = edit_contact_details_save

        saveButton.setOnClickListener {
            saveContactDetails()
        }
    }

    private fun saveContactDetails() {

        searchInput.text.clear()

        val list = mutableMapOf<String, ContactInfo>()

        for (i in 0 until contactDetailsAdapter.itemCount) {

            val item = contactDetailsAdapter.getItem(i) as SingleContactChannel

            if(item.info.isNotEmpty()){
                list[item.existingIdentifier ?: UUID.randomUUID().toString()] = ContactInfo(item.case, item.info)
            }
        }


        db.collection("contact_details").document(currentUser.uid).set(list).addOnSuccessListener {
            val activity = activity as MainActivity
            activity.fm.beginTransaction().detach(activity.profileCurrentUserFragment)
                .attach(activity.profileCurrentUserFragment).commit()

            activity.subFm.popBackStack("editContactDetailsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            activity.switchVisibility(0)
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

    private fun listenToExistingDetails() {

        contactDetailsAdapter.clear()

        db.collection("contact_details").document(currentUser.uid).get().addOnSuccessListener {
            val doc = it.data
            if (doc != null) {
                val detailsList = doc as Map<String, Map<String, Any>>

                for (field in detailsList) {

                    val actualInfo = ContactInfo(
                        field.value.getValue("case").toString().toInt(),
                        field.value.getValue("info").toString()
                    )

                    contactDetailsAdapter.add(
                        SingleContactChannel(
                            ContactChannel(
                                actualInfo.case,
                                when (actualInfo.case) {
                                    0 -> "Email"
                                    1 -> "Phone"
                                    2 -> "WhatsApp"
                                    3 -> "Twitter"
                                    4 -> "Instagram"
                                    5 -> "Website"
                                    6 -> "LinkedIn"
                                    7 -> "Facebook"
                                    8 -> "Medium"
                                    9 -> "YouTube"
                                    10 -> "Snapchat"
                                    else -> ""
                                }
                            ), activity as MainActivity,
                            actualInfo.info,
                            field.key
                        )
                    )
                }
            }
        }
    }
}
