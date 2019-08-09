package com.republicera

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.fragments.*
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.*
import com.republicera.viewModels.*
import com.volcaniccoder.bottomify.BottomifyNavigationView
import com.volcaniccoder.bottomify.OnNavigationItemChangeListener
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_home_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.subcontents_main.*
import java.util.*

class MainActivity : AppCompatActivity(), GeneralMethods {

    val topLevelDB = FirebaseFirestore.getInstance()
    lateinit var db: DocumentReference
    lateinit var firebaseAnalytics: FirebaseAnalytics
    var uid: String? = null
    var topLevelUser: User? = null

    lateinit var sharedPref: SharedPreferences


    var currentCommunity: Community? = null

    lateinit var lastCommunity: String

    private lateinit var bottomNavigationAdmin: BottomifyNavigationView
    lateinit var bottomNavigation: BottomifyNavigationView

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout
    lateinit var userHomeFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager
    val userFm = supportFragmentManager

    var active: Fragment? = null
    var subActive: Fragment? = null
    var userActive: Fragment? = null

    lateinit var currentTopLevelUserViewModel: CurrentUserViewModel
    private lateinit var currentProfile: CommunityProfile
    private lateinit var currentProfileViewModel: CurrentCommunityProfileViewModel
    private lateinit var randomUserViewModel: RandomUserViewModel
    private lateinit var questionViewModel: QuestionViewModel
    private lateinit var interestsViewModel: InterestsViewModel
    private lateinit var tagsViewModel: TagsViewModel
    private lateinit var followedAccountsViewModel: FollowedAccountsViewModel
    private lateinit var followersViewModel: FollowersViewModel
    private lateinit var currentCommunityViewModel: CurrentCommunityViewModel


    lateinit var boardFragment: BoardFragment
    lateinit var shoutsFragment: ShoutsFragment
    lateinit var adminsFragment: AdminsFragment
    lateinit var profileCurrentUserFragment: ProfileCurrentUserFragment


    lateinit var profileRandomUserFragment: ProfileRandomUserFragment
    lateinit var profileSecondRandomUserFragment: ProfileSecondRandomUserFragment
    lateinit var openedQuestionFragment: OpenedQuestionFragment
    lateinit var boardNotificationsFragment: BoardNotificationsFragment
    lateinit var savedQuestionFragment: SavedQuestionsFragment
    lateinit var answerFragment: AnswerFragment
    lateinit var newQuestionFragment: NewQuestionFragment
    lateinit var editProfileFragment: EditProfileFragment
    //    lateinit var websiteViewFragment: WebsiteViewFragment
    lateinit var editQuestionFragment: EditQuestionFragment
    lateinit var editAnswerFragment: EditAnswerFragment
    lateinit var editInterestsFragment: EditInterestsFragment
    lateinit var shoutExpendedFragment: ShoutExpendedFragment
    lateinit var searchFragment: SearchFragment
    lateinit var savedShoutsFragment: SavedShoutsFragment
    lateinit var shoutsNotificationsFragment: ShoutsNotificationsFragment
    lateinit var adminsNewQuestionFragment: AdminsNewQuestionFragment
    lateinit var adminsSearchFragment: AdminsSearchFragment
    lateinit var adminsAnswerFragment: AdminsAnswerFragment
    lateinit var adminsEditAnswerFragment: AdminsEditAnswerFragment
    lateinit var adminsOpenedQuestionFragment: AdminsOpenedQuestionFragment
    lateinit var adminsEditQuestionFragment: AdminsEditQuestionFragment
    lateinit var adminsSavedQuestionsFragment: AdminsSavedQuestionsFragment
    lateinit var adminsNotificationsFragment: AdminNotificationsFragment


    lateinit var communitiesHomeFragment: CommunitiesHome
    lateinit var exploreCommunitiesFragment: ExploreCommunitiesFragment
    lateinit var newCommunityFragment: NewCommunityFragment
    lateinit var editLanguagePreferencesFragment: EditLanguagePreferencesFragment
    lateinit var editContactDetailsFragment: EditContactDetailsFragment
    lateinit var editBasicInfoFragment: EditBasicInfoFragment


