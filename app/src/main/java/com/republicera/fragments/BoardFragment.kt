package com.republicera.fragments


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.iconics.IconicsColor.Companion.colorRes
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize.Companion.dp
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.RegisterLoginActivity
import com.republicera.groupieAdapters.SingleBoardBlock
import com.republicera.groupieAdapters.SingleBoardRow
import com.republicera.groupieAdapters.SingleLanguageOptionBoard
import com.republicera.groupieAdapters.SingleTagSuggestion
import com.republicera.interfaces.BoardMethods
import com.republicera.models.*
import com.republicera.viewModels.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.toolbar_board.*
import kotlinx.android.synthetic.main.fragment_board.*
import kotlinx.android.synthetic.main.toolbar.*

class BoardFragment : Fragment(), BoardMethods {

    lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var db: DocumentReference
    lateinit var currentCommunity: Community
    lateinit var currentLanguage: String
    private var currentLayout = 0
    private lateinit var sharedPref: SharedPreferences

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    private val searchedQuestionsRecyclerAdapter = GroupAdapter<ViewHolder>()
    val questionsRowLayoutAdapter = GroupAdapter<ViewHolder>()
    val questionsBlockLayoutAdapter = GroupAdapter<ViewHolder>()
    private val languagePickerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var questionRecyclerLayoutManager: LinearLayoutManager

    lateinit var communityProfile: CommunityProfile
    lateinit var topLevelUser: User
    lateinit var sharedViewModelForQuestion: QuestionViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel
    lateinit var sharedViewModelInterests: InterestsViewModel
    lateinit var sharedViewModelTags: TagsViewModel


    lateinit var questionSwipeRefresh: SwipeRefreshLayout
    lateinit var searchSwipeRefresh: SwipeRefreshLayout

    lateinit var questionsRecycler: RecyclerView
    private lateinit var searchedQuestionsRecycler: RecyclerView
//    lateinit var layoutIcon: ImageButton

    var interestsList: List<String> = listOf()

    lateinit var freshMessage: TextView
    lateinit var freshMessage2: TextView

    lateinit var boardLastVisible: DocumentSnapshot
    var boardIsScrolling = false
    var boardIsLastItemReached = false

    lateinit var searchLastVisible: DocumentSnapshot
    var searchIsScrolling = false
    var searchIsLastItemReached = false

    private lateinit var boardFilterChipGroup: ChipGroup
    private var searchedTagsList = mutableListOf<String>()

    lateinit var result: Drawer
    private lateinit var languageItems: MutableList<ISubItem<*>>

    lateinit var boardSearchBox: EditText
    lateinit var searchConstraint: ConstraintLayout

//    lateinit var blockLayout: SecondaryDrawerItem
//    lateinit var rowsLayout: SecondaryDrawerItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_board, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
        val activity = activity as MainActivity

//        val title = board_community_title
        searchConstraint = toolbar_board_container

        searchedQuestionsRecycler = board_search_question_feed
        val searchedQuestionRecyclerLayoutManager = LinearLayoutManager(this.context)
        searchedQuestionsRecycler.adapter = searchedQuestionsRecyclerAdapter
        searchedQuestionsRecycler.layoutManager = searchedQuestionRecyclerLayoutManager

        questionsRecycler = board_question_feed
        questionsRecycler.adapter = questionsRowLayoutAdapter
        questionRecyclerLayoutManager = LinearLayoutManager(this.context)
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        val languagePickerRecycler = board_languages_recycler
        languagePickerRecycler.adapter = languagePickerAdapter
        languagePickerRecycler.layoutManager = LinearLayoutManager(this.context!!)

        questionSwipeRefresh = board_questions_swipe_refresh
        searchSwipeRefresh = board_search_swipe_refresh

        val reportedSwipeRefresh = board_reported_swipe_refresh


//        layoutIcon = board_layout_icon
//        val spinner = board_language_spinner
//        spinner.setOnClickListener {
//            if (languagePickerRecycler.visibility == View.VISIBLE
//            ) {
//                languagePickerRecycler.visibility = View.GONE
//            } else {
//                languagePickerRecycler.visibility = View.VISIBLE
//            }
//        }

