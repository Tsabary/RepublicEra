package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity

import com.republicera.R
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.CurrentUserViewModel
import kotlinx.android.synthetic.main.fragment_edit_basic_info.*

class EditBasicInfoFragment : Fragment(), GeneralMethods {

    val db = FirebaseFirestore.getInstance()

    lateinit var currentUser: User
    lateinit var currentUserViewModel: CurrentUserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_basic_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val firstNameInput = edit_basic_information_first_name_input
        val lastNameInput = edit_basic_information_last_name_input

        val activity = activity as MainActivity

        activity.let {
            currentUserViewModel = ViewModelProviders.of(it).get(CurrentUserViewModel::class.java)
            currentUserViewModel.currentUserObject.observe(activity, Observer { user ->
                currentUser = user

                firstNameInput.setText(currentUser.first_name)
                lastNameInput.setText(currentUser.last_name)
            })

//            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(activity, Observer { community ->
//                db = FirebaseFirestore.getInstance().collection("communities_data").document(community.id)
//            })
        }


        val saveButton = edit_basic_info_save
        saveButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            db.collection("users").document(currentUser.uid)
                .update(mapOf("first_name" to firstName, "last_name" to lastName)).addOnSuccessListener {
                activity.userFm.popBackStack("editBasicInfoFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

                val newUser = User(
                    currentUser.uid,
                    firstName,
                    lastName,
                    currentUser.image,
                    currentUser.communities_list,
                    currentUser.lang_list,
                    currentUser.birth_day,
                    currentUser.reputation,
                    currentUser.join_date,
                    currentUser.last_activity
                )

                    currentUserViewModel.currentUserObject.postValue(newUser)
                    closeKeyboard(activity)
            }
        }
    }
}