    var boardNotificationsCount = MutableLiveData<Int>()
    var shoutsNotificationsCount = MutableLiveData<Int>()
    var adminNotificationsCount = MutableLiveData<Int>()

    var isOpenedQuestionActive = false
    var isEditAnswerActive = false
    var isBoardNotificationsActive = false
    var isRandomUserProfileActive = false
    var isSecondRandomUserProfileActive = false

    var isFetchingImage = false
    var userHasBeenInitialized = false

    var tags: MutableList<SingleTagForList> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this, "ca-app-pub-3582328763375520~1669417159")

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        subFrame = feed_subcontents_frame_container
        mainFrame = feed_frame_container
        userHomeFrame = user_home_frame_container

        bottomNavigationAdmin = main_activity_bottom_nav_admin
        bottomNavigation = main_activity_bottom_nav_not_admin

        communitiesHomeFragment = CommunitiesHome()
        exploreCommunitiesFragment = ExploreCommunitiesFragment()
        newCommunityFragment = NewCommunityFragment()
        editLanguagePreferencesFragment = EditLanguagePreferencesFragment()
        editContactDetailsFragment = EditContactDetailsFragment()
        editBasicInfoFragment = EditBasicInfoFragment()


        val sharedPref = getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE)
        lastCommunity = sharedPref!!.getString("last_community", "default")!!


        currentTopLevelUserViewModel = ViewModelProviders.of(this).get(CurrentUserViewModel::class.java)
        tagsViewModel = ViewModelProviders.of(this).get(TagsViewModel::class.java)
        currentProfileViewModel = ViewModelProviders.of(this).get(CurrentCommunityProfileViewModel::class.java)
        questionViewModel = ViewModelProviders.of(this).get(QuestionViewModel::class.java)
        randomUserViewModel = ViewModelProviders.of(this).get(RandomUserViewModel::class.java)
        interestsViewModel = ViewModelProviders.of(this).get(InterestsViewModel::class.java)
        followedAccountsViewModel = ViewModelProviders.of(this).get(FollowedAccountsViewModel::class.java)
        followersViewModel = ViewModelProviders.of(this).get(FollowersViewModel::class.java)
        currentCommunityViewModel = ViewModelProviders.of(this).get(CurrentCommunityViewModel::class.java)

        currentCommunityViewModel.currentCommunity.observe(this, Observer { community ->
            currentCommunity = community
            userFm.popBackStack("chooseCommunityFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            changeCommunity()
        })
    }

    override fun onPause() {
        super.onPause()
        userFm.popBackStack("communitiesHomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onResume() {
        super.onResume()

        if (uid == null) {

            if (!checkIfLoggedIn()) {

                userFm.beginTransaction()
                    .add(R.id.user_home_frame_container, communitiesHomeFragment, "communitiesHomeFragment")
                    .addToBackStack("communitiesHomeFragment")
                    .commit()
                userActive = communitiesHomeFragment
                userHomeFrame.visibility = View.VISIBLE
            } else {

                if (lastCommunity != "default") {

                    topLevelDB.collection("communities").document(lastCommunity).get().addOnSuccessListener {
                        val community = it.toObject(Community::class.java)
                        if (community != null) {
                            currentCommunityViewModel.currentCommunity.postValue(community)
                        }
                    }
                } else {

                    userFm.beginTransaction()
                        .add(
                            R.id.user_home_frame_container,
                            communitiesHomeFragment,
                            "chooseCommunityFragment"
                        )
                        .addToBackStack("chooseCommunityFragment")
                        .commit()
                    userActive = communitiesHomeFragment
                    userHomeFrame.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun checkIfLoggedIn(): Boolean {
        uid = FirebaseAuth.getInstance().uid
        return if (uid == null) {
            false
        } else {
            fetchCurrentUser(uid!!)
            true
        }
    }


    private fun fetchCurrentUser(uid: String) {
        topLevelDB.collection("users").document(uid).get().addOnSuccessListener {
            topLevelUser = it.toObject(User::class.java)
            if (topLevelUser != null) {
                currentTopLevelUserViewModel.currentUserObject.postValue(topLevelUser)
                userHasBeenInitialized = true
            }
        }
    }


    fun takeToRegister() {
        val intent = Intent(this, RegisterLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    private fun changeCommunity() {

        db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity!!.id)
        main_activity_changing_community_splash_screen.visibility = View.VISIBLE
        bottomNavigationAdmin.isClickable = false
        bottomNavigation.isClickable = false
        fetchCurrentProfile(uid!!)
    }

    private fun fetchCurrentProfile(uid: String) {
        db.collection("profiles").document(uid).get().addOnSuccessListener {
            val profile = it.toObject(CommunityProfile::class.java)
            if (profile != null) {
                currentProfile = profile
                currentProfileViewModel.currentCommunityProfileObject = currentProfile
                setupBottomNav()
            } else {

//                topLevelDB.collection("users").document(uid).get().addOnSuccessListener { documentSnapshot ->
//                    val topLevelUser = documentSnapshot.toObject(User::class.java)
//
//                }

                if (topLevelUser != null) {

                    val currentTime = Date(System.currentTimeMillis())
                    val newCommunityProfile =
                        CommunityProfile(
                            uid,
                            topLevelUser!!.first_name + " " + topLevelUser!!.last_name,
                            "",
                            listOf(),
                            "",
                            0,
                            0,
                            0,
                            0,
                            0,
                            currentTime,
                            currentTime
                        )

                    db.collection("profiles").document(uid).set(newCommunityProfile)
                        .addOnSuccessListener {
                            currentProfile = newCommunityProfile
                            currentProfileViewModel.currentCommunityProfileObject =
                                currentProfile

                            topLevelUser!!.communities_list.add(currentCommunity!!.id)

                            currentTopLevelUserViewModel.currentUserObject.postValue(topLevelUser)

                            setupBottomNav()
                        }
                }
            }
        }
    }


    private fun setupBottomNav() {

        if (currentCommunity!!.admins.contains(currentProfile.uid) || currentCommunity!!.founder == currentProfile.uid) {

            adminsFragment = AdminsFragment()
            adminsNewQuestionFragment = AdminsNewQuestionFragment()
            adminsSearchFragment = AdminsSearchFragment()
            adminsAnswerFragment = AdminsAnswerFragment()
            adminsEditAnswerFragment = AdminsEditAnswerFragment()
            adminsOpenedQuestionFragment = AdminsOpenedQuestionFragment()
            adminsEditQuestionFragment = AdminsEditQuestionFragment()
            adminsSavedQuestionsFragment = AdminsSavedQuestionsFragment()
            adminsNotificationsFragment = AdminNotificationsFragment()

            fm.beginTransaction().add(R.id.feed_frame_container, adminsFragment, "adminsFragment")
                .hide(adminsFragment).commitAllowingStateLoss()
            subFm.beginTransaction()
                .add(R.id.feed_subcontents_frame_container, adminsNotificationsFragment, "adminsNotificationsFragment")
                .hide(adminsNotificationsFragment).commitAllowingStateLoss()
            subFm.beginTransaction()
                .add(
                    R.id.feed_subcontents_frame_container,
                    adminsSavedQuestionsFragment,
                    "adminsSavedQuestionsFragment"
                )
                .hide(adminsSavedQuestionsFragment).commitAllowingStateLoss()

            bottomNavigationAdmin.visibility = View.VISIBLE
            bottomNavigation.visibility = View.GONE
        } else {
            bottomNavigationAdmin.visibility = View.GONE
            bottomNavigation.visibility = View.VISIBLE
        }

        bottomNavigationAdmin.setOnNavigationItemChangedListener(object : OnNavigationItemChangeListener {
            override fun onNavigationItemChanged(navigationItem: BottomifyNavigationView.NavigationItem) {
                when (navigationItem.position) {
                    0 -> navigateToBoard()
                    1 -> navigateToShouts()
                    2 -> navigateToQuorum()
                    3 -> navigateToProfile()
                }
            }
        })

        bottomNavigation.setOnNavigationItemChangedListener(object : OnNavigationItemChangeListener {
            override fun onNavigationItemChanged(navigationItem: BottomifyNavigationView.NavigationItem) {
                when (navigationItem.position) {
                    0 -> navigateToBoard()
                    1 -> navigateToShouts()
                    2 -> navigateToProfile()
                }
            }
        })

        addTagsToViewModel()
    }


    private fun addTagsToViewModel() {
        tags.clear()
        db.collection("tags").get().addOnSuccessListener {
            for (document in it) {
                for (field in document.data) {
                    tags.add(SingleTagForList(field.key, field.value.toString().toInt()))
                }
            }
            tagsViewModel.tagList = tags

            createInterestList()
        }
    }


    private fun createInterestList() {

        db.collection("interests").document(currentProfile.uid).get().addOnSuccessListener {
            val doc = it.data
            if (doc != null) {
                val interestsList = doc["interests_list"] as MutableList<String>
                interestsViewModel.interestList.postValue(interestsList)
            }
            createFollowedAccountsList()
            Log.d("checkProgress", "createInterestList")
        }
    }

    private fun createFollowedAccountsList() {
        Log.d("checkProgress", "createFollowedAccountsList1")

        followedAccountsViewModel.followedAccounts.postValue(mutableListOf())

        db.collection("accounts_that_user_follows").document(currentProfile.uid).get()
            .addOnSuccessListener {

                val doc = it.data
                if (doc != null) {
                    val accountsList = doc["accounts_list"] as MutableList<String>
                    followedAccountsViewModel.followedAccounts.postValue(accountsList)
                }
                createFollowersList()
                Log.d("checkProgress", "createFollowedAccountsList2")

            }.addOnFailureListener {
                Log.d("checkProgress", "createFollowedAccountsList3" + it.localizedMessage)

            }
    }


    private fun createFollowersList() {

        followersViewModel.followers.postValue(mutableListOf())

        db.collection("accounts_that_follow_user").document(currentProfile.uid).get()
            .addOnSuccessListener {

                val doc = it.data
                if (doc != null) {
                    val accountsList = doc["accounts_list"] as MutableList<String>
                    followersViewModel.followers.postValue(accountsList)
                }
                addFragmentsToFragmentManagers()
            }
    }


    private fun addFragmentsToFragmentManagers() {
        //main container
        boardFragment = BoardFragment()
        shoutsFragment = ShoutsFragment()
        profileCurrentUserFragment = ProfileCurrentUserFragment()


        fm.beginTransaction().add(R.id.feed_frame_container, boardFragment, "feedFragment")
            .hide(boardFragment).commitAllowingStateLoss()

        fm.beginTransaction().add(R.id.feed_frame_container, shoutsFragment, "shoutsFragment")
            .hide(shoutsFragment).commitAllowingStateLoss()



        fm.beginTransaction()
            .add(R.id.feed_frame_container, profileCurrentUserFragment, "profileCurrentUserFragment")
            .hide(profileCurrentUserFragment).commitAllowingStateLoss()

        active = boardFragment

        //sub container
        profileRandomUserFragment = ProfileRandomUserFragment()
        profileSecondRandomUserFragment = ProfileSecondRandomUserFragment()
        openedQuestionFragment = OpenedQuestionFragment()
        boardNotificationsFragment = BoardNotificationsFragment()
        savedQuestionFragment = SavedQuestionsFragment()
        answerFragment = AnswerFragment()
        newQuestionFragment = NewQuestionFragment()
        editProfileFragment = EditProfileFragment()
        editQuestionFragment = EditQuestionFragment()
        editAnswerFragment = EditAnswerFragment()
        editInterestsFragment = EditInterestsFragment()
        shoutExpendedFragment = ShoutExpendedFragment()
        searchFragment = SearchFragment()
        savedShoutsFragment = SavedShoutsFragment()
        shoutsNotificationsFragment = ShoutsNotificationsFragment()



        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, boardNotificationsFragment, "boardNotificationsFragment")
            .hide(boardNotificationsFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, savedQuestionFragment, "savedQuestionFragment")
            .hide(savedQuestionFragment).commitAllowingStateLoss()

        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, shoutsNotificationsFragment, "shoutsNotificationsFragment")
            .hide(shoutsNotificationsFragment).commitAllowingStateLoss()
        subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, savedShoutsFragment, "savedShoutsFragment")
            .hide(savedShoutsFragment).commitAllowingStateLoss()



        allowUserInteraction()
    }


    private fun allowUserInteraction() {
        main_activity_changing_community_splash_screen.visibility = View.GONE
        bottomNavigationAdmin.isClickable = true
        sharedPref = getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE)
        val lastFeed = sharedPref.getString("last_feed", "board")!!
        if (lastFeed == "board") {
            navigateToBoard()
        } else {
            navigateToShouts()
        }
    }

    private fun resetFragments() {
        closeKeyboard(this)

        isOpenedQuestionActive = false
        isEditAnswerActive = false
        isBoardNotificationsActive = false
        isRandomUserProfileActive = false
        isSecondRandomUserProfileActive = false

        if (mainFrame.visibility == View.GONE) {
            switchVisibility(0)
        }

        for (i in 0 until subFm.backStackEntryCount) {
            subFm.popBackStack()
        }

        if (currentCommunity!!.admins.contains(currentProfile.uid) || currentCommunity!!.founder == currentProfile.uid) {
            subFm.beginTransaction().hide(boardNotificationsFragment).hide(shoutsNotificationsFragment)
                .hide(adminsNotificationsFragment)
                .hide(savedQuestionFragment).hide(savedShoutsFragment).hide(adminsSavedQuestionsFragment).commit()
        } else {
            subFm.beginTransaction().hide(boardNotificationsFragment).hide(shoutsNotificationsFragment)
                .hide(savedQuestionFragment).hide(savedShoutsFragment).commit()
        }


    }

    fun switchVisibility(case: Int) {
        when (case) {
            0 -> {
                mainFrame.visibility = View.VISIBLE
                subFrame.visibility = View.GONE
                userHomeFrame.visibility = View.GONE
            }

            1 -> {
                mainFrame.visibility = View.GONE
                subFrame.visibility = View.VISIBLE
                userHomeFrame.visibility = View.GONE
            }

            else -> {
                mainFrame.visibility = View.GONE
                subFrame.visibility = View.GONE
                userHomeFrame.visibility = View.VISIBLE
            }
        }
    }


    override fun onBackPressed() {

        when {
            userHomeFrame.visibility == View.VISIBLE -> {

                when (userActive) {

                    editLanguagePreferencesFragment -> {
                        userFm.popBackStack("editLanguagePreferencesFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        userActive = communitiesHomeFragment
                    }

                    editContactDetailsFragment -> {
                        userFm.popBackStack("editContactDetailsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        userActive = communitiesHomeFragment
                    }

                    newCommunityFragment -> {
                        userFm.popBackStack("newCommunityFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        userActive = communitiesHomeFragment
                    }

                    exploreCommunitiesFragment -> {
                        userFm.popBackStack("exploreCommunitiesFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        userActive = communitiesHomeFragment
                    }

                    communitiesHomeFragment -> {

                        if (topLevelUser != null && currentCommunity != null) {
                            switchVisibility(0)
                        } else {
                            super.onBackPressed()
                        }
                    }
                }
            }

            subFrame.visibility == View.VISIBLE -> {

                when (subActive) {

                    profileRandomUserFragment -> {
                        subFm.popBackStack("profileRandomUserFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        isRandomUserProfileActive = false

                        when {
                            isBoardNotificationsActive -> {
                                subActive = boardNotificationsFragment
                            }
                            isOpenedQuestionActive -> {
                                subActive = openedQuestionFragment
                            }
                        }
                    }

                    profileSecondRandomUserFragment -> {
                        subFm.popBackStack("profileSecondRandomUserFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                    }

                    openedQuestionFragment -> {
                        if (isBoardNotificationsActive) {
                            subActive = boardNotificationsFragment
                        } else {
                            switchVisibility(0)
                        }
                        subFm.popBackStack("openedQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        isOpenedQuestionActive = false
                    }

                    boardNotificationsFragment -> {
                        isBoardNotificationsActive = false
                        subFm.beginTransaction().hide(boardNotificationsFragment).commit()
                        switchVisibility(0)
                    }

                    savedQuestionFragment -> {
                        isBoardNotificationsActive = false
                        subFm.beginTransaction().hide(savedQuestionFragment).commit()
                        switchVisibility(0)
                    }

                    answerFragment -> {
                        subFm.popBackStack("answerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                    }

                    newQuestionFragment -> {
                        subFm.popBackStack("newQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = searchFragment
                        Log.d("whatfragment", "newQ")
                    }

                    editProfileFragment -> {
                        subFm.popBackStack("editProfileFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

//                    webViewFragment -> {
//                        subFm.popBackStack("webViewFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
//                        subActive = imageExpandedFragment
//                    }

                    editQuestionFragment -> {
                        subFm.popBackStack("editQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                    }

                    editAnswerFragment -> {
                        subFm.popBackStack("editAnswerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = openedQuestionFragment
                        isEditAnswerActive = false
                    }

                    editInterestsFragment -> {
                        subFm.popBackStack("editInterestsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

                    editContactDetailsFragment -> {
                        subFm.popBackStack("editContactDetailsFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

                    shoutExpendedFragment -> {
                        subFm.popBackStack("shoutExpendedFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

                    searchFragment -> {
                        subFm.popBackStack("searchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                        Log.d("whatfragment", "search")

                    }

                    editLanguagePreferencesFragment -> {
                        subFm.popBackStack("editLanguagePreferencesFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }


                    adminsNewQuestionFragment -> {
                        adminsNewQuestionFragment.questionTitle.text.clear()
                        subFm.popBackStack("adminsNewQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = adminsSearchFragment
                    }

                    adminsSearchFragment -> {
                        subFm.popBackStack("adminsSearchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }

                    adminsAnswerFragment -> {
                        subFm.popBackStack("adminsAnswerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = adminsOpenedQuestionFragment
                    }
                    adminsEditAnswerFragment -> {
                        subFm.popBackStack("adminsEditAnswerFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = adminsOpenedQuestionFragment
                    }
                    adminsOpenedQuestionFragment -> {
                        subFm.popBackStack("adminsOpenedQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }
                    adminsEditQuestionFragment -> {
                        subFm.popBackStack("adminsEditQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = adminsOpenedQuestionFragment
                    }

                    adminsNotificationsFragment -> {
                        subFm.beginTransaction().hide(adminsNotificationsFragment).commit()
                        switchVisibility(0)
                    }

                    adminsSavedQuestionsFragment -> {
                        subFm.beginTransaction().hide(adminsSavedQuestionsFragment).commit()
                        switchVisibility(0)
                    }
                }

            }

            mainFrame.visibility == View.VISIBLE -> {
                when (active) { // main frame is active

                    profileCurrentUserFragment -> {
                        navigateToBoard()
                    }

                    shoutsFragment -> {
                        if (shoutsFragment.shoutButton.visibility == View.VISIBLE) {
                            shoutsFragment.hideShoutButtons()
                        } else {
                            navigateToBoard()
                        }
                    }

                    boardFragment -> {
                        super.onBackPressed()
                    }
                }
            }

        }
    }


//    private fun branchInitSession() {
//        Branch.getInstance().initSession({ branchUniversalObject, _, error ->
//
//            if (error == null) {
//                if (branchUniversalObject != null) {
//
//                    when (branchUniversalObject.contentMetadata.customMetadata["type"]) {
//                        "community" -> collectCommunity(branchUniversalObject)
//                    }
//                }
//            } else {
//                println("branch definitely error" + error.message)
//            }
//        }, this.intent.data, this)
//    }


    private fun branchInitSession() {
        Log.d("branch my testtttt", "init")

        // Branch init
        Branch.getInstance().initSession({ referringParams, error ->
            if (error == null) {
                Log.d("branch my testtttt", referringParams.toString())

                if (referringParams != null) {
                    if (referringParams.has("type")) {
                        when (referringParams.getString("type")) {
                            "community" -> collectCommunity(referringParams.getString("\$canonical_identifier"))
                        }
                    }

                }
            } else {
                Log.d("branch my testtttt", "error")

                Log.e("BRANCH SDK", error.message)
            }
        }, this.intent.data, this)
    }


    override fun onStart() {
        super.onStart()
        branchInitSession()
    }

    //this has to do with branch
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    private fun navigateToBoard() {
        if (active != null) {
            fm.beginTransaction().hide(active!!).show(boardFragment).commit()
            active = boardFragment
            resetFragments()

            val editor = sharedPref.edit()
            editor.putString("last_feed", "board")
            editor.apply()
//            bottomNavigationAdmin.setActiveNavigationIndex(0)
        }
    }

    private fun navigateToShouts() {
        if (active != null) {
            fm.beginTransaction().hide(active!!).show(shoutsFragment).commit()
            active = shoutsFragment
            resetFragments()

            val editor = sharedPref.edit()
            editor.putString("last_feed", "shouts")
            editor.apply()
//            bottomNavigationAdmin.setActiveNavigationIndex(1)
        }
    }


    private fun navigateToQuorum() {
        if (active != null) {
            fm.beginTransaction().hide(active!!).show(adminsFragment).commit()
            active = adminsFragment
            resetFragments()
            profileCurrentUserFragment.scrollView.fullScroll(View.FOCUS_UP)
        }
    }


    fun navigateToProfile() {
        if (active != null) {
            fm.beginTransaction().hide(active!!).show(profileCurrentUserFragment).commit()
            active = profileCurrentUserFragment
            resetFragments()
            profileCurrentUserFragment.scrollView.fullScroll(View.FOCUS_UP)
        }
    }

    private fun collectCommunity(communityID: String) {


        topLevelDB.collection("communities").document(communityID).get().addOnSuccessListener {
            val community = it.toObject(Community::class.java)
            if (community != null) {

                topLevelUser!!.communities_list.add(communityID)
                currentTopLevelUserViewModel.currentUserObject.postValue(topLevelUser)
                currentCommunityViewModel.currentCommunity.postValue(community)

                val sharedPref =
                    getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE)

                val editor = sharedPref.edit()
                editor.putString("last_community", communityID)
                editor.apply()

                userHomeFrame.visibility = View.GONE
            }
        }
    }


    private fun collectProfile(branchUniversalObject: BranchUniversalObject) {
        val uid = FirebaseAuth.getInstance().uid

        val profileId = branchUniversalObject.canonicalIdentifier

        if (uid == profileId) {
            navigateToProfile()
        } else {
            db.collection("profiles").document(profileId).get().addOnSuccessListener {
                val user = it.toObject(CommunityProfile::class.java)
                if (user != null) {
                    randomUserViewModel.randomUserObject.postValue(user)
                    subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        profileRandomUserFragment,
                        "profileRandomUserFragment"
                    ).addToBackStack("profileRandomUserFragment").commit()
                    subActive = profileRandomUserFragment
                    switchVisibility(1)
                }
            }
        }
    }

    private fun collectQuestion(branchUniversalObject: BranchUniversalObject) {

        db.collection("questions").document(branchUniversalObject.canonicalIdentifier).get().addOnSuccessListener {
            val question = it.toObject(Question::class.java)
            if (question != null) {
                questionViewModel.questionObject.postValue(question)

                db.collection("profiles").document(question.author_ID).get().addOnSuccessListener { documentSnapshot ->
                    val randomUser = documentSnapshot.toObject(CommunityProfile::class.java)
                    if (randomUser != null) {
                        randomUserViewModel.randomUserObject.postValue(randomUser)
                        navigateToBoard()

                        subFm.beginTransaction().add(
                            R.id.feed_subcontents_frame_container,
                            openedQuestionFragment,
                            "openedQuestionFragment"
                        )
                            .addToBackStack("openedQuestionFragment").commit()
                        subActive = openedQuestionFragment
                        switchVisibility(1)
                    }
                }
            }
        }
    }


}
