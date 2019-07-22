package com.republicera.fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleTagSuggestion
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.SingleTagForList
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.InterestsViewModel
import com.republicera.viewModels.TagsViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_edit_interests.*

class EditInterestsFragment : Fragment() {
    lateinit var db : DocumentReference

    lateinit var currentUser: CommunityProfile
    lateinit var sharedViewModelTags: TagsViewModel
    lateinit var sharedViewModelInterests: InterestsViewModel

    private lateinit var currentCommunity: Community

    var interestsList: MutableList<String> = mutableListOf()

    lateinit var chipGroup: ChipGroup
    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    var tagsInChipGroup = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_edit_interests, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        chipGroup = edit_interests_chipgroup

        activity.let {
            sharedViewModelTags = ViewModelProviders.of(it).get(TagsViewModel::class.java)
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject
            sharedViewModelInterests = ViewModelProviders.of(it).get(InterestsViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })

            sharedViewModelInterests.interestList.observe(activity, Observer { currentInterestsList ->
                interestsList = currentInterestsList
                tagsInChipGroup.clear()
                chipGroup.removeAllViews()
            })
        }

        val searchInput = edit_interests_search_input
        val tagSuggestionRecycler = edit_interests_recycler
        tagSuggestionRecycler.adapter = tagsFilteredAdapter
        tagSuggestionRecycler.layoutManager = LinearLayoutManager(this.context)




        searchInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()
                val userInput = s.toString().toLowerCase().replace(" ", "-")
                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE
                } else {
                    tagSuggestionRecycler.visibility = View.VISIBLE

                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {
                        var countTagMatches = 0
                        for (i in 0 until chipGroup.childCount) {
                            val chip = chipGroup.getChildAt(i) as Chip

                            if (t.tagString == chip.text.toString()) {
                                countTagMatches += 1
                            }
                        }

                        if (countTagMatches == 0) {
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

        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val singleTag = item as SingleTagSuggestion
            tagsInChipGroup.add(singleTag.tag.tagString)

            onTagSelected(singleTag.tag.tagString)

            interestsList.add(singleTag.tag.tagString)

            searchInput.text.clear()
            tagSuggestionRecycler.visibility = View.GONE

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("interests_edited", null)
        }

    }

    override fun onResume() {
        super.onResume()
        populateChipGroup()
    }

    override fun onPause() {
        super.onPause()

        db.collection("interests").document(currentUser.uid).set(mapOf("interests_list" to interestsList))

    }

    private fun populateChipGroup() {
        for (interest in interestsList) {
            tagsInChipGroup.add(interest)
        }
        tagsInChipGroup.sortBy { it }
        for (tag in tagsInChipGroup) {
            onTagSelected(tag)
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
        chipGroup.addView(chip)
        chipGroup.visibility = View.VISIBLE
        chip.setOnCloseIconClickListener {
            tagsInChipGroup.remove(chip.text!!)
            chipGroup.removeView(it)
            interestsList.remove(selectedTag)

            val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
            firebaseAnalytics.logEvent("interests_edited", null)
        }
    }


}

