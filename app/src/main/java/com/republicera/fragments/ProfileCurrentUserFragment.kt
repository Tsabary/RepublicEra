package com.republicera.fragments


import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.RegisterLoginActivity
import com.republicera.groupieAdapters.SingleBoardRow
import com.republicera.groupieAdapters.SingleContactDetailsIcon
import com.republicera.groupieAdapters.SingleQuestionAndAnswerRow
import com.republicera.groupieAdapters.SingleShout
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.*
import com.republicera.viewModels.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import kotlinx.android.synthetic.main.fragment_profile_current_user.*


class ProfileCurrentUserFragment : Fragment(), GeneralMethods {

    lateinit var db: DocumentReference

    private lateinit var galleryShoutsRecycler: RecyclerView
    private lateinit var galleryQuestionsRecycler: RecyclerView
    private lateinit var galleryAnswersRecycler: RecyclerView

    private val galleryShoutsAdapter = GroupAdapter<ViewHolder>()
    private val galleryQuestionsAdapter = GroupAdapter<ViewHolder>()
    private val galleryAnswersAdapter = GroupAdapter<ViewHolder>()

    private lateinit var contactDetailsIconsRecycler: RecyclerView
    private val contactDetailsIconsAdapter = GroupAdapter<ViewHolder>()

    private lateinit var shoutsBtn: TextView
    private lateinit var questionsBtn: TextView
    private lateinit var answersBtn: TextView

    lateinit var profileAnswers: TextView

    private lateinit var userProfile: CommunityProfile

    private lateinit var currentCommunityViewModel: CurrentCommunityViewModel
    private lateinit var contactDetailsViewModel: ContactDetailsViewModel
    private lateinit var currentCommunityProfileViewModel: CurrentCommunityProfileViewModel
    lateinit var sharedViewModelRandomUser: RandomUserViewModel
    private lateinit var sharedViewModelForQuestion: QuestionViewModel

    private lateinit var currentCommunity: Community

    lateinit var scrollView: NestedScrollView

    private lateinit var buo: BranchUniversalObject
    private lateinit var lp: LinkProperties

