package com.republicera.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.*
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.groupieAdapters.SingleBoardRow
import com.republicera.groupieAdapters.SingleContactDetailsIcon
import com.republicera.groupieAdapters.SingleQuestionAndAnswerRow
import com.republicera.groupieAdapters.SingleShout
import com.republicera.interfaces.ProfileMethods
import com.republicera.models.*
import com.republicera.viewModels.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_profile_random_user.*


class ProfileRandomUserFragment : Fragment(), ProfileMethods {

    lateinit var db: DocumentReference

    private val topLevelDB = FirebaseFirestore.getInstance()

    private lateinit var sharedViewModelRandomUser: RandomUserViewModel
    private lateinit var followedAccountsViewModel: FollowedAccountsViewModel
    private lateinit var sharedViewModelForQuestion: QuestionViewModel

    private lateinit var currentCommunity: Community

    private lateinit var followedAccountsList: MutableList<String>
    lateinit var followButton: TextView


    lateinit var userProfile: CommunityProfile
    lateinit var currentUser: CommunityProfile

    lateinit var questionsBtn: TextView
    lateinit var shoutsBtn: TextView
    lateinit var answersBtn: TextView

    lateinit var contactDetailsIconsRecycler: RecyclerView

    lateinit var profileGalleryShouts: RecyclerView
    lateinit var profileGalleryQuestions: RecyclerView
    lateinit var profileGalleryAnswers: RecyclerView

    lateinit var profileAnswers: TextView
    lateinit var profileFollowersCount : TextView