        freshMessage = board_fresh_message
        freshMessage2 = board_fresh_message2

        sharedPref = activity.getSharedPreferences(activity.getString(R.string.package_name), Context.MODE_PRIVATE)
        currentLanguage = sharedPref.getString("last_language", "en")!!
        currentLayout = sharedPref.getInt("last_layout", 0)

//        if (currentLayout == 0) {
//            layoutIcon.tag = "row_layout"
//        } else {
//            layoutIcon.tag = "board_layout"
//        }

        setLayout(currentLayout)

//        layoutIcon.setOnClickListener {
//            if (layoutIcon.tag == "row_layout") {
//                setLayout(1)
//            } else {
//                setLayout(0)
//            }
//        }

        languageItems = mutableListOf<ISubItem<*>>()

        activity.let {
            ViewModelProviders.of(it).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity,
                Observer { user ->
                    topLevelUser = user

//                    var lanIdentifier: Long = 100
//
//                    for (language in topLevelUser.lang_list) {
////                        languagePickerAdapter.add(SingleLanguageOptionBoard(languageCodeToName(language)))
//
//                        languageItems.add(
//                            SecondaryDrawerItem().withIdentifier(lanIdentifier).withName(
//                                languageCodeToName(language)
//                            ).withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
//                                override fun onItemClick(
//                                    view: View?,
//                                    position: Int,
//                                    drawerItem: IDrawerItem<*>
//                                ): Boolean {
//                                    currentLanguage = language
//                                    val editor = sharedPref.edit()
//                                    editor.putString("last_language", currentLanguage)
//                                    editor.apply()
//                                    listenToQuestions()
//
//                                    return true
//                                }
//                            })
//                        )
//                        lanIdentifier++
//                    }
//                    spinner.setItems(shortenedList)
//                    if (shortenedList.contains(languageCodeToName(currentLanguage))) {
//                        spinner.text = languageCodeToName(currentLanguage)
//                    }
                })

            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            sharedViewModelInterests = ViewModelProviders.of(it).get(InterestsViewModel::class.java)
            sharedViewModelInterests.interestList.observe(activity, Observer { currentInterestsList ->
                interestsList = currentInterestsList
            })

            sharedViewModelTags = ViewModelProviders.of(it).get(TagsViewModel::class.java)
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
//                    title.text = currentCommunity.title
                    listenToQuestions(currentLanguage)
                })

            communityProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject
        }





        setUpDrawerNav(activity, topLevelUser, communityProfile, 0)

        val menuIcon = toolbar_board_menu
        menuIcon.setOnClickListener {
            result.openDrawer()
        }

//        languagePickerAdapter.setOnItemClickListener { item, _ ->
//            val language = item as SingleLanguageOptionBoard
//            currentLanguage = languageNameToCode(language.language)
//            val editor = sharedPref.edit()
//            editor.putString("last_language", currentLanguage)
//            editor.apply()
//            listenToQuestions()
//            languagePickerRecycler.visibility = View.GONE
////            spinner.text = currentLanguage
//        }


        recyclersVisibility(0)


//        scrollView = board_questions_scroll_view

        boardSearchBox = toolbar_board_search_box
        val tagSuggestionRecycler = board_search_recycler
        boardFilterChipGroup = toolbar_board_filter_chipgroup
//        val fab: FloatingActionButton = board_fab

        tagSuggestionRecycler.layoutManager = LinearLayoutManager(this.context)
        tagSuggestionRecycler.adapter = tagsFilteredAdapter

        reportedSwipeRefresh.visibility = View.GONE
        searchSwipeRefresh.visibility = View.GONE

        questionSwipeRefresh.setOnRefreshListener {
            listenToQuestions(currentLanguage)
            questionSwipeRefresh.isRefreshing = false
            questionSwipeRefresh.isEnabled = false
        }
        questionSwipeRefresh.isEnabled = false

        searchSwipeRefresh.setOnRefreshListener {
            searchQuestions(1)
            searchSwipeRefresh.isRefreshing = false
            searchSwipeRefresh.isEnabled = false
        }
        searchSwipeRefresh.isEnabled = false