    lateinit var menuResign: MenuItem


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_profile_current_user, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this.context!!)
        val activity = activity as MainActivity

        scrollView = profile_li_scrollview
        val picture: ImageView = profile_li_image
        val name: TextView = profile_li_user_name

        galleryShoutsRecycler = profile_li_gallery_shouts
        galleryQuestionsRecycler = profile_li_gallery_questions
        galleryAnswersRecycler = profile_li_gallery_answers
        contactDetailsIconsRecycler = profile_li_staks_recycler
        val reputation = profile_li_reputation_count
        val profileFollowersCount = profile_li_followers_count
        profileAnswers = profile_li_answers_count
        val tagline = profile_li_tagline

        val menuButton = profile_li_menu_button
        val popup = PopupMenu(this.context, menuButton)
        popup.inflate(R.menu.profile_navigation)
        menuResign = popup.menu.getItem(2)
        menuButton.setOnClickListener {
            popup.show()
        }

        galleryShoutsRecycler.adapter = galleryShoutsAdapter
        galleryShoutsRecycler.layoutManager = LinearLayoutManager(this.context)

        galleryQuestionsRecycler.adapter = galleryQuestionsAdapter
        galleryQuestionsRecycler.layoutManager = LinearLayoutManager(this.context)

        galleryAnswersRecycler.adapter = galleryAnswersAdapter
        galleryAnswersRecycler.layoutManager = LinearLayoutManager(this.context)

        contactDetailsIconsRecycler.adapter = contactDetailsIconsAdapter

        shoutsBtn = profile_li_shouts_btn
        questionsBtn = profile_li_questions_btn
        answersBtn = profile_li_answers_btn

        activity.let {
            currentCommunityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)
            currentCommunityProfileViewModel =
                ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
            sharedViewModelRandomUser = ViewModelProviders.of(it).get(RandomUserViewModel::class.java)
            sharedViewModelForQuestion = ViewModelProviders.of(it).get(QuestionViewModel::class.java)
            contactDetailsViewModel = ViewModelProviders.of(it).get(ContactDetailsViewModel::class.java)

            ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java).currentCommunity.observe(
                activity,
                Observer { community ->
                    currentCommunity = community
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                    userProfile = currentCommunityProfileViewModel.currentCommunityProfileObject
                    menuResign.isVisible = currentCommunity.admins.contains(userProfile.uid)
                    listenToShouts()
                    listenToQuestions()
                    listenToAnswers()
                    listenToContactDetails()
                })



            getCount(db.collection("reputation_counter").document(userProfile.uid)).addOnSuccessListener { count ->
                reputation.text = count.toString()
                db.collection("profiles").document(userProfile.uid)
                    .set(mapOf("reputation" to count), SetOptions.merge())
            }

            Glide.with(this).load(
                if (userProfile.image.isNotEmpty()) {
                    userProfile.image
                } else {
                    R.drawable.user_profile
                }
            ).into(picture)


            name.text = userProfile.name
            reputation.text = numberCalculation(userProfile.reputation)

            profileFollowersCount.text =
                if (userProfile.followers > 0) {
                    numberCalculation(userProfile.followers)
                } else {
                    "0"
                }

            if (userProfile.tag_line.isNotEmpty()) {
                tagline.visibility = View.VISIBLE
                tagline.text = userProfile.tag_line
            } else {
                tagline.visibility = View.GONE
            }

            buo = BranchUniversalObject()
                .setCanonicalIdentifier(userProfile.uid)
                .setTitle("Get Dere and join traveler communities")
                .setContentDescription("")
                .setContentImageUrl(userProfile.image)
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(ContentMetadata().addCustomMetadata("type", "user"))

            lp = LinkProperties()
                .setFeature("inviting")
                .setCampaign("content 123 launch")
                .setStage("new user")

        }





        popup.setOnMenuItemClickListener {

            when (it.itemId) {

                R.id.profile_edit_profile -> {
                    activity.subFm.beginTransaction()
                        .add(R.id.feed_subcontents_frame_container, activity.editProfileFragment, "editProfileFragment")
                        .addToBackStack("editProfileFragment")
                        .commit()
                    activity.subActive = activity.editProfileFragment

                    activity.switchVisibility(1)

                    return@setOnMenuItemClickListener true
                }


                R.id.profile_edit_interests -> {
                    activity.subFm.beginTransaction().add(
                        R.id.feed_subcontents_frame_container,
                        activity.editInterestsFragment,
                        "editInterestsFragment"
                    ).addToBackStack("editInterestsFragment")
                        .commit()
                    activity.subActive = activity.editInterestsFragment
                    activity.switchVisibility(1)
                    return@setOnMenuItemClickListener true
                }

                R.id.profile_resign_admin -> {

                    FirebaseFirestore.getInstance().collection("communities").document(currentCommunity.id)
                        .update("admins", FieldValue.arrayRemove(userProfile.uid))

                    currentCommunity.admins.remove(userProfile.uid)
                    currentCommunityViewModel.currentCommunity.postValue(currentCommunity)
                    return@setOnMenuItemClickListener true
                }


/*
                R.id.profile_invite_a_friend -> {

                    val ss =
                        ShareSheetStyle(this.context!!, "Check out my profile", "Follow me around the world")
                            .setCopyUrlStyle(
                                resources.getDrawable(android.R.drawable.ic_menu_send),
                                "Copy",
                                "Added to clipboard"
                            )
                            .setMoreOptionStyle(resources.getDrawable(android.R.drawable.ic_menu_search), "Show more")
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK_MESSENGER)
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP)
                            .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER)
                            .setAsFullWidthStyle(true)
                            .setSharingTitle("Share With")

                    buo.showShareSheet(activity, lp, ss, object : Branch.BranchLinkShareListener {
                        override fun onShareLinkDialogLaunched() {}
                        override fun onShareLinkDialogDismissed() {}
                        override fun onLinkShareResponse(
                            sharedLink: String,
                            sharedChannel: String,
                            error: BranchError
                        ) {
                        }

                        override fun onChannelSelected(channelName: String) {
                            firebaseAnalytics.logEvent("profile_shared_$channelName", null)
                        }
                    })
                    return@setOnMenuItemClickListener true
                }
*/

                else -> return@setOnMenuItemClickListener false
            }
        }

        shoutsBtn.setOnClickListener {
            changeGalleryFeed("shouts")
        }

        questionsBtn.setOnClickListener {
            changeGalleryFeed("questions")
        }

        answersBtn.setOnClickListener {
            changeGalleryFeed("answers")
        }


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

    private fun getCount(ref: DocumentReference): Task<Int> {
        // Sum the count of each shard in the subcollection
        return ref.collection("shards").get()
            .continueWith { task ->
                var count = 0
                for (snap in task.result!!) {
                    val shard = snap.toObject(Shard::class.java)
                    count += shard.count
                }
                count
            }
    }


    private fun changeGalleryFeed(source: String) {

        when (source) {

            "shouts" -> {
                shoutsBtn.setTextColor(resources.getColor(R.color.gray700))
                questionsBtn.setTextColor(resources.getColor(R.color.gray300))
                answersBtn.setTextColor(resources.getColor(R.color.gray300))
                galleryShoutsRecycler.visibility = View.VISIBLE
                galleryQuestionsRecycler.visibility = View.GONE
                galleryAnswersRecycler.visibility = View.GONE
            }

            "questions" -> {
                shoutsBtn.setTextColor(resources.getColor(R.color.gray300))
                questionsBtn.setTextColor(resources.getColor(R.color.gray700))
                answersBtn.setTextColor(resources.getColor(R.color.gray300))
                galleryShoutsRecycler.visibility = View.GONE
                galleryQuestionsRecycler.visibility = View.VISIBLE
                galleryAnswersRecycler.visibility = View.GONE
            }

            "answers" -> {
                shoutsBtn.setTextColor(resources.getColor(R.color.gray300))
                questionsBtn.setTextColor(resources.getColor(R.color.gray300))
                answersBtn.setTextColor(resources.getColor(R.color.gray700))
                galleryShoutsRecycler.visibility = View.GONE
                galleryQuestionsRecycler.visibility = View.GONE
                galleryAnswersRecycler.visibility = View.VISIBLE
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
        db.collection("questions").whereEqualTo("author_ID", userProfile.uid)
            .orderBy("timestamp").get()
            .addOnSuccessListener {

                for (document in it) {
                    val questionObject = document.toObject(Question::class.java)
                    galleryQuestionsAdapter.add(SingleBoardRow(questionObject, activity as MainActivity))
                }
            }
    }

    private fun listenToAnswers() {
        galleryAnswersAdapter.clear()

        db.collection("answers").whereEqualTo("author_ID", userProfile.uid)
            .orderBy("timestamp").get()
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

        FirebaseFirestore.getInstance().collection("contact_details").document(userProfile.uid).get()
            .addOnSuccessListener {

                val doc = it.data
                if (doc != null) {

                    val list = doc as Map<String, Map<String, Any>>

                    if (doc.isEmpty()) {
                        contactDetailsIconsRecycler.visibility = View.GONE
                    } else {
                        contactDetailsIconsRecycler.visibility = View.VISIBLE
                    }


                    var totalAmountOfFields = 0
                    for (field in userProfile.contact_info) {
                        if (list.containsKey(field)) {
                            totalAmountOfFields++
                        }
                    }

                    contactDetailsIconsRecycler.layoutManager = when (totalAmountOfFields) {
                        1 -> GridLayoutManager(this.context, 1)
                        2 -> GridLayoutManager(this.context, 2)
                        3 -> GridLayoutManager(this.context, 3)
                        4 -> GridLayoutManager(this.context, 4)
                        5 -> GridLayoutManager(this.context, 5)
                        else -> GridLayoutManager(this.context, 6)
                    }

                    for (field in userProfile.contact_info) {
                        if (list.containsKey(field)) {
                            contactDetailsIconsAdapter.add(
                                SingleContactDetailsIcon(
                                    list[field]?.getValue("case").toString().toInt(),
                                    list[field]?.getValue("info").toString(),
                                    activity as MainActivity
                                )
                            )
                        }
                    }
                }
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


    companion object {
        fun newInstance(): ProfileCurrentUserFragment = ProfileCurrentUserFragment()
    }
}

