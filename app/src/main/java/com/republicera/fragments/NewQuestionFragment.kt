package com.republicera.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.algolia.search.saas.Client
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleTagSuggestion
import com.republicera.interfaces.BoardMethods
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Question
import com.republicera.models.SingleTagForList
import com.republicera.viewModels.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_new_question.*
import org.json.JSONObject
import java.util.*


class NewQuestionFragment : Fragment(), BoardMethods {

    lateinit var db: DocumentReference

    lateinit var sharedViewModelTags: TagsViewModel
    private lateinit var sharedViewModelQuestion: QuestionViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel
    lateinit var sharedViewModelInterests: InterestsViewModel
    lateinit var currentUser: CommunityProfile

    private lateinit var currentCommunity: Community

    lateinit var index: com.algolia.search.saas.Index

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    lateinit var questionChipGroup: ChipGroup
    private var tagsList: MutableList<String> = mutableListOf()
    private lateinit var questionDetails: EditText
    lateinit var questionTitle: EditText

    var interestsList: MutableList<String> = mutableListOf()

    lateinit var languageCode: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_new_question, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val activity = activity as MainActivity

        activity.let {
            sharedViewModelTags = ViewModelProviders.of(it).get(TagsViewModel::class.java)
            sharedViewModelQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            sharedViewModelInterests = ViewModelProviders.of(it).get(InterestsViewModel::class.java)
            sharedViewModelInterests.interestList.observe(activity, Observer { currentInterestsList ->
                interestsList = currentInterestsList
            })
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                    new_question_community_name.text = currentCommunity.title
                })
        }

        val languageIdentifier = FirebaseNaturalLanguage.getInstance().languageIdentification

        val applicationID = activity.getString(R.string.algolia_application_id)
        val apiKey = activity.getString(R.string.algolia_api_key)

        val client = Client(applicationID, apiKey)
        index = client.getIndex("${currentCommunity.id}_questions")

        questionTitle = new_question_title
        questionTitle.requestFocus()
        questionDetails = new_question_details
        val postQuestionButton = new_question_btn
        val questionTagsInput = new_question_tag_input
        val addTagButton = new_question_add_tag_button
        questionChipGroup = new_question_chip_group
        val questionLanguage = new_question_language_input

        postQuestionButton.setOnClickListener {

            when {
                questionTitle.text!!.length < 15 -> Toast.makeText(
                    this.context,
                    "Question title is too short",
                    Toast.LENGTH_SHORT
                ).show()
                questionDetails.text.isEmpty() -> Toast.makeText(
                    this.context,
                    "Please give your question some more details",
                    Toast.LENGTH_SHORT
                ).show()
                questionChipGroup.childCount == 0 -> Toast.makeText(
                    this.context,
                    "Please add at least one tag_unactive to your question",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    tagsList.clear()

                    for (i in 0 until questionChipGroup.childCount) {
                        val chip = questionChipGroup.getChildAt(i) as Chip
                        tagsList.add(chip.text.toString())
                    }

                    postQuestion(
                        questionTitle.text.toString(),
                        questionDetails.text.toString(),
                        tagsList,
                        Date(System.currentTimeMillis()),
                        currentUser
                    )
                }
            }
        }

        val tagSuggestionRecycler = new_question_tag_recycler
        tagSuggestionRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.adapter = tagsFilteredAdapter

        questionDetails.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                languageIdentifier.identifyLanguage(s.toString()).addOnSuccessListener {
                    questionLanguage.text = setQuestionLanguage(it)
                    languageCode = it
                }
            }

        })

        questionTagsInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()

                val userInput = s.toString().toLowerCase()

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE

                } else {
                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {

                        var countTagMatches = 0
                        for (i in 0 until questionChipGroup.childCount) {
                            val chip = questionChipGroup.getChildAt(i) as Chip

                            if (t.tagString == chip.text.toString()) {
                                countTagMatches += 1
                            }
                        }

                        if (countTagMatches == 0) {
                            tagSuggestionRecycler.visibility = View.VISIBLE
                            tagsFilteredAdapter.add(SingleTagSuggestion(t))
                        }
                    }
                }
            }


            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })

        addTagButton.setOnClickListener {

            if (questionTagsInput.text.length > 1) {

                var tagsMatchCount = 0

                for (i in 0 until questionChipGroup.childCount) {
                    val chip = questionChipGroup.getChildAt(i) as Chip
                    if (chip.text.toString() == questionTagsInput.text.toString()) {
                        tagsMatchCount += 1
                    }
                }

                if (tagsMatchCount == 0) {
                    if (questionChipGroup.childCount < 5) {
                        onTagSelected(questionTagsInput.text.toString().toLowerCase().trimEnd().replace(" ", "-"))
                        questionTagsInput.text.clear()
                    } else {
                        Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this.context, "Tag had already been added", Toast.LENGTH_LONG).show()
                }
            }
        }


        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            if (questionChipGroup.childCount < 5) {
                onTagSelected(row.tag.tagString)
                questionTagsInput.text.clear()
            } else {
                Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onTagSelected(selectedTag: String) {

        val chip = Chip(this.context)
        chip.text = selectedTag
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.isClickable = false
        chip.setChipBackgroundColorResource(R.color.white)
        chip.chipStrokeWidth = 1f
        chip.setChipStrokeColorResource(R.color.gray500)
        chip.setCloseIconTintResource(R.color.gray500)
        chip.setTextAppearance(R.style.ChipSelectedStyle)
        chip.setOnCloseIconClickListener {
            questionChipGroup.removeView(it)
        }

        questionChipGroup.addView(chip)
        questionChipGroup.visibility = View.VISIBLE
    }


    private fun postQuestion(
        title: String,
        details: String,
        tags: MutableList<String>,
        timestamp: Date,
        currentUser: CommunityProfile
    ) {

        val activity = activity as MainActivity

        val batch = FirebaseFirestore.getInstance().batch()

        val questionDocRef = db.collection("questions").document()
        val newQuestion = Question(
            questionDocRef.id,
            title,
            details,
            languageCode,
            tags,
            timestamp,
            currentUser.uid,
            currentUser.name,
            currentUser.image,
            0,
            timestamp,
            mapOf()
        )
        batch.set(questionDocRef, newQuestion)

//        val userLanguagesRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
//        batch.update(userLanguagesRef, "lang_list", FieldValue.arrayUnion(languageCode))

        for (tag in tags) {
//
//            val tagRef = db.collection("tags").document(tag[0] + tag[1].toString())
//            batch.set(tagRef, mapOf(tag to FieldValue.increment(1)), SetOptions.merge())
//
//            val usersInterestsRef = db.collection("interests").document(currentUser.uid)
//            batch.update(usersInterestsRef, "interests_list", FieldValue.arrayUnion(tag))

            interestsList.add(tag)
        }

        batch.commit().addOnSuccessListener {

            sharedViewModelInterests.interestList.postValue(interestsList)
            sharedViewModelQuestion.questionObject.postValue(newQuestion)
            sharedViewModelRandomUser.randomUserObject.postValue(currentUser)


            activity.subFm.popBackStack("searchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            activity.subFm.beginTransaction().add(
                R.id.feed_subcontents_frame_container,
                activity.openedQuestionFragment,
                "openedQuestionFragment"
            ).addToBackStack("openedQuestionFragment")
                .commit()
            activity.subActive = activity.openedQuestionFragment
            activity.boardFragment.listenToQuestions()
            activity.fm.beginTransaction().show(activity.boardFragment).commit()

            closeKeyboard(activity)

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("question_added", null)

            questionTitle.text.clear()
            questionDetails.text.clear()
            questionChipGroup.removeAllViews()
        }
    }


    companion object {
        fun newInstance(): NewQuestionFragment = NewQuestionFragment()
    }

}