//
//        val boardNotificationIcon = toolbar_with_search_notifications_icon
//        val boardSavedQuestionIcon = toolbar_menu
//
//        notificationBadge.setOnClickListener {
//            goToNotifications(activity)
//        }
//
//        boardNotificationIcon.setOnClickListener {
//            goToNotifications(activity)
//        }
//
//        boardSavedQuestionIcon.setOnClickListener {
//            goToSavedQuestions(activity)
//        }

        val constraintButton = board_new_question_container

        constraintButton.setOnClickListener {

            //            activity.userFm.popBackStack("chooseCommunityFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.searchFragment, "searchFragment")
                .addToBackStack("searchFragment").commit()
            activity.subActive = activity.searchFragment
            activity.switchVisibility(1)
        }

        boardSearchBox.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()

                val userInput = s.toString().toLowerCase().replace(" ", "-")

                if (userInput == "") {
                    board_search_recycler.visibility = View.GONE
                } else {
                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {
                        board_search_recycler.visibility = View.VISIBLE
                        tagsFilteredAdapter.add(SingleTagSuggestion(t))
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

        })

        tagsFilteredAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleTagSuggestion
            onTagSelected(row.tag.tagString)
            searchedTagsList.add(row.tag.tagString)
            boardSearchBox.text.clear()
            boardSearchBox.visibility = View.GONE
            recyclersVisibility(1)
            searchQuestions(0)
            boardFilterChipGroup.visibility = View.VISIBLE
            searchConstraint.setBackgroundColor(activity.resources.getColor(R.color.transparent))
        }

        questionsBlockLayoutAdapter.setOnItemClickListener { item, _ ->
            val block = item as SingleBoardBlock
            val author = block.question.author_ID
            val question = block.question

            sharedViewModelForQuestion.questionObject.postValue(question)

            openQuestion(author, activity)
        }


        questionsRowLayoutAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleBoardRow
            val author = row.question.author_ID
            val question = row.question

            sharedViewModelForQuestion.questionObject.postValue(question)

            openQuestion(author, activity)
        }



        searchedQuestionsRecyclerAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleBoardRow
            val author = row.question.author_ID
            val question = row.question

            sharedViewModelForQuestion.questionObject.postValue(question)

            openQuestion(author, activity)
        }
    }


    private fun setLayout(case: Int) {
        val editor = sharedPref.edit()

        if (case == 0) {
//            layoutIcon.setImageResource(R.drawable.blocks_layout)
//            layoutIcon.tag = "row_layout"
            firebaseAnalytics.logEvent("board_row_layout", null)
            val position = questionRecyclerLayoutManager.findFirstCompletelyVisibleItemPosition()
            questionsRecycler.adapter = questionsRowLayoutAdapter

            editor.putInt("last_layout", 0)
        } else {
//            layoutIcon.setImageResource(R.drawable.rows_layout)
//            layoutIcon.tag = "block_layout"
            firebaseAnalytics.logEvent("board_block_layout", null)
            val position = questionRecyclerLayoutManager.findFirstCompletelyVisibleItemPosition()
            questionsRecycler.adapter = questionsBlockLayoutAdapter
            editor.putInt("last_layout", 1)
        }

        editor.apply()
    }


    private fun openQuestion(author: String, activity: MainActivity) {

        activity.subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, activity.openedQuestionFragment, "openedQuestionFragment")
            .addToBackStack("openedQuestionFragment").commit()

        db.collection("profiles").document(author).get().addOnSuccessListener {
            val user = it.toObject(CommunityProfile::class.java)

            if (user != null) {
                sharedViewModelRandomUser.randomUserObject.postValue(user)
                activity.subActive = activity.openedQuestionFragment
                activity.isOpenedQuestionActive = true
                activity.switchVisibility(1)
            } else {
                Toast.makeText(this.context, "user is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recyclersVisibility(case: Int) {
        if (case == 0) {
            searchSwipeRefresh.visibility = View.GONE
            questionSwipeRefresh.visibility = View.VISIBLE
        } else {
            searchSwipeRefresh.visibility = View.VISIBLE
            questionSwipeRefresh.visibility = View.GONE
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
            boardFilterChipGroup.removeView(it)
            searchedTagsList.remove(selectedTag)
            searchQuestions(1)
            boardFilterChipGroup.visibility = View.GONE
            boardSearchBox.visibility = View.VISIBLE
//            searchConstraint.setBackgroundResource(R.drawable.button_curve_8_gray)

        }

        boardFilterChipGroup.addView(chip)
        boardFilterChipGroup.visibility = View.VISIBLE
    }


    private fun searchQuestions(case: Int) {
        when (boardFilterChipGroup.childCount) {

            0 -> {
                recyclersVisibility(0)
            }

            1 -> {

//                index.search(Query("query").setFilters(searchedTagsList[0]), RequestOptions())


                searchQuestionsOneTag(
                    searchedTagsList[0]
                )
                if (case == 0) {
                    val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                    firebaseAnalytics.logEvent("search_board", null)
                }
            }
//            2 -> {
//                searchQuestionsTwoTags(
//                    searchedTagsList[0],
//                    searchedTagsList[1]
//                )
//            }
//            3 -> {
//                searchQuestionsThreeTags(
//                    searchedTagsList[0],
//                    searchedTagsList[1],
//                    searchedTagsList[2]
//                )
//            }
//            4 -> {
//                searchQuestionsFourTags(
//                    searchedTagsList[0],
//                    searchedTagsList[1],
//                    searchedTagsList[2],
//                    searchedTagsList[3]
//                )
//            }
//            5 -> {
//                searchQuestionsFiveTags(
//                    searchedTagsList[0],
//                    searchedTagsList[1],
//                    searchedTagsList[2],
//                    searchedTagsList[3],
//                    searchedTagsList[4]
//                )
//            }

            else -> {
                Toast.makeText(this.context, "You can only filter by one tag at a time", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    private fun searchQuestionsOneTag(
        searchTagOne: String
    ) {
        searchedQuestionsRecyclerAdapter.clear()

        db.collection("questions").whereArrayContains("tags", searchTagOne)
            .orderBy("last_interaction", Query.Direction.DESCENDING).limit(25)
            .get().addOnSuccessListener {
                for (question in it) {
                    searchedQuestionsRecyclerAdapter.add(
                        SingleBoardRow(
                            question.toObject(Question::class.java),
                            activity as MainActivity
                        )
                    )
                }

                searchedQuestionsRecyclerAdapter.notifyDataSetChanged()

                searchLastVisible = it.documents[it.size() - 1]
            }

        searchedQuestionsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    searchIsScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val thisLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                val firstVisibleItem = thisLayoutManager.findFirstCompletelyVisibleItemPosition()
                val visibleItemCount = thisLayoutManager.childCount
                val totalItemCount = thisLayoutManager.itemCount

                searchSwipeRefresh.isEnabled = firstVisibleItem == 0

                val countCheck = firstVisibleItem + visibleItemCount == totalItemCount

                if (searchIsScrolling && countCheck && !searchIsLastItemReached) {
                    searchIsScrolling = false

                    db.collection("questions").whereArrayContains("tags", searchTagOne)
                        .orderBy("last_interaction", Query.Direction.DESCENDING)
                        .startAfter(searchLastVisible)
                        .limit(25).get()
                        .addOnSuccessListener { querySnapshot ->

                            if (!querySnapshot.isEmpty) {
                                for (question in querySnapshot) {
                                    searchedQuestionsRecyclerAdapter.add(
                                        SingleBoardRow(
                                            question.toObject(Question::class.java),
                                            activity as MainActivity
                                        )
                                    )
                                }

                                searchedQuestionsRecyclerAdapter.notifyDataSetChanged()

                                searchLastVisible = querySnapshot.documents[querySnapshot.size() - 1]

                                if (querySnapshot.size() < 25) {
                                    searchIsLastItemReached = true
                                }
                            } else {
                                searchIsLastItemReached = true
                            }
                        }
                }
            }
        })
    }


    fun listenToQuestions(currentLanguage: String) {

        questionsRowLayoutAdapter.clear()
        questionsBlockLayoutAdapter.clear()

        db.collection("questions").whereEqualTo("language", currentLanguage)
            .orderBy("last_interaction", Query.Direction.DESCENDING).limit(75)
            .get().addOnSuccessListener {

                if (it.size() == 0) {
                    freshMessage.visibility = View.VISIBLE
                    freshMessage2.visibility = View.VISIBLE
                } else {
                    freshMessage.visibility = View.GONE
                    freshMessage2.visibility = View.GONE

                    var postsCount = 0

                    for (document in it) {
                        val questionObject = document.toObject(Question::class.java)

                        singleQuestionLoop@ for (interest in interestsList) {

                            for (tag in questionObject.tags) {

                                if (interest == tag) {

                                    questionsRowLayoutAdapter.add(
                                        SingleBoardRow(
                                            questionObject,
                                            activity as MainActivity
                                        )
                                    )
                                    questionsBlockLayoutAdapter.add(
                                        (SingleBoardBlock(
                                            questionObject,
                                            activity as MainActivity
                                        ))
                                    )

                                    postsCount++

                                    break@singleQuestionLoop
                                }
                            }
                        }
                    }

                    if (postsCount == 0 && it.size() > 0) {
                        val activity = activity as MainActivity
                        activity.subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            activity.editInterestsFragment,
                            "editInterestsFragment"
                        ).addToBackStack("editInterestsFragment")
                            .commit()
                        activity.subActive = activity.editInterestsFragment
                        activity.switchVisibility(1)

                        freshMessage.text = "Untuned!"
                        freshMessage2.text = "Head to your profile and add more interests to populate your forum"
                        freshMessage.visibility = View.VISIBLE
                        freshMessage2.visibility = View.VISIBLE
                    } else {
                        freshMessage.visibility = View.GONE
                        freshMessage2.visibility = View.GONE
                    }

                    questionsBlockLayoutAdapter.notifyDataSetChanged()
                    questionsRowLayoutAdapter.notifyDataSetChanged()

                    boardLastVisible = it.documents[it.size() - 1]
                }
            }

        questionsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    boardIsScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val thisLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                val firstVisibleItem = thisLayoutManager.findFirstCompletelyVisibleItemPosition()
                val visibleItemCount = thisLayoutManager.childCount
                val totalItemCount = thisLayoutManager.itemCount

                questionSwipeRefresh.isEnabled = firstVisibleItem == 0

                val countCheck = firstVisibleItem + visibleItemCount == totalItemCount

                if (boardIsScrolling && countCheck && !boardIsLastItemReached) {
                    boardIsScrolling = false

                    db.collection("questions").whereEqualTo("language", currentLanguage)
                        .orderBy("last_interaction", Query.Direction.DESCENDING).startAfter(boardLastVisible)
                        .limit(25).get()
                        .addOnSuccessListener { querySnapshot ->


                            if (!querySnapshot.isEmpty) {
                                for (document in querySnapshot) {
                                    val questionObject = document.toObject(Question::class.java)

                                    singleQuestionLoop@ for (interest in interestsList) {

                                        for (tag in questionObject.tags) {

                                            if (interest == tag) {

                                                questionsRowLayoutAdapter.add(
                                                    SingleBoardRow(
                                                        questionObject,
                                                        activity as MainActivity
                                                    )
                                                )
                                                questionsBlockLayoutAdapter.add(
                                                    (SingleBoardBlock(
                                                        questionObject,
                                                        activity as MainActivity
                                                    ))
                                                )
                                                break@singleQuestionLoop
                                            }
                                        }
                                    }
                                }

                                questionsBlockLayoutAdapter.notifyDataSetChanged()
                                questionsRowLayoutAdapter.notifyDataSetChanged()

                                boardLastVisible = querySnapshot.documents[querySnapshot.size() - 1]

                                if (querySnapshot.size() < 25) {
                                    boardIsLastItemReached = true
                                }
                            } else {
                                boardIsLastItemReached = true
                            }
                        }
                }
            }
        })

    }

    companion object {
        fun newInstance(): BoardFragment = BoardFragment()
    }

}