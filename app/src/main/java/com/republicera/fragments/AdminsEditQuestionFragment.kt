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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.FirebaseDatabase
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

class AdminsEditQuestionFragment : Fragment(), BoardMethods {

    lateinit var db: DocumentReference

    lateinit var sharedViewModelTags: TagsViewModel
    private lateinit var sharedViewModelQuestion: QuestionViewModel
    private lateinit var sharedViewModelInterests: InterestsViewModel
    lateinit var currentUser: CommunityProfile

    private lateinit var currentCommunity: Community

    private var interestsList: MutableList<String> = mutableListOf()

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    lateinit var questionChipGroup: ChipGroup
    private var tagsList: MutableList<String> = mutableListOf()
    private lateinit var questionDetails: EditText
    private lateinit var questionTitle: EditText
    private lateinit var questionTagsInput: EditText

    private lateinit var languageCode: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_new_question, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelTags = ViewModelProviders.of(it).get(TagsViewModel::class.java)
            sharedViewModelQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject
            sharedViewModelInterests = ViewModelProviders.of(it).get(InterestsViewModel::class.java)
            sharedViewModelInterests.interestList.observe(activity, Observer { currentInterestsList ->
                interestsList = currentInterestsList
            })

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }

        questionTitle = new_question_title
        questionTitle.requestFocus()
        questionDetails = new_question_details
        val saveButton = new_question_btn
        saveButton.text = getString(R.string.save_question)
        questionTagsInput = new_question_tag_input
        val addTagButton = new_question_add_tag_button
        questionChipGroup = new_question_chip_group
        val questionLanguage = new_question_language_input


        sharedViewModelQuestion.questionObject.observe(this, Observer {
            it?.let { question ->
                questionChipGroup.removeAllViews()
                questionTitle.setText(question.title)
                questionTitle.setSelection(question.title.length)
                questionDetails.setText(question.details)
                languageCode = question.language
                question.tags.forEach { tag ->
                    addTag(tag)
                }

                saveButton.setOnClickListener {
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
                            "Please add at least one tag to your question",
                            Toast.LENGTH_SHORT
                        ).show()
                        else -> {

                            tagsList.clear()
                            for (i in 0 until questionChipGroup.childCount) {
                                val chip = questionChipGroup.getChildAt(i) as Chip
                                tagsList.add(chip.text.toString())
                            }


                            db.collection("admins_questions").document(question.id).update(
                                mapOf(
                                    "title" to questionTitle.text.toString(),
                                    "details" to questionDetails.text.toString(),
                                    "tags" to tagsList,
                                    "last_interaction" to FieldValue.serverTimestamp()
                                )
                            ).addOnSuccessListener {

//                                for (tag in question.tags) {
//
//                                    val tagRef = db.collection("tags")
//                                        .document(tag[0] + tag[1].toString())
//
//                                    if (!tagsList.contains(tag)) {
//                                        tagRef.update(tag, FieldValue.increment(-1))
//                                    }
//                                }

//                                for (tag in tagsList) {
//
//                                    if (!question.tags.contains(tag)) {
//                                        db.collection("tags")
//                                            .document(tag[0] + tag[1].toString()).update(
//                                                tag, FieldValue.increment(1)
//                                            ).addOnSuccessListener {
//                                                db.collection("interests")
//                                                    .document(currentUser.uid)
//                                                    .update("interests_list", FieldValue.arrayUnion(tag)).addOnSuccessListener {
//                                                        interestsList.add(tag)
//                                                        sharedViewModelInterests.interestList.postValue(interestsList)
//                                                    }
//                                            }
//                                    }
//                                }


                                for (tag in tagsList) {
                                    interestsList.add(tag)
                                    sharedViewModelInterests.interestList.postValue(interestsList)
                                }

                                val updatedQuestion = Question(
                                    question.id,
                                    questionTitle.text.toString(),
                                    questionDetails.text.toString(),
                                    languageCode,
                                    tagsList,
                                    question.timestamp,
                                    question.author_ID,
                                    question.author_name,
                                    question.author_image,
                                    question.answers,
                                    question.last_interaction,
                                    question.score_items
                                )

                                sharedViewModelQuestion.questionObject.postValue(updatedQuestion)

//                                questionChipGroup.removeAllViews()
                                questionDetails.text.clear()
                                questionTitle.text.clear()

                                activity.subFm.popBackStack(
                                    "adminsEditQuestionFragment",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                )
                                activity.subActive = activity.openedQuestionFragment
                            }
                        }
                    }
                }
            }
        })

        val languageIdentifier = FirebaseNaturalLanguage.getInstance().languageIdentification


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


        val tagSuggestionRecycler = new_question_tag_recycler
        tagSuggestionRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        tagSuggestionRecycler.adapter = tagsFilteredAdapter

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

                        val tagsInGroup = mutableListOf<String>()
                        for (i in 0 until questionChipGroup.childCount) {
                            val chip = questionChipGroup.getChildAt(i) as Chip
                            tagsInGroup.add(chip.text.toString())
                        }

                        if (!tagsInGroup.contains(t.tagString)) {
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
            if (questionTagsInput.text.isNotEmpty()) {
                addTag(questionTagsInput.text.toString().toLowerCase().trimEnd().replace(" ", "-"))
            }
        }

        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            addTag(row.tag.tagString)
        }
    }

    private fun addTag(tag: String) {

        val tagsInGroup = mutableListOf<String>()

        for (i in 0 until questionChipGroup.childCount) {
            val chip = questionChipGroup.getChildAt(i) as Chip
            tagsInGroup.add(chip.text.toString())
        }

        if (!tagsInGroup.contains(tag)) {
            if (questionChipGroup.childCount < 5) {
                onTagSelected(tag.toLowerCase())
                questionTagsInput.text.clear()
            } else {
                Toast.makeText(this.context, "Maximum 5 tags", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this.context, "Tag $tag had already been added", Toast.LENGTH_LONG).show()
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
        questionChipGroup.addView(chip)
        questionChipGroup.visibility = View.VISIBLE
        chip.setOnCloseIconClickListener {
            questionChipGroup.removeView(it)
        }
    }

    companion object {
        fun newInstance(): AdminsEditQuestionFragment = AdminsEditQuestionFragment()
    }
}