    private val galleryShoutsAdapter = GroupAdapter<ViewHolder>()
    private val galleryQuestionsAdapter = GroupAdapter<ViewHolder>()
    private val galleryAnswersAdapter = GroupAdapter<ViewHolder>()
    private val contactDetailsIconsAdapter = GroupAdapter<ViewHolder>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_random_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)

        val activity = activity as MainActivity

        activity.let {
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)

            followedAccountsViewModel =
                ViewModelProviders.of(it).get(FollowedAccountsViewModel::class.java)
            followedAccountsViewModel.followedAccounts.observe(activity, Observer { list->
                followedAccountsList = list
            })

            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                })
        }
        shoutsBtn = profile_ru_shouts_btn
        questionsBtn = profile_ru_questions_btn
        answersBtn = profile_ru_answers_btn

        contactDetailsIconsRecycler = profile_ru_staks_recycler
        profileGalleryShouts = profile_ru_gallery_shouts
        profileGalleryQuestions = profile_ru_gallery_questions
        profileGalleryAnswers = profile_ru_gallery_answers

        val profileTagLine = profile_ru_tagline
        val profilePicture: ImageView = profile_ru_image
        val profileName: TextView = profile_ru_user_name
        val profileReputation = profile_ru_reputation_count
         profileFollowersCount = profile_ru_followers_count
        profileAnswers = profile_ru_answers_count
        val profileFollowers = profile_ru_answers_count
        followButton = profile_ru_follow_button

        profileGalleryShouts.adapter = galleryShoutsAdapter
        profileGalleryShouts.layoutManager = LinearLayoutManager(this.context)

        profileGalleryQuestions.adapter = galleryQuestionsAdapter
        profileGalleryQuestions.layoutManager = LinearLayoutManager(this.context)

        profileGalleryAnswers.adapter = galleryAnswersAdapter
        profileGalleryAnswers.layoutManager = LinearLayoutManager(this.context)

        contactDetailsIconsRecycler.adapter = contactDetailsIconsAdapter

        shoutsBtn.setOnClickListener {
            changeGalleryFeed("shouts")
        }

        questionsBtn.setOnClickListener {
            changeGalleryFeed("questions")
        }

        answersBtn.setOnClickListener {
            changeGalleryFeed("answers")
        }


        sharedViewModelRandomUser.randomUserObject.observe(this, Observer {
            it?.let { user ->
                userProfile = user

                if (it.image.isNotEmpty()) {
                    Glide.with(this).load(it.image).into(profilePicture)
                } else {
                    Glide.with(this).load(R.drawable.user_profile).into(profilePicture)
                }

                profileName.text = it.name
                profileReputation.text = numberCalculation(it.reputation)

                profileFollowersCount.text =
                    if (userProfile.followers > 0) {
                        numberCalculation(userProfile.followers)
                    } else {
                        "0"
                    }
                if (it.tag_line.isNotEmpty()) {
                    profileTagLine.visibility = View.VISIBLE
                    profileTagLine.text = it.tag_line
                } else {
                    profileTagLine.visibility = View.GONE
                }

                listenToShouts()
                listenToQuestions()
                listenToAnswers()
                listenToContactDetails()

                db.collection("accounts_following").document(userProfile.uid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val doc = documentSnapshot.data
                        if (doc != null) {
                            val accountsList = doc["accounts_list"] as MutableList<String>
                            profileFollowers.text = if (accountsList.isNullOrEmpty()) {
                                "0"
                            } else {
                                numberCalculation(accountsList.size.toLong())
                            }
                        }
                    }

                if (followedAccountsList.contains(userProfile.uid)) {
                    followButton.tag = "followed"
                    followedButton(followButton, activity)
                } else {
                    notFollowedButton(followButton, activity)
                    followButton.tag = "notFollowed"
                }

                followButton.setOnClickListener {
                    if (followButton.tag == "followed") {
                        executeUnfollow()
                    } else {
                        executeFollow(activity)
                    }
                }
            }
        }
        )

        galleryQuestionsAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleBoardRow
            val question = row.question
            openQuestion(activity, question)
        }

        galleryAnswersAdapter.setOnItemClickListener { item, _ ->
            val row = item as SingleQuestionAndAnswerRow
            val question = row.question

            openQuestion(activity, question)
        }
    }

    private fun openQuestion(activity: MainActivity, question: Question) {

        sharedViewModelForQuestion.questionObject.postValue(question)
        sharedViewModelRandomUser.randomUserObject.postValue(userProfile)

        activity.subFm.beginTransaction()
            .add(R.id.feed_subcontents_frame_container, activity.openedQuestionFragment, "openedQuestionFragment")
            .addToBackStack("openedQuestionFragment").commit()

        activity.subActive = activity.openedQuestionFragment
        activity.isOpenedQuestionActive = true
        activity.switchVisibility(1)
    }

    private fun changeGalleryFeed(source: String) {

        when (source) {

            "shouts" -> {
                shoutsBtn.setTextColor(resources.getColor(R.color.gray700))
                questionsBtn.setTextColor(resources.getColor(R.color.gray300))
                answersBtn.setTextColor(resources.getColor(R.color.gray300))
                profileGalleryShouts.visibility = View.VISIBLE
                profileGalleryQuestions.visibility = View.GONE
                profileGalleryAnswers.visibility = View.GONE
            }

            "questions" -> {
                shoutsBtn.setTextColor(resources.getColor(R.color.gray300))
                questionsBtn.setTextColor(resources.getColor(R.color.gray700))
                answersBtn.setTextColor(resources.getColor(R.color.gray300))
                profileGalleryShouts.visibility = View.GONE
                profileGalleryQuestions.visibility = View.VISIBLE
                profileGalleryAnswers.visibility = View.GONE
            }

            "answers" -> {
                shoutsBtn.setTextColor(resources.getColor(R.color.gray300))
                questionsBtn.setTextColor(resources.getColor(R.color.gray300))
                answersBtn.setTextColor(resources.getColor(R.color.gray700))
                profileGalleryShouts.visibility = View.GONE
                profileGalleryQuestions.visibility = View.GONE
                profileGalleryAnswers.visibility = View.VISIBLE
            }
        }
    }

    private fun listenToShouts() {
        galleryShoutsAdapter.clear()

        db.collection("shouts").whereEqualTo("author_ID", userProfile.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener {
                for (oneShout in it) {
                    val shoutDoc = oneShout.toObject(Shout::class.java)
                    galleryShoutsAdapter.add(SingleShout(shoutDoc, userProfile, activity as MainActivity))
                }
            }
    }

    private fun listenToQuestions() {
        galleryQuestionsAdapter.clear()

        db.collection("questions").whereEqualTo("author_ID", userProfile.uid).orderBy("timestamp").get()
            .addOnSuccessListener {

                for (document in it) {
                    val questionObject = document.toObject(Question::class.java)
                    galleryQuestionsAdapter.add(SingleBoardRow(questionObject, activity as MainActivity))
                }
            }
    }

    private fun listenToAnswers() {
        galleryAnswersAdapter.clear()

        db.collection("answers").whereEqualTo("author_ID", userProfile.uid).orderBy("timestamp").get()
            .addOnSuccessListener {

                profileAnswers.text =
                    if (it.size() > 0) {
                        numberCalculation(it.size().toLong())
                    } else {
                        "0"
                    }

                for (document in it) {
                    val answerObject = document.toObject(Answer::class.java)
                    db.collection("questions").document(answerObject.question_ID).get()
                        .addOnSuccessListener { documentSnapshot ->

                            val questionObject = documentSnapshot.toObject(Question::class.java)

                            if (questionObject != null) {
                                galleryAnswersAdapter.add(
                                    SingleQuestionAndAnswerRow(
                                        questionObject,
                                        answerObject
                                    )
                                )

                            }
                        }
                }
            }
    }

    private fun listenToContactDetails() {
        contactDetailsIconsAdapter.clear()

        topLevelDB.collection("contact_details").document(userProfile.uid).get().addOnSuccessListener {

            val doc = it.data
            if (doc != null) {

                val list = doc as Map<String, Map<String, Any>>

                if (doc.isEmpty()) {
                    contactDetailsIconsRecycler.visibility = View.GONE
                } else {
                    contactDetailsIconsRecycler.visibility = View.VISIBLE
                }

                contactDetailsIconsRecycler.layoutManager = when (doc.size) {
                    1 -> GridLayoutManager(this.context, 1)
                    2 -> GridLayoutManager(this.context, 2)
                    3 -> GridLayoutManager(this.context, 3)
                    4 -> GridLayoutManager(this.context, 4)
                    5 -> GridLayoutManager(this.context, 5)
                    else -> GridLayoutManager(this.context, 6)

                }

                for (field in list) {

                    /*
                    cases:
                    email = 0
                    phone = 1
                    whatsApp = 2
                    twitter = 3
                    instagram = 4
                    personalWebsite = 5
                    businessWebsite = 6
                    linkedin = 7
                    facebook = 8
                    medium = 9
                    youtube = 10
                    snapchat = 11
                    */

                    contactDetailsIconsAdapter.add(
                        SingleContactDetailsIcon(
                            field.value.getValue("case").toString().toInt(),
                            field.value.getValue("info").toString(),
                            activity as MainActivity
                        )
                    )
                }
            }
        }
    }

    private fun executeUnfollow() {
        val batch = FirebaseFirestore.getInstance().batch()

        val followedAccountsRef = db.collection("accounts_that_user_follows").document(currentUser.uid)
        batch.set(followedAccountsRef, mapOf("accounts_list" to FieldValue.arrayRemove(userProfile.uid)), SetOptions.merge())

        val accountsFollowingRef = db.collection("accounts_that_follow_user").document(userProfile.uid)
        batch.set(accountsFollowingRef, mapOf("accounts_list" to FieldValue.arrayRemove(currentUser.uid)), SetOptions.merge())

        batch.commit().addOnSuccessListener {

            followButton.tag = "notFollowed"

            profileFollowersCount.text = numberCalculation(userProfile.followers -1)

            notFollowedButton(followButton, activity as MainActivity)

            followedAccountsList.remove(userProfile.uid)
            followedAccountsViewModel.followedAccounts.postValue(followedAccountsList)

        }
    }

    private fun executeFollow(activity: Activity) {

        val batch = FirebaseFirestore.getInstance().batch()

        val followedAccountsRef = db.collection("accounts_that_user_follows").document(currentUser.uid)
        batch.set(followedAccountsRef, mapOf("accounts_list" to FieldValue.arrayUnion(userProfile.uid)), SetOptions.merge())

        val accountsFollowingRef = db.collection("accounts_that_follow_user").document(userProfile.uid)
        batch.set(accountsFollowingRef, mapOf("accounts_list" to FieldValue.arrayUnion(currentUser.uid)), SetOptions.merge())

        batch.commit().addOnSuccessListener {

            followButton.tag = "followed"

            profileFollowersCount.text = numberCalculation(userProfile.followers +1)

            followedButton(followButton, activity as MainActivity)

            followedAccountsList.add(userProfile.uid)
            followedAccountsViewModel.followedAccounts.postValue(followedAccountsList)
        }
    }

    companion object {
        fun newInstance(): ProfileRandomUserFragment = ProfileRandomUserFragment()
    }
}