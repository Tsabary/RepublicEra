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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.algolia.search.saas.Client
import com.github.nisrulz.sensey.ChopDetector
import com.github.nisrulz.sensey.Sensey
import com.github.nisrulz.sensey.ShakeDetector
import com.github.nisrulz.sensey.WristTwistDetector
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.republicera.fragments.*
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.*
import com.republicera.viewModels.*
import io.branch.indexing.BranchUniversalObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.choose_forum_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.subcontents_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), GeneralMethods {

    val topLevelDB = FirebaseFirestore.getInstance()
    lateinit var db: DocumentReference
    lateinit var firebaseAnalytics: FirebaseAnalytics
    var uid: String? = null

    lateinit var sharedPref: SharedPreferences


    var currentCommunity: Community? = null

    lateinit var lastCommunity: String

    lateinit var bottomNavigation: ChipNavigationBar

    lateinit var mainFrame: FrameLayout
    lateinit var subFrame: FrameLayout
    lateinit var chooseCommunityFrame: FrameLayout

    val fm = supportFragmentManager
    val subFm = supportFragmentManager
    val CommunitiesFm = supportFragmentManager

    var active: Fragment? = null
    var subActive: Fragment? = null

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
    lateinit var quorumFragment: QuorumFragment
    lateinit var profileCurrentUserFragment: ProfileCurrentUserFragment


    lateinit var profileRandomUserFragment: ProfileRandomUserFragment
    lateinit var profileSecondRandomUserFragment: ProfileSecondRandomUserFragment
    lateinit var openedQuestionFragment: OpenedQuestionFragment
    lateinit var boardNotificationsFragment: BoardNotificationsFragment
    lateinit var savedQuestionFragment: SavedQuestionsFragment
    lateinit var answerFragment: AnswerFragment
    lateinit var newQuestionFragment: NewQuestionFragment
    lateinit var editProfileFragment: EditProfileFragment
    lateinit var websiteViewFragment: WebsiteViewFragment
    lateinit var editQuestionFragment: EditQuestionFragment
    lateinit var editAnswerFragment: EditAnswerFragment
    lateinit var editInterestsFragment: EditInterestsFragment
    lateinit var editContactDetailsFragment: EditContactDetailsFragment
    lateinit var shoutExpendedFragment: ShoutExpendedFragment
    lateinit var searchFragment: SearchFragment
    lateinit var editLanguagePreferencesFragment: EditLanguagePreferencesFragment
    lateinit var adminsNewQuestionFragment: AdminsNewQuestionFragment
    lateinit var adminsSearchFragment: AdminsSearchFragment
    lateinit var savedShoutsFragment: SavedShoutsFragment
    lateinit var shoutsNotificationsFragment: ShoutsNotificationsFragment
    lateinit var communitiesHomeFragment: CommunitiesHome
    lateinit var exploreCommunitiesFragment: ExploreCommunitiesFragment
    lateinit var newCommunityFragment: NewCommunityFragment


    var boardNotificationsCount = MutableLiveData<Int>()
    var shoutsNotificationsCount = MutableLiveData<Int>()

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

        subFrame = feed_subcontents_frame_container
        mainFrame = feed_frame_container
        chooseCommunityFrame = feed_choose_community_frame_container
        bottomNavigation = main_activity_bottom_nav

        communitiesHomeFragment = CommunitiesHome()
        exploreCommunitiesFragment = ExploreCommunitiesFragment()
        newCommunityFragment = NewCommunityFragment()


        val sharedPref = getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE)
        lastCommunity = sharedPref!!.getString("last_community", "default")!!


        currentTopLevelUserViewModel = ViewModelProviders.of(this).get(CurrentUserViewModel::class.java)
        tagsViewModel = ViewModelProviders.of(this).get(TagsViewModel::class.java)
        currentProfileViewModel = ViewModelProviders.of(this).get(CurrentCommunityProfileViewModel::class.java)
        questionViewModel = ViewModelProviders.of(this).get(QuestionViewModel::class.java)
        randomUserViewModel = ViewModelProviders.of(this).get(RandomUserViewModel::class.java)
        interestsViewModel = ViewModelProviders.of(this).get(InterestsViewModel::class.java)
        followedAccountsViewModel = ViewModelProviders.of(this).get(FollowedAccountsViewModel::class.java)
        followersViewModel= ViewModelProviders.of(this).get(FollowersViewModel::class.java)
        currentCommunityViewModel = ViewModelProviders.of(this).get(CurrentCommunityViewModel::class.java)

        currentCommunityViewModel.currentCommunity.observe(this, Observer { community ->
            currentCommunity = community
            CommunitiesFm.popBackStack("chooseCommunityFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            changeCommunity()
        })

        FirebaseApp.initializeApp(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        Sensey.getInstance().init(this)

        val wristTwistListener = WristTwistDetector.WristTwistListener {

            if (chooseCommunityFrame.visibility != View.VISIBLE) {
                if (subActive == searchFragment) {
                    subFm.popBackStack("searchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }

                CommunitiesFm.beginTransaction()
                    .add(
                        R.id.feed_choose_community_frame_container,
                        communitiesHomeFragment,
                        "chooseCommunityFragment"
                    ).addToBackStack("chooseCommunityFragment")
                    .commit()
                chooseCommunityFrame.visibility = View.VISIBLE

            }
        }

        Sensey.getInstance().startWristTwistDetection(1f, 1, wristTwistListener)


//        shakeListener = object : ShakeDetector.ShakeListener {
//            override fun onShakeDetected() {
//            }
//
//            override fun onShakeStopped() {
//                if (chooseCommunityFrame.visibility != View.VISIBLE) {
//                    if (subActive == searchFragment) {
//                        subFm.popBackStack("searchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
//                    }
//
//                    CommunitiesFm.beginTransaction()
//                        .add(
//                            R.id.feed_choose_community_frame_container,
//                            chooseCommunityFragment,
//                            "chooseCommunityFragment"
//                        ).addToBackStack("chooseCommunityFragment")
//                        .commit()
//                    chooseCommunityFrame.visibility = View.VISIBLE
//
//                }
//
//
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        CommunitiesFm.popBackStack("communitiesHomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onResume() {
        super.onResume()
        if (uid == null) {

            if (!checkIfLoggedIn()) {
                CommunitiesFm.beginTransaction()
                    .add(R.id.feed_choose_community_frame_container, communitiesHomeFragment, "communitiesHomeFragment")
                    .addToBackStack("communitiesHomeFragment")
                    .commit()
                chooseCommunityFrame.visibility = View.VISIBLE
            } else {
                if (lastCommunity != "default") {
                    topLevelDB.collection("communities").document(lastCommunity).get().addOnSuccessListener {
                        val community = it.toObject(Community::class.java)
                        if (community != null) {
                            currentCommunityViewModel.currentCommunity.postValue(community)
                        }
                    }
                } else {
                    CommunitiesFm.beginTransaction()
                        .add(
                            R.id.feed_choose_community_frame_container,
                            communitiesHomeFragment,
                            "chooseCommunityFragment"
                        )
                        .addToBackStack("chooseCommunityFragment")
                        .commit()
                    chooseCommunityFrame.visibility = View.VISIBLE
                }

            }
        }
    }


    fun checkIfLoggedIn(): Boolean {

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
            val user = it.toObject(User::class.java)
            if (user != null) {
                currentTopLevelUserViewModel.currentUserObject.postValue(user)
                userHasBeenInitialized = true
            }
        }
    }


    fun takeToRegister() {
        val intent = Intent(this, RegisterLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    fun changeCommunity() {

        db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity!!.id)
        main_activity_changing_community_splash_screen.visibility = View.VISIBLE
        bottomNavigation.isClickable = false
        fetchCurrentProfile(uid!!)
    }

    fun fetchCurrentProfile(uid: String) {
        db.collection("profiles").document(uid).get().addOnSuccessListener {
            val profile = it.toObject(CommunityProfile::class.java)
            if (profile != null) {

                currentProfile = profile
                currentProfileViewModel.currentCommunityProfileObject = currentProfile
                setupBottomNav()
            } else {

                topLevelDB.collection("users").document(uid).get().addOnSuccessListener { documentSnapshot ->
                    val topLevelUser = documentSnapshot.toObject(User::class.java)
                    if (topLevelUser != null) {

                        val currentTime = System.currentTimeMillis()
                        val newCommunityProfile =
                            CommunityProfile(uid, topLevelUser.firstName + " " + topLevelUser.lastName, "", listOf(), "", 0, 0, currentTime, currentTime)

                        val batch = topLevelDB.batch()

                        val profileRef = db.collection("profiles").document(uid)
                        batch.set(profileRef, newCommunityProfile)

                        val interestsRef = db.collection("interests").document(uid)
                        batch.set(interestsRef, mapOf("interests_list" to listOf("welcome-to-the-community")))

                        val communityMembersRef = topLevelDB.collection("communities").document(currentCommunity!!.id)
                        batch.update(communityMembersRef, "members", FieldValue.increment(1))

                        val updateUerCommunitiesRef = topLevelDB.collection("users").document(uid)
                        batch.update(updateUerCommunitiesRef, "communities_list", FieldValue.arrayUnion(currentCommunity!!.id))

                        val savedQuestionsRef = db.collection("saved_questions").document(uid)
                        batch.set(savedQuestionsRef, mapOf("saved_questions" to listOf<String>()))

                        val savedShoutsRef = db.collection("saved_shouts").document(uid)
                        batch.set(savedShoutsRef, mapOf("saved_shouts" to listOf<String>()))

                        val followedAccountsRef = db.collection("followed_accounts").document(uid)
                        batch.set(followedAccountsRef, mapOf("accounts_list" to listOf<String>()))

                        val accountsFollowingRef = db.collection("accounts_following").document(uid)
                        batch.set(accountsFollowingRef, mapOf("accounts_list" to listOf<String>()))

//                        if (currentCommunity!!.admins.size < 1) {
//                            val communityAdmins = topLevelDB.collection("communities").document(currentCommunity!!.id)
//                            batch.update(communityAdmins, "admins", FieldValue.arrayUnion(uid))
//                        }

                        batch.commit().addOnSuccessListener {
                            createCounter(
                                db.collection("reputation_counter").document(uid),
                                20
                            ).addOnSuccessListener {
                                currentProfile = newCommunityProfile
                                currentProfileViewModel.currentCommunityProfileObject =
                                    currentProfile

                                topLevelUser.communities_list.add(currentCommunity!!.id)

                                currentTopLevelUserViewModel.currentUserObject.postValue(topLevelUser)
                                increaseAlgoliaMemberCount()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun increaseAlgoliaMemberCount() {

        val applicationID = getString(R.string.algolia_application_id)
        val apiKey = getString(R.string.algolia_api_key)

        val client = Client(applicationID, apiKey)
        val index = client.getIndex("communities")

        val updateArray = ArrayList<JSONObject>()

        updateArray.add(
            JSONObject().put("members", currentCommunity!!.members + 1).put(
                "objectID",
                currentCommunity!!.id
            )
        )

        index.partialUpdateObjectsAsync(JSONArray(updateArray), null)
        setupBottomNav()
    }

    private fun setupBottomNav() {
        bottomNavigation.setOnItemSelectedListener {
            when (it) {
                R.id.destination_board -> navigateToBoard()
                R.id.destination_shouts -> navigateToShouts()
                R.id.destination_profile_logged_in_user -> navigateToProfile()
                R.id.destination_quorum -> navigateToQuorum()
            }
        }

        bottomNavigation.setItemSelected(R.id.destination_board)

        bottomNavigation.setHideOnScroll(true)
        if (currentCommunity!!.admins.contains(currentProfile.uid)) {
            bottomNavigation.setMenuResource(R.menu.navigation_admin)
        } else {
            bottomNavigation.setMenuResource(R.menu.navigation)
        }

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
        }
    }

    private fun createFollowedAccountsList() {

        followedAccountsViewModel.followedAccounts.postValue(mutableListOf())

        db.collection("followed_accounts").document(currentProfile.uid).get()
            .addOnSuccessListener {

                val doc = it.data
                if (doc != null) {
                    val accountsList = doc["accounts_list"] as MutableList<String>
                    followedAccountsViewModel.followedAccounts.postValue(accountsList)
                }
                createFollowersList()
            }
    }


    private fun createFollowersList() {

        followersViewModel.followers.postValue(mutableListOf())

        db.collection("accounts_following").document(currentProfile.uid).get()
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
        quorumFragment = QuorumFragment()
        profileCurrentUserFragment = ProfileCurrentUserFragment()


        fm.beginTransaction().add(R.id.feed_frame_container, boardFragment, "feedFragment")
            .hide(boardFragment).commitAllowingStateLoss()

        fm.beginTransaction().add(R.id.feed_frame_container, shoutsFragment, "shoutsFragment")
            .hide(shoutsFragment).commitAllowingStateLoss()

        fm.beginTransaction().add(R.id.feed_frame_container, quorumFragment, "quorumFragment")
            .hide(quorumFragment).commitAllowingStateLoss()

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
        editContactDetailsFragment = EditContactDetailsFragment()
        shoutExpendedFragment = ShoutExpendedFragment()
        searchFragment = SearchFragment()
        editLanguagePreferencesFragment = EditLanguagePreferencesFragment()
        adminsNewQuestionFragment = AdminsNewQuestionFragment()
        adminsSearchFragment = AdminsSearchFragment()
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
        bottomNavigation.isClickable = true
        sharedPref = getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE)
        val lastFeed = sharedPref.getString("last_feed", "board")!!
        if(lastFeed =="board"){
            navigateToBoard()
        } else {
            navigateToShouts()
        }

        bottomNavigation.setItemSelected(R.id.destination_board)
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

        subFm.beginTransaction().hide(boardNotificationsFragment).hide(shoutsNotificationsFragment)
            .hide(savedQuestionFragment).hide(savedShoutsFragment).commit()

    }

    fun switchVisibility(case: Int) {
        if (case == 0) {
            mainFrame.visibility = View.VISIBLE
            subFrame.visibility = View.GONE
        } else {
            mainFrame.visibility = View.GONE
            subFrame.visibility = View.VISIBLE
        }
    }


    override fun onBackPressed() {

        if (chooseCommunityFrame.visibility == View.VISIBLE) {
            CommunitiesFm.popBackStack("chooseCommunityFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            chooseCommunityFrame.visibility = View.GONE
        } else {

            when {
                mainFrame.visibility == View.GONE -> when (subActive) {

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
                        newQuestionFragment.questionTitle.text.clear()
                        subFm.popBackStack("newQuestionFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        subActive = searchFragment
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
                    }

                    editLanguagePreferencesFragment -> {
                        subFm.popBackStack("editLanguagePreferencesFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        switchVisibility(0)
                    }
                }

                else -> when (active) { // main frame is active

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
//                        "user" -> collectProfile(branchUniversalObject,)
//                        "question" -> collectQuestion(branchUniversalObject)
//                    }
//                }
//            } else {
//                println("branch definitely error" + error.message)
//            }
//        }, this.intent.data, this)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        sd.start(sm)
////        branchInitSession()
//    }
////
////    override fun onStart() {
////        super.onStart()
////        branchInitSession()
////    }
//
//    override fun onPause() {
//        super.onPause()
//        sd.stop()
//    }

    override fun onDestroy() {
        super.onDestroy()
        Sensey.getInstance().stop()
    }

    private fun createCounter(ref: DocumentReference, numShards: Int): Task<Void> {
        // Initialize the counter document, then initialize each shard.
        return ref.set(Counter(numShards))
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    Log.d("checkk", "createprofilesuuccess" +task.exception!!)

                    throw task.exception!!
                }

                val tasks = arrayListOf<Task<Void>>()

                // Initialize each shard with count=0
                for (i in 0 until numShards) {
                    val makeShard = ref.collection("shards")
                        .document(i.toString())
                        .set(Shard(0))

                    tasks.add(makeShard)
                }

                Tasks.whenAll(tasks)
            }
    }

    private fun navigateToBoard() {
        if (active != null) {
            fm.beginTransaction().hide(active!!).show(boardFragment).commit()
            active = boardFragment
            resetFragments()

            val editor = sharedPref.edit()
            editor.putString("last_feed", "board")
            editor.apply()
            bottomNavigation.setItemSelected(R.id.destination_board)
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
            bottomNavigation.setItemSelected(R.id.destination_shouts)

        }
    }


    private fun navigateToQuorum() {
        if (active != null) {
            fm.beginTransaction().hide(active!!).show(quorumFragment).commit()
            active = quorumFragment
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

