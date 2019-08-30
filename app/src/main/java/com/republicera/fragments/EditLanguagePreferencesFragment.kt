package com.republicera.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleLanguageOption
import com.republicera.interfaces.ProfileMethods
import com.republicera.models.User
import com.republicera.viewModels.CurrentUserViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

import kotlinx.android.synthetic.main.fragment_edit_language_preferences.*


class EditLanguagePreferencesFragment : Fragment(), ProfileMethods {

    lateinit var db: DocumentReference

    private lateinit var currentTopLevelUser: User
    private lateinit var currentTopLevelUserViewModel: CurrentUserViewModel


    private val languagesAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_language_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        activity.let {
            currentTopLevelUserViewModel = ViewModelProviders.of(it).get(CurrentUserViewModel::class.java)
            currentTopLevelUserViewModel.currentUserObject.observe(activity, Observer{ user ->
                currentTopLevelUser = user
                loadLanguageOptions(languagesAdapter, currentTopLevelUser.lang_list)
            })

            db = FirebaseFirestore.getInstance().collection("users").document(currentTopLevelUser.uid)
        }

        val languagesRecycler = edit_languages_recycler
        languagesRecycler.adapter = languagesAdapter
        languagesRecycler.layoutManager = GridLayoutManager(this.context, 2)

        val chosenLanguages = mutableListOf<String>()

        val saveButton = edit_languages_save
        saveButton.setOnClickListener {
            for (i in 0 until languagesAdapter.itemCount) {
                val lang = languagesAdapter.getItem(i) as SingleLanguageOption
                if (lang.isChecked) {
                    chosenLanguages.add(lang.language.first)
                }
            }
            db.set(mapOf("lang_list" to chosenLanguages), SetOptions.merge()).addOnSuccessListener {
                    val newUser = User(
                        currentTopLevelUser.uid,
                        currentTopLevelUser.first_name,
                        currentTopLevelUser.last_name,
                        currentTopLevelUser.image,
                        currentTopLevelUser.communities_list,
                        chosenLanguages,
                        currentTopLevelUser.birth_day,
                        currentTopLevelUser.reputation,
                        currentTopLevelUser.join_date,
                        currentTopLevelUser.last_activity
                    )

                    currentTopLevelUserViewModel.currentUserObject.postValue(newUser)
                    activity.userFm.popBackStack(
                        "editLanguagePreferencesFragment",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                }
        }

    }

}
