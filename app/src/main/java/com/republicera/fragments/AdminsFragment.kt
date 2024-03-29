package com.republicera.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mikepenz.materialdrawer.Drawer
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.*
import com.republicera.interfaces.BoardMethods
import com.republicera.models.*
import com.republicera.viewModels.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.toolbar_board.*
import kotlinx.android.synthetic.main.fragment_board.*
import kotlinx.android.synthetic.main.toolbar.*

class AdminsFragment : Fragment(), BoardMethods {

    lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var db: DocumentReference
    lateinit var currentCommunity: Community
    lateinit var currentLanguage: String
    var currentLayout = 0
    lateinit var sharedPref: SharedPreferences

    val tagsFilteredAdapter = GroupAdapter<ViewHolder>()
    private val searchedQuestionsRecyclerAdapter = GroupAdapter<ViewHolder>()
    private val reportedPostsRecyclerAdapter = GroupAdapter<ViewHolder>()
    private val questionsRowLayoutAdapter = GroupAdapter<ViewHolder>()
    private val questionsBlockLayoutAdapter = GroupAdapter<ViewHolder>()
    private val languagePickerAdapter = GroupAdapter<ViewHolder>()

    private lateinit var questionRecyclerLayoutManager: LinearLayoutManager

    lateinit var topLevelUser: User
    lateinit var sharedViewModelForQuestion: QuestionViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel
    lateinit var sharedViewModelInterests: InterestsViewModel
    lateinit var sharedViewModelTags: TagsViewModel

    lateinit var questionSwipeRefresh: SwipeRefreshLayout
    lateinit var searchSwipeRefresh: SwipeRefreshLayout
    lateinit var reportedPostsSwipeRefresh: SwipeRefreshLayout

    private lateinit var reportedPostsRecycler: RecyclerView
    private lateinit var questionsRecycler: RecyclerView
    private lateinit var searchedQuestionsRecycler: RecyclerView

    lateinit var layoutIcon: ImageButton

    lateinit var sectionTitle: TextView

    var interestsList: List<String> = listOf()

    lateinit var freshMessage: TextView
    lateinit var freshMessage2: TextView

    lateinit var boardLastVisible: DocumentSnapshot
    var boardIsScrolling = false
    var boardIsLastItemReached = false

    lateinit var reportedLastVisible: DocumentSnapshot
    var reportedIsScrolling = false
    var reportedIsLastItemReached = false

    lateinit var searchLastVisible: DocumentSnapshot
    var searchIsScrolling = false
    var searchIsLastItemReached = false

    private lateinit var boardFilterChipGroup: ChipGroup
    var searchedTagsList = mutableListOf<String>()

    lateinit var result: Drawer

    lateinit var communityProfile : CommunityProfile

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_board, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
        val activity = activity as MainActivity

        questionSwipeRefresh = board_questions_swipe_refresh
        searchSwipeRefresh = board_search_swipe_refresh

        searchedQuestionsRecycler = board_search_question_feed
        val searchedQuestionRecyclerLayoutManager = LinearLayoutManager(this.context)
        searchedQuestionRecyclerLayoutManager.reverseLayout = true
        searchedQuestionsRecycler.adapter = searchedQuestionsRecyclerAdapter
        searchedQuestionsRecycler.layoutManager = searchedQuestionRecyclerLayoutManager

        questionSwipeRefresh = board_questions_swipe_refresh
        questionsRecycler = board_question_feed
        questionsRecycler.adapter = questionsRowLayoutAdapter
        questionRecyclerLayoutManager = LinearLayoutManager(this.context)
        questionRecyclerLayoutManager.reverseLayout = true
        questionsRecycler.layoutManager = questionRecyclerLayoutManager

        reportedPostsSwipeRefresh = board_reported_swipe_refresh
        reportedPostsRecycler = board_reported_posts_feed
        reportedPostsRecycler.adapter = reportedPostsRecyclerAdapter
        reportedPostsRecycler.layoutManager = LinearLayoutManager(this.context)

        val languagePickerRecycler = board_languages_recycler
        languagePickerRecycler.adapter = languagePickerAdapter
        languagePickerRecycler.layoutManager = LinearLayoutManager(this.context!!)

