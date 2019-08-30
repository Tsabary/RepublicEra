package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.algolia.search.saas.Client
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity

import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.CurrentUserViewModel
import kotlinx.android.synthetic.main.fragment_new_community.*
import org.json.JSONObject


class NewCommunityFragment : Fragment() {

    private lateinit var communityViewModel: CurrentCommunityViewModel

    var topLevelUser: User? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val activity = activity as MainActivity

        activity.let {
            ViewModelProviders.of(activity).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity, Observer {
                    topLevelUser = it
                })

            communityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)

        }

        val titleInput = new_community_title_input
        val descriptionInput = new_community_description_input
        val publicButton = new_community_public
        val privateButton = new_community_private
        val startButton = new_community_start_button

        startButton.setOnClickListener {

            if (topLevelUser != null) {

                if (titleInput.text.length in 2..35 && descriptionInput.text.length in 2..150) {
                    val communityDoc = FirebaseFirestore.getInstance().collection("communities").document()
                    val title = titleInput.text.toString()
                    val description = descriptionInput.text.toString()

                    val newCommunity =
                        Community(
                            communityDoc.id,
                            title,
                            description,
                            "",
                            0,
                            0,
                            publicButton.isChecked,
                            topLevelUser!!.uid,
                            mutableListOf(topLevelUser!!.uid)
                        )
                    communityDoc.set(newCommunity).addOnSuccessListener {
                        Toast.makeText(this.context, "Community started", Toast.LENGTH_SHORT).show()

                        communityViewModel.currentCommunity.postValue(newCommunity)
                        activity.userHomeFrame.visibility = View.GONE
                        titleInput.text.clear()
                        descriptionInput.text.clear()
                        publicButton.isChecked = true
                        privateButton.isChecked = false
                    }.addOnFailureListener {
                        Toast.makeText(this.context, "Community failed" + it.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this.context, "Community name is too short", Toast.LENGTH_SHORT).show()
                }
            } else {
                activity.takeToRegister()
            }
        }
    }
}
