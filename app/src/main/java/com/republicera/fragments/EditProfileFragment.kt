package com.republicera.fragments


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleContactDetailsPicker
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.ContactInfo
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import java.util.*
import kotlin.collections.HashMap

class EditProfileFragment : Fragment() {

    lateinit var db : DocumentReference

    private var selectedPhotoUri: Uri? = null

    private val contactDetailsAdapter = GroupAdapter<ViewHolder>()

    lateinit var userImage: CircleImageView
    lateinit var tagLineInput: EditText
    lateinit var userNameInput: EditText
    lateinit var saveButton: TextView

    lateinit var currentCommunityProfileViewModel: CurrentCommunityProfileViewModel
    lateinit var currentProfile: CommunityProfile

    lateinit var currentCommunityViewModel: CurrentCommunityViewModel
    lateinit var currentCommunity: Community

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_edit_profile, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            //            currentProfileViewModel = ViewModelProviders.of(it).get(CurrentUserViewModel::class.java)
//            currentProfile = currentProfileViewModel.currentUserObject

            currentCommunityProfileViewModel =
                ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
            currentProfile = currentCommunityProfileViewModel.currentCommunityProfileObject

            currentCommunityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)
            currentCommunityViewModel.currentCommunity.observe(activity, Observer { communityName ->
                currentCommunity = communityName
                db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
            })
        }

        userImage = edit_profile_image
        tagLineInput = edit_profile_description
        userNameInput = edit_profile_name

        val contactDetailsRecycler = edit_profile_contacts_recycler
        contactDetailsRecycler.adapter = contactDetailsAdapter
        contactDetailsRecycler.layoutManager = LinearLayoutManager(this.context)

        saveButton = edit_profile_save


        setUpUserDetails()
        getContactDetails()

        userImage.setOnClickListener {
            if (hasNoPermissions()) {
                requestPermission()
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "image/*"
                startActivityForResult(intent, 10)
            }
        }


        saveButton.setOnClickListener {
            saveButton.isClickable = false
            edit_profile_loading.visibility = View.VISIBLE
            uploadImageToFirebase(currentProfile.image)
        }
    }

    private fun getContactDetails() {

        contactDetailsAdapter.clear()

        FirebaseFirestore.getInstance().collection("contact_details").document(currentProfile.uid).get().addOnSuccessListener {

            val allContactInfoDoc = it.data
            if (allContactInfoDoc != null) {
                for (field in allContactInfoDoc) {
                    val contactInfoMap = field.value as HashMap<String, Any>
                    contactDetailsAdapter.add(
                        SingleContactDetailsPicker(
                            field.key,
                            ContactInfo(contactInfoMap["case"]!!.toString().toInt(), contactInfoMap["info"].toString()),
                            currentProfile.contact_info
                        )
                    )

                    Log.d("detaill", contactInfoMap["info"].toString())
                }


//                db.collection("${currentCommunity.first}_community_profile").document(currentProfile.uid).get()
//                    .addOnSuccessListener { documentSnapshot ->
//                        val communityProfileDoc = documentSnapshot.toObject(CommunityProfile::class.java)
//                        if (communityProfileDoc != null) {
//
//                        }
//                    }
            }

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data


            if (selectedPhotoUri != null) {
                Glide.with(this.context!!).load(selectedPhotoUri.toString()).into(userImage)
            }
        }
    }


    private fun performUpdateProfile(imageUri: String) {
        val activity = activity as MainActivity

        if (userNameInput.length() > 2) {

            val name = userNameInput.text.toString().trimEnd()
            val tagline = tagLineInput.text.toString().trimEnd()
            val contactDetailsList = mutableListOf<String>()

            for (i in 0 until contactDetailsAdapter.itemCount) {
                val channel = contactDetailsAdapter.getItem(i) as SingleContactDetailsPicker
                if (channel.isChecked) {
                    contactDetailsList.add(channel.itemID)
                }
            }

            db.collection("profiles").document(currentProfile.uid).set(
                mapOf(
                    "name" to name,
                    "tag_line" to tagline,
                    "image" to imageUri,
                    "contact_info" to contactDetailsList
                )
                , SetOptions.merge()
            ).addOnSuccessListener {

                val updatedCurrentProfile = CommunityProfile(
                    currentProfile.uid,
                    name,
                    imageUri,
                    contactDetailsList,
                    tagline,
                    currentProfile.reputation,
                    currentProfile.followers,
                    currentProfile.answers,
                    currentProfile.questions,
                    currentProfile.shouts,
                    currentProfile.join_date,
                    currentProfile.last_activity
                )


                currentCommunityProfileViewModel.currentCommunityProfileObject = updatedCurrentProfile

                activity.fm.beginTransaction().detach(activity.profileCurrentUserFragment)
                    .attach(activity.profileCurrentUserFragment).commit()

                activity.subFm.popBackStack("editProfileFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

                activity.switchVisibility(0)
            }.addOnFailureListener {
                saveButton.isClickable = true
                edit_profile_loading.visibility = View.GONE
            }

        } else {
            Toast.makeText(this.context, "Name can't be less than 2 letters", Toast.LENGTH_LONG).show()
        }
    }


    private fun uploadImageToFirebase(currentImageUri: String) {

        if (selectedPhotoUri != null) {
            val ref = FirebaseStorage.getInstance().getReference("/images/$currentCommunity/profile_pics/${currentProfile.uid}")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { newImageUri ->
                        performUpdateProfile(newImageUri.toString())
                    }
                }
        } else {
            performUpdateProfile(currentImageUri)
        }
    }


    private fun setUpUserDetails() {

        Glide.with(this).load(
            if (currentProfile.image.isNotEmpty()) {
                currentProfile.image
            } else {
                R.drawable.user_profile
            }
        ).into(userImage)
        tagLineInput.setText(currentProfile.tag_line)
        userNameInput.setText(currentProfile.name)
    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity as MainActivity, permissions, 0)
    }

    companion object {
        fun newInstance(): EditProfileFragment = EditProfileFragment()
    }
}