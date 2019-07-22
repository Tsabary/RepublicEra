package com.republicera.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.interfaces.BoardMethods
import com.republicera.models.Answer
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.viewModels.AnswerImagesViewModel
import com.republicera.viewModels.AnswerViewModel
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_answer.*

class EditAnswerFragment : Fragment(), BoardMethods {

    lateinit var db : DocumentReference


    private lateinit var sharedViewModelAnswerImages: AnswerImagesViewModel
    private lateinit var sharedViewModelAnswer: AnswerViewModel
    private lateinit var currentCommunity: Community

    lateinit var answer: Answer
    lateinit var currentUser: CommunityProfile
    private var imagesRecyclerAdapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_answer, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject
            sharedViewModelAnswerImages = ViewModelProviders.of(it).get(AnswerImagesViewModel::class.java)
            sharedViewModelAnswer = ViewModelProviders.of(it).get(AnswerViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { community ->
                    currentCommunity = community
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        val addImage = answer_add_photos_button
        val updateAnswer = answer_btn

        val imagesRecycler = answer_photos_recycler
        val imagesRecyclerLayoutManager = GridLayoutManager(this.context, 3)

        imagesRecycler.adapter = imagesRecyclerAdapter
        imagesRecycler.layoutManager = imagesRecyclerLayoutManager

        val content = answer_content

        sharedViewModelAnswerImages.imageList.observe(this, Observer {
            it?.let { existingImageList ->

                val imageListFinal = mutableListOf<String>()

                imagesRecyclerAdapter.clear()

                imagesRecycler.visibility = if (existingImageList.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
/*
                for (image in existingImageList) {

                    db.collection("images").document(image).get().addOnSuccessListener {
                        val imageObject = it.toObject(Image::class.java)
                        if (imageObject != null) {
                            imagesRecyclerAdapter.add(CollectionPhoto(imageObject, activity, "answer", 0))
                            imageListFinal.add(imageObject.id)
                        }
                    }
                }
*/

                updateAnswer.setOnClickListener {

                    if (answer_content.text.length > 15) {

                        val updatedAnswer = Answer(
                            answer.answer_ID,
                            answer.question_ID,
                            content.text.toString(),
                            answer.timestamp,
                            answer.author_ID,
                            imageListFinal,
                            answer.score_items
                        )

                        db.collection("answers").document(answer.answer_ID).set(
                            mapOf("content" to content.text.toString(), "photos" to imageListFinal),
                            SetOptions.merge()
                        ).addOnSuccessListener {
                            //                            activity.isEditAnswerActive = false

                            activity.subFm.beginTransaction().add(
                                R.id.feed_subcontents_frame_container,
                                activity.openedQuestionFragment,
                                "openedQuestionFragment"
                            ).addToBackStack("openedQuestionFragment").commit()
                            activity.subActive = activity.openedQuestionFragment
                        }
                    } else {
                        Toast.makeText(this.context, "Your answer is too short, please elaborate", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })


        sharedViewModelAnswer.sharedAnswerObject.observe(this, Observer { observedQuestion ->
            observedQuestion?.let { answerObject ->

                answer = answerObject

                val answerImagesList = mutableListOf<String>()

                sharedViewModelAnswerImages.imageList.postValue(mutableListOf())

/*
                answerObject.photos.forEach {


                    db.collection("images").document(it).get().addOnSuccessListener { documentSnapshot ->
                        val imageObject = documentSnapshot.toObject(Image::class.java)
                        if (imageObject != null) {
                            answerImagesList.add(imageObject.id)
                            sharedViewModelAnswerImages.imageList.postValue(answerImagesList)
                        }
                    }
                }

                */
                content.setText(answerObject.content)
            }
        })

        /*

        addImage.setOnClickListener {
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.addImageToAnswer, "addImageToAnswer")
                .addToBackStack("addImageToAnswer").commit()
            activity.subActive = activity.addImageToAnswer
        }
        */
    }
}