        sectionTitle = board_community_title
        sectionTitle.tag = "board"
        sectionTitle.text = "Admins board"
        sectionTitle.setOnClickListener {
            switchSection()
        }

        layoutIcon = board_layout_icon
        val spinner = board_language_spinner
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
//
//        layoutIcon.setOnClickListener {
//            if (layoutIcon.tag == "row_layout") {
//                setLayout(1)
//            } else {
//                setLayout(0)
//            }
//        }

        activity.let {
            ViewModelProviders.of(it).get(CurrentUserViewModel::class.java).currentUserObject.observe(
                activity,
                Observer { user ->
                    topLevelUser = user
                    val shortenedList = mutableListOf<String>()

                    for (language in topLevelUser.lang_list) {
                        languagePickerAdapter.add(SingleLanguageOptionBoard(languageCodeToName(language)))
                    }
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

            communityProfile = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java).currentCommunityProfileObject

            sharedViewModelTags = ViewModelProviders.of(it).get(TagsViewModel::class.java)
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                    listenToQuestions()
                    listenToReported()
                })
        }


        languagePickerAdapter.setOnItemClickListener { item, _ ->
            val language = item as SingleLanguageOptionBoard
            currentLanguage = languageNameToCode(language.language)
            val editor = sharedPref.edit()
            editor.putString("last_language", currentLanguage)
            editor.apply()
            listenToQuestions()
            languagePickerRecycler.visibility = View.GONE
            spinner.text = currentLanguage

        }

//        spinner.setOnItemSelectedListener { _, position, _, _ ->
//            currentLanguage = topLevelUser.lang_list[position]
//            val editor = sharedPref.edit()
//            editor.putString("last_language", topLevelUser.lang_list[position])
//            editor.apply()
//            listenToQuestions()
//            listenToReported()
//        }

//        val notificationBadge = toolbar_with_search_notifications_badge
//
//        activity.adminNotificationsCount.observe(this, Observer {
//            it?.let { notCount ->
//                notificationBadge.setNumber(notCount)
//            }
//        })


        recyclersVisibility(0)


//        scrollView = board_questions_scroll_view

        val boardSearchBox = toolbar_board_search_box
        val tagSuggestionRecycler = board_search_recycler
        boardFilterChipGroup = toolbar_board_filter_chipgroup

        tagSuggestionRecycler.layoutManager = LinearLayoutManager(this.context)
        tagSuggestionRecycler.adapter = tagsFilteredAdapter

        questionSwipeRefresh.setOnRefreshListener {
            listenToQuestions()
            questionSwipeRefresh.isRefreshing = false
            questionSwipeRefresh.isEnabled = false
        }

        searchSwipeRefresh.setOnRefreshListener {
            searchQuestions(1)
            searchSwipeRefresh.isRefreshing = false
            searchSwipeRefresh.isEnabled = false
        }

        reportedPostsSwipeRefresh.setOnRefreshListener {
            listenToReported()
            reportedPostsSwipeRefresh.isRefreshing = false
        }


//        val boardNotificationIcon = toolbar_with_search_notifications_icon

