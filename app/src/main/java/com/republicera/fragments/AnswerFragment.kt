package com.republicera.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.interfaces.BoardMethods
import com.republicera.models.Answer
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Question
import com.republicera.viewModels.AnswerImagesViewModel
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.QuestionViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_answer.*
import java.util.*

class AnswerFragment : Fragment(), BoardMethods {

    lateinit var db: DocumentReference

    private lateinit var sharedViewModelAnswerImages: AnswerImagesViewModel

    private lateinit var currentCommunity: Community

    lateinit var question: Question
    lateinit var currentUser: CommunityProfile

    private var imagesRecyclerAdapter = GroupAdapter<ViewHolder>()
    private var imageListFinal = mutableListOf<String>()
    private lateinit var answerContent: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_answer, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionTitle = answer_question_title
        val answerButton = answer_btn
        answerButton.text = getString(R.string.answer)

        val activity = activity as MainActivity

        activity.let {
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })

            sharedViewModelAnswerImages = ViewModelProviders.of(it).get(AnswerImagesViewModel::class.java)


            ViewModelProviders.of(it).get(QuestionViewModel::class.java).questionObject.observe(
                this, Observer { observedQuestion ->
                    observedQuestion?.let { questionObject ->
                        question = questionObject
                        questionTitle.text = question.title
                        sharedViewModelAnswerImages.imageList.postValue(mutableListOf())
                        answerContent.text.clear()
                    }
                })
        }



        answerContent = answer_content

        answerButton.setOnClickListener {

            if (answer_content.text.length > 15) {
                postAnswer(
                    answerContent.text.toString(),
                    Date(),
                    activity
                )
            } else {
                Toast.makeText(this.context, "Your answer is too short, please elaborate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun postAnswer(content: String, timestamp: Date, activity: MainActivity) {


        val answerDoc = db.collection("answers").document()

        val newAnswer =
            Answer(
                answerDoc.id,
                question.id,
                content,
                timestamp,
                currentUser.uid,
                currentUser.name,
                currentUser.image,
                imageListFinal,
                mapOf(),
                0
            )

        answerDoc.set(newAnswer).addOnSuccessListener {

            if (currentUser.uid != question.author_ID) {
                changeReputation(
                    8,
                    answerDoc.id,
                    question.id,
                    currentUser.uid,
                    currentUser.name,
                    currentUser.image,
                    currentUser.uid,
                    "answer",
                    activity,
                    currentCommunity.id
                )
            }

            activity.openedQuestionFragment.listenToAnswers()
            activity.subActive = activity.openedQuestionFragment
            closeKeyboard(activity)
            answerContent.text.clear()

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("question_answer_added", null)
            activity.subFm.popBackStack("answerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
}
