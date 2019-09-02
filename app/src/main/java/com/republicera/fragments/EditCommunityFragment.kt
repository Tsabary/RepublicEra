package com.republicera.fragments


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.republicera.MainActivity

import com.republicera.R
import com.republicera.models.Community
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityIdViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.CurrentUserViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_new_community.*
import org.json.JSONObject


class EditCommunityFragment : Fragment() {

    private lateinit var communityIdViewModel: CurrentCommunityIdViewModel
    private lateinit var communityViewModel: CurrentCommunityViewModel

    lateinit var titleInput: EditText
    lateinit var descriptionInput: EditText
    lateinit var publicButton: RadioButton
    lateinit var privateButton: RadioButton
    lateinit var startButton: TextView

    var topLevelUser: User? = null
    lateinit var communityImage: CircleImageView
    private var selectedPhotoUri: Uri? = null

    private lateinit var currentCommunityId: String
    lateinit var currentCommunity: Community

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

        communityImage = new_community_image
        titleInput = new_community_title_input
        descriptionInput = new_community_description_input
        publicButton = new_community_public
        privateButton = new_community_private
        startButton = new_community_start_button

        startButton.text = getString(R.string.update)

        activity.let {
            ViewModelProviders.of(activity).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity, Observer {
                    topLevelUser = it
                })

            communityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)

            communityIdViewModel = ViewModelProviders.of(it).get(CurrentCommunityIdViewModel::class.java)
            communityIdViewModel.currentCommunity.observe(activity, Observer { communityId ->
                currentCommunityId = communityId

                FirebaseFirestore.getInstance().document("communities/$communityId").get()
                    .addOnSuccessListener { documentSnapshot ->
                        val community = documentSnapshot.toObject(Community::class.java)
                        if (community != null) {
                            currentCommunity = community
                            titleInput.setText(community.title)
                            descriptionInput.setText(community.description)

                            publicButton.isChecked = community.public
                            privateButton.isChecked = !community.public

                            Glide.with(activity).load(community.image).into(communityImage)

                        }
                    }

            })

        }



        communityImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            this.startActivityForResult(intent, 50)
        }

        startButton.setOnClickListener {

            if (topLevelUser != null) {

                if (titleInput.text.length in 2..35 && descriptionInput.text.length in 2..150) {
                    if (selectedPhotoUri != null) {
                        uploadImageToFirebase()
                    } else {
                        val communityDoc =
                            FirebaseFirestore.getInstance().document("communities/$currentCommunityId")
                        updateCommunity(communityDoc, currentCommunity.image)
                    }
                } else {
                    Toast.makeText(this.context, "Community name is too short", Toast.LENGTH_SHORT).show()
                }
            } else {
                activity.takeToRegister()
            }
        }
    }

    private fun uploadImageToFirebase() {

        if (selectedPhotoUri != null) {

            val communityDoc = FirebaseFirestore.getInstance().document("communities/$currentCommunityId")

            val ref = FirebaseStorage.getInstance().getReference("/images/community_pics/$currentCommunityId")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { newImageUri ->
                        updateCommunity(communityDoc, newImageUri.toString())
                    }
                }
        }
    }

    private fun updateCommunity(communityDoc: DocumentReference, image: String) {
        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()

        val newCommunity =
            Community(
                communityDoc.id,
                title,
                description,
                image,
                currentCommunity.members,
                currentCommunity.active_members,
                publicButton.isChecked,
                currentCommunity.founder,
                currentCommunity.admins
            )
        communityDoc.set(newCommunity).addOnSuccessListener {
            Toast.makeText(this.context, "Community updated", Toast.LENGTH_SHORT).show()

//            communityViewModel.currentCommunity.postValue(newCommunity)

//            (activity as MainActivity).userHomeFrame.visibility = View.GONE
            titleInput.text.clear()
            descriptionInput.text.clear()
            publicButton.isChecked = true
            privateButton.isChecked = false
        }.addOnFailureListener {
            Toast.makeText(this.context, "Failed to update community" + it.localizedMessage, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(activity, "edit community photo", Toast.LENGTH_LONG).show()

        if (requestCode == 50 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            if (selectedPhotoUri != null) {
                Glide.with(this.context!!).load(selectedPhotoUri.toString()).into(communityImage)
            }
        }
    }
}