//        notificationBadge.setOnClickListener {
//            goToNotifications(activity)
//        }
//
//        boardNotificationIcon.setOnClickListener {
//            goToNotifications(activity)
//        }

        setUpDrawerNav(activity, topLevelUser, communityProfile, 3)

        val menuIcon = toolbar_board_menu
        menuIcon.setOnClickListener {
            result.openDrawer()
        }

        val constraintButton = board_new_question_container

        constraintButton.setOnClickListener {
            activity.subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, activity.adminsSearchFragment, "adminsSearchFragment")
                .addToBackStack("adminsSearchFragment").commit()
            activity.subActive = activity.adminsSearchFragment
            activity.switchVisibility(1)
        }

        boardSearchBox.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagsFilteredAdapter.clear()

                val userInput = s.toString().toLowerCase().replace(" ", "-")

                if (userInput == "") {
                    tagSuggestionRecycler.visibility = View.GONE
                } else {
                    val relevantTags: List<SingleTagForList> =
                        sharedViewModelTags.tagList.filter { it.tagString.contains(userInput) }

                    for (t in relevantTags) {
                        tagSuggestionRecycler.visibility = View.VISIBLE
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
            recyclersVisibility(1)
            searchQuestions(0)
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

    private fun switchSection() {
        if (sectionTitle.tag == "board") {
            sectionTitle.tag = "reported"
            sectionTitle.text = "Posts review"
            recyclersVisibility(1)
            layoutIcon.visibility = View.GONE
        } else {
            sectionTitle.tag = "board"
            sectionTitle.text = "Admins board"
            recyclersVisibility(0)
            layoutIcon.visibility = View.VISIBLE
        }
    }

    private fun setLayout(case: Int) {
        val editor = sharedPref.edit()

        if (case == 0) {
            layoutIcon.setImageResource(R.drawable.blocks_layout)

            layoutIcon.tag = "row_layout"

            firebaseAnalytics.logEvent("board_row_layout", null)

            val position = questionRecyclerLayoutManager.findFirstCompletelyVisibleItemPosition()
            questionsRecycler.adapter = questionsRowLayoutAdapter

            editor.putInt("last_layout", 0)
        } else {
            layoutIcon.setImageResource(R.drawable.rows_layout)

            layoutIcon.tag = "block_layout"

            firebaseAnalytics.logEvent("board_block_layout", null)

            val position = questionRecyclerLayoutManager.findFirstCompletelyVisibleItemPosition()
            questionsRecycler.adapter = questionsBlockLayoutAdapter
            editor.putInt("last_layout", 1)

        }
        editor.apply()

    }

    private fun goToSavedQuestions(activity: MainActivity) {
        activity.subFm.beginTransaction().hide(activity.adminsNotificationsFragment)
            .show(activity.adminsSavedQuestionsFragment).commit()
        activity.subActive = activity.adminsSavedQuestionsFragment
        activity.switchVisibility(1)
        activity.isBoardNotificationsActive = true
    }

    private fun goToNotifications(activity: MainActivity) {
        activity.subFm.beginTransaction().hide(activity.adminsSavedQuestionsFragment)
            .show(activity.adminsNotificationsFragment)
            .commit()
        activity.subActive = activity.adminsNotificationsFragment

        activity.switchVisibility(1)
        activity.isBoardNotificationsActive = true
    }

    private fun openQuestion(author: String, activity: MainActivity) {

        activity.subFm.beginTransaction()
            .add(
                R.id.feed_subcontents_frame_container,
                activity.adminsOpenedQuestionFragment,
                "adminsOpenedQuestionFragment"
            )
            .addToBackStack("adminsOpenedQuestionFragment").commit()

        db.collection("profiles").document(author).get().addOnSuccessListener {
            val user = it.toObject(CommunityProfile::class.java)

            if (user != null) {
                sharedViewModelRandomUser.randomUserObject.postValue(user)
                activity.subActive = activity.adminsOpenedQuestionFragment
                activity.isOpenedQuestionActive = true
                activity.switchVisibility(1)
            }
        }
    }

    private fun recyclersVisibility(case: Int) {
        when (case) {
            0 -> {
                questionSwipeRefresh.visibility = View.VISIBLE
                reportedPostsSwipeRefresh.visibility = View.GONE
                searchSwipeRefresh.visibility = View.GONE
            }

            1 -> {
                questionSwipeRefresh.visibility = View.GONE
                reportedPostsSwipeRefresh.visibility = View.VISIBLE
                searchSwipeRefresh.visibility = View.GONE
            }

            else -> {
                questionSwipeRefresh.visibility = View.GONE
                reportedPostsSwipeRefresh.visibility = View.GONE
                searchSwipeRefresh.visibility = View.VISIBLE
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
            boardFilterChipGroup.removeView(it)
            searchedTagsList.remove(selectedTag)
            searchQuestions(1)
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

                searchQuestionsOneTag(
                    searchedTagsList[0]
                )
                if (case == 0) {
                    val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
                    firebaseAnalytics.logEvent("search_board", null)
                }
            }


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

        db.collection("admins_questions").whereArrayContains("tags", searchTagOne)
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

                    db.collection("admins_questions").whereArrayContains("tags", searchTagOne)
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


    fun listenToQuestions() {

        questionsRowLayoutAdapter.clear()
        questionsBlockLayoutAdapter.clear()

        db.collection("admins_questions").whereEqualTo("language", currentLanguage)
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

                    if (postsCount == 0) {
//                        val activity = activity as MainActivity
//                        activity.subFm.beginTransaction().add(
//                            R.id.feed_subcontents_frame_container,
//                            activity.editInterestsFragment,
//                            "editInterestsFragment"
//                        ).addToBackStack("editInterestsFragment")
//                            .commit()
//                        activity.subActive = activity.editInterestsFragment
//                        activity.switchVisibility(1)

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

                    db.collection("admins_questions").whereEqualTo("language", currentLanguage)
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


    fun listenToReported() {

        reportedPostsRecyclerAdapter.clear()

        db.collection("reported_shouts").orderBy("timestamp", Query.Direction.ASCENDING)
            .get().addOnSuccessListener {

                if (it.size() == 0) {
//                    freshMessage.visibility = View.VISIBLE //need another one for reported
                } else {
//                    freshMessage.visibility = View.GONE


                    for (document in it) {
                        val reportedShoutObject = document.toObject(ReportedShout::class.java)

                        reportedPostsRecyclerAdapter.add(
                            SingleReportedShout(
                                reportedShoutObject,
                                topLevelUser,
                                activity as MainActivity
                            )
                        )
                    }

                    reportedPostsRecyclerAdapter.notifyDataSetChanged()

                    reportedLastVisible = it.documents[it.size() - 1]
                }
            }

        reportedPostsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    reportedIsScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val thisLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                val firstVisibleItem = thisLayoutManager.findFirstCompletelyVisibleItemPosition()
                val visibleItemCount = thisLayoutManager.childCount
                val totalItemCount = thisLayoutManager.itemCount

                reportedPostsSwipeRefresh.isEnabled = firstVisibleItem == 0

                val countCheck = firstVisibleItem + visibleItemCount == totalItemCount

                if (reportedIsScrolling && countCheck && !reportedIsLastItemReached) {
                    reportedIsScrolling = false

                    db.collection("reported_shouts").orderBy("timestamp", Query.Direction.ASCENDING)
                        .startAfter(reportedLastVisible)
                        .limit(25).get()
                        .addOnSuccessListener { querySnapshot ->

                            if (!querySnapshot.isEmpty) {
                                for (document in querySnapshot) {
                                    val reportedShoutObject = document.toObject(ReportedShout::class.java)

                                    reportedPostsRecyclerAdapter.add(
                                        SingleReportedShout(
                                            reportedShoutObject,
                                            topLevelUser,
                                            activity as MainActivity
                                        )
                                    )
                                }

                                reportedPostsRecyclerAdapter.notifyDataSetChanged()

                                reportedLastVisible = querySnapshot.documents[querySnapshot.size() - 1]

                                if (querySnapshot.size() < 25) {
                                    reportedIsLastItemReached = true
                                }
                            } else {
                                reportedIsLastItemReached = true
                            }
                        }
                }
            }
        })

    }

    /*

    fun listenToReportedShouts() {

        reportedPostsRecyclerAdapter.clear()

        db.collection("reported_shouts").orderBy("timestamp", Query.Direction.ASCENDING)
            .get().addOnSuccessListener {
                for (document in it) {

                    if (reportedShoutObject.keeps.size + reportedShoutObject.removes.size >= currentCommunity.admins.size * 0.5) {
                        when {
                            reportedShoutObject.keeps.size >= reportedShoutObject.removes.size * 2 -> {
                                val batch = FirebaseFirestore.getInstance().batch()
                                val reportedShoutRef = db.collection("reported_shouts").document(reportedShoutObject.id)
                                batch.delete(reportedShoutRef)

                                val originalShoutRef = db.collection("shouts").document(reportedShoutObject.id)
                                batch.update(originalShoutRef, mapOf("reports" to listOf<String>(), "visible" to true))

                                batch.commit()
                            }

                            reportedShoutObject.removes.size >= reportedShoutObject.keeps.size * 2 -> {
                                val batch = FirebaseFirestore.getInstance().batch()
                                val reportedShoutRef = db.collection("reported_shouts").document(reportedShoutObject.id)
                                batch.delete(reportedShoutRef)

                                val originalShoutRef = db.collection("shouts").document(reportedShoutObject.id)
                                batch.delete(originalShoutRef)

                                batch.commit()
                            }
                        }
                    }
                }
            }
    }

*/
    companion object {
        fun newInstance(): AdminsFragment = AdminsFragment()
    }
}