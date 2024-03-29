package com.republicera.fragments


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.republicera.MainActivity

import com.republicera.R
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.Community
import com.republicera.models.User
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.CurrentUserViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_new_community.*
import org.json.JSONObject


class NewCommunityFragment : Fragment(), GeneralMethods {

    private lateinit var communityViewModel: CurrentCommunityViewModel

    lateinit var titleInput: EditText
    lateinit var descriptionInput: EditText
    lateinit var publicButton: RadioButton
    lateinit var privateButton: RadioButton
    lateinit var startButton: TextView

    var topLevelUser: User? = null
    lateinit var communityImage: CircleImageView
    private var selectedPhotoUri: Uri? = null

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

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

        communityImage = new_community_image
        titleInput = new_community_title_input
        descriptionInput = new_community_description_input
        publicButton = new_community_public
        privateButton = new_community_private
        startButton = new_community_start_button

        communityImage.setOnClickListener {

            if (hasNoPermissions()) {
                requestPermission()
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "image/*"
                startActivityForResult(intent, 30)
            }
        }

        startButton.setOnClickListener {

            if (topLevelUser != null) {

                if (titleInput.text.length in 2..35 && descriptionInput.text.length in 2..150) {
                    uploadImageToFirebase()
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

            val communityDoc = FirebaseFirestore.getInstance().collection("communities").document()

            val ref = FirebaseStorage.getInstance().getReference("/images/community_pics/${communityDoc.id}")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { newImageUri ->
                        createCommunity(communityDoc, newImageUri.toString())
                    }
                }
        }
    }

    private fun createCommunity(communityDoc: DocumentReference, image: String) {
        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()

        val newCommunity =
            Community(
                communityDoc.id,
                title,
                description,
                image,
                0,
                0,
                publicButton.isChecked,
                topLevelUser!!.uid,
                mutableListOf(topLevelUser!!.uid)
            )
        communityDoc.set(newCommunity).addOnSuccessListener {
            Toast.makeText(this.context, "New community created", Toast.LENGTH_SHORT).show()

            communityViewModel.currentCommunity.postValue(newCommunity)
            (activity as MainActivity).userHomeFrame.visibility = View.GONE
            titleInput.text.clear()
            descriptionInput.text.clear()
            publicButton.isChecked = true
            privateButton.isChecked = false
        }.addOnFailureListener {
            Toast.makeText(this.context, "Failed to create new community " + it.localizedMessage, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 30 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            if (selectedPhotoUri != null) {
                Glide.with(this.context!!).load(selectedPhotoUri.toString()).into(communityImage)
            }
        }
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
}
