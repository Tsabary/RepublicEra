package com.republicera.fragments


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.republicera.MainActivity
import com.republicera.groupieAdapters.SingleShout
import com.republicera.interfaces.GeneralMethods
import com.republicera.models.Community
import com.republicera.models.CommunityProfile
import com.republicera.models.Shout
import com.republicera.viewModels.CurrentCommunityProfileViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.republicera.viewModels.FollowersViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_shouts.*
import kotlinx.android.synthetic.main.shout_bottom_sheet.*
import kotlinx.android.synthetic.main.shouts_toolbar.*
import me.echodev.resizer.Resizer
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*
import com.republicera.R


class ShoutsFragment : Fragment(), GeneralMethods {

    lateinit var db: DocumentReference

    private lateinit var currentCommunityViewModel: CurrentCommunityViewModel
    private lateinit var followersViewModel: FollowersViewModel
    lateinit var followersList: MutableList<String>


    lateinit var currentUser: CommunityProfile
    private lateinit var currentCommunity: Community

    private lateinit var freshMessage: TextView
    lateinit var feedStateTitle: TextView

    lateinit var allSwipeRefresh: SwipeRefreshLayout
    lateinit var followingSwipeRefresh: SwipeRefreshLayout

    private lateinit var shoutsFollowingRecycler: RecyclerView
    val shoutsFollowingAdapter = GroupAdapter<ViewHolder>()

    private lateinit var shoutsAllRecycler: RecyclerView
    val shoutsAllAdapter = GroupAdapter<ViewHolder>()

    lateinit var followingLastVisible: DocumentSnapshot
    var followingIsScrolling = false
    var followingIsLastItemReached = false

    lateinit var allLastVisible: DocumentSnapshot
    var allIsScrolling = false
    var allIsLastItemReached = false

    lateinit var shoutButton: TextView
    lateinit var shoutAddImage: ImageButton
    lateinit var shoutRemoveImage: TextView
    lateinit var shoutCard: CardView
    lateinit var shoutImage: ImageView
    lateinit var shoutInput: EditText
    lateinit var shoutProgress: ProgressBar


    var selectedPhotoUri: Uri? = null

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shouts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity

        val joinQuorumContainer = shouts_join_quorum_cotainer

        activity.let {
            currentUser = ViewModelProviders.of(it).get(CurrentCommunityProfileViewModel::class.java)
                .currentCommunityProfileObject
            followersViewModel = ViewModelProviders.of(it).get(FollowersViewModel::class.java)
            followersViewModel.followers.observe(activity, Observer { list ->
                followersList = list
            })
            currentCommunityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)

            currentCommunityViewModel.currentCommunity.observe(
                activity,
                Observer { communityName ->
                    currentCommunity = communityName
                    db = FirebaseFirestore.getInstance().collection("communities_data").document(currentCommunity.id)
                    if (currentCommunity.admins.size < currentCommunity.members.toDouble() / 5 && !currentCommunity.admins.contains(
                            currentUser.uid
                        )
                    ) {
                        joinQuorumContainer.visibility = View.VISIBLE
                    } else {
                        joinQuorumContainer.visibility = View.GONE
                    }
                })
        }

        freshMessage = shouts_fresh_message
        feedStateTitle = shouts_feed_title
        feedStateTitle.tag = "following"
        feedStateTitle.setOnClickListener {
            switchRecyclers()
        }

        val joinQuorum = shouts_join_quorum
        joinQuorum.setOnClickListener {


            AlertDialog.Builder(context)
                .setTitle("Join Quorum")
                .setMessage("Quorums are the governments of our communities")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Join") { _, _ ->
                    FirebaseFirestore.getInstance().collection("communities").document(currentCommunity.id).update(
                        "admins", FieldValue.arrayUnion(currentUser.uid)
                    ).addOnSuccessListener {
                        joinQuorumContainer.visibility = View.GONE
                        currentCommunity.admins.add(currentUser.uid)
                        currentCommunityViewModel.currentCommunity.postValue(currentCommunity)
                    }
                }
                .show()
        }

        val savedShoutsButton = shouts_toolbar_saved_questions_icon
        savedShoutsButton.setOnClickListener {
            goToSavedShouts(activity)
        }

        val shoutsNotificationsIcon = shouts_toolbar_notifications_icon
        shoutsNotificationsIcon.setOnClickListener {
            goToNotifications(activity)
        }

        followingSwipeRefresh = shouts_following_swipe_refresh
        shoutsFollowingRecycler = shouts_following_recycler
        shoutsFollowingRecycler.adapter = shoutsFollowingAdapter
        shoutsFollowingRecycler.layoutManager = LinearLayoutManager(this.context)

        allSwipeRefresh = shouts_all_swipe_refresh
        shoutsAllRecycler = shouts_all_recycler
        shoutsAllRecycler.adapter = shoutsAllAdapter
        shoutsAllRecycler.layoutManager = LinearLayoutManager(this.context)

        listenToAllShouts()
        listenToFollowingShouts()

        followingSwipeRefresh.setOnRefreshListener {
            listenToFollowingShouts()
            followingSwipeRefresh.isRefreshing = false
        }

        allSwipeRefresh.setOnRefreshListener {
            listenToAllShouts()
            allSwipeRefresh.isRefreshing = false
        }

        shoutButton = shouts_bottom_sheet_shout_button
        shoutAddImage = shouts_bottom_sheet_add_image
        shoutRemoveImage = shouts_bottom_sheet_remove_image
        shoutCard = shouts_bottom_sheet_card
        shoutImage = shouts_bottom_sheet_image
        shoutInput = shouts_bottom_sheet_input
        shoutProgress = shouts_bottom_sheet_progress

        shoutInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    shoutButton.visibility = View.GONE
                    shoutAddImage.visibility = View.GONE
                } else {
                    shoutButton.visibility = View.VISIBLE
                    shoutAddImage.visibility = View.VISIBLE
                }
            }

        })


        shoutButton.setOnClickListener {

            val shoutDoc = db.collection("shouts").document()
            if (selectedPhotoUri != null) {
                uploadImage(shoutDoc)
            } else {
                shout("", shoutDoc)
            }
        }

        shoutAddImage.setOnClickListener {

            if (hasNoPermissions()) {
                requestPermission()
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "image/*"
                startActivityForResult(intent, 1)
            }
        }

        shoutRemoveImage.setOnClickListener {
            selectedPhotoUri = null
            shoutCard.visibility = View.GONE
            shoutRemoveImage.visibility = View.GONE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            selectedPhotoUri = data.data

            if (selectedPhotoUri != null) {
                shoutCard.visibility = View.VISIBLE
                Glide.with(this.context!!).load(selectedPhotoUri.toString()).into(shoutImage)
            }
        }
    }

    private fun uploadImage(shoutDoc: DocumentReference) {

        shoutProgress.visibility = View.VISIBLE

        val refImage =
            FirebaseStorage.getInstance().getReference("/images/${currentCommunity.id}/shouts/${shoutDoc.id}")
        val randomName = UUID.randomUUID().toString()


        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Dere"

        val imageFile: File = File.createTempFile("ImageFile", "temporary")


        val myInputStream =
            (activity as MainActivity).contentResolver.openInputStream(Uri.parse(selectedPhotoUri.toString()))
        FileUtils.copyInputStreamToFile(myInputStream, imageFile)


        refImage.putFile(
            Uri.fromFile(
                Resizer(this.context)
                    .setTargetLength(1200)
                    .setQuality(100)
                    .setOutputFormat("PNG")
                    .setOutputFilename(randomName)
                    .setOutputDirPath(path)
                    .setSourceImage(imageFile)
                    .resizedFile
            )
        ).addOnSuccessListener {


            refImage.downloadUrl.addOnSuccessListener { bigUri ->

                val imageUri = bigUri.toString()

                val fileBig = File(bigUri.path)
                if (fileBig.exists()) {
                    fileBig.delete()
                }
                shout(imageUri, shoutDoc)
            }
        }

    }

    private fun shout(image: String, shoutDoc: DocumentReference) {
        val shout = Shout(
            shoutDoc.id,
            currentUser.uid,
            currentUser.name,
            currentUser.image,
            shoutInput.text.toString(),
            if (image.isNotEmpty()) {
                listOf(image)
            } else {
                listOf()
            },
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            listOf(),
            true,
            mutableListOf(),
            0,
            followersList
        )

        shoutDoc.set(shout).addOnSuccessListener {
            shoutsAllAdapter.add(0, SingleShout(shout, currentUser, activity as MainActivity))
            shoutInput.text.clear()
            shoutInput.clearFocus()
            closeKeyboard(activity as MainActivity)
            shoutsFollowingRecycler.smoothScrollToPosition(0)
            shoutButton.visibility = View.GONE
            shoutAddImage.visibility = View.GONE
            shoutCard.visibility = View.GONE
            shoutProgress.visibility = View.GONE
            selectedPhotoUri = null

        }
    }

    private fun listenToAllShouts() {
        shoutsAllAdapter.clear()

        db.collection("shouts").whereEqualTo("visible", true).orderBy("last_interaction", Query.Direction.DESCENDING)
            .limit(25)
            .get().addOnSuccessListener {
                if (it.size() == 0) {
                    freshMessage.visibility = View.VISIBLE
                } else {
                    freshMessage.visibility = View.GONE


                    for (oneShout in it) {
                        val shoutDoc = oneShout.toObject(Shout::class.java)
                        shoutsAllAdapter.add(SingleShout(shoutDoc, currentUser, activity as MainActivity))
                    }
                    shoutsAllAdapter.notifyDataSetChanged()

                    allLastVisible = it.documents[it.size() - 1]
                }
            }

        shoutsAllRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    allIsScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val thisLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                val firstVisibleItem = thisLayoutManager.findFirstCompletelyVisibleItemPosition()
                val visibleItemCount = thisLayoutManager.childCount
                val totalItemCount = thisLayoutManager.itemCount

                allSwipeRefresh.isEnabled = firstVisibleItem == 0

                val countCheck = firstVisibleItem + visibleItemCount == totalItemCount

                if (allIsScrolling && countCheck && !allIsLastItemReached) {
                    allIsScrolling = false

                    db.collection("shouts").whereEqualTo("visible", true)
                        .orderBy("last_interaction", Query.Direction.DESCENDING).startAfter(allLastVisible)
                        .limit(25).get()
                        .addOnSuccessListener { querySnapshot ->

                            if (!querySnapshot.isEmpty) {

                                for (oneShout in querySnapshot) {
                                    val shoutDoc = oneShout.toObject(Shout::class.java)
                                    shoutsAllAdapter.add(
                                        SingleShout(
                                            shoutDoc,
                                            currentUser,
                                            activity as MainActivity
                                        )
                                    )
                                }

                                shoutsAllAdapter.notifyDataSetChanged()

                                allLastVisible = querySnapshot.documents[querySnapshot.size() - 1]

                                if (querySnapshot.size() < 25) {
                                    allIsLastItemReached = true
                                }
                            } else {
                                allIsLastItemReached = true
                            }
                        }
                }
            }
        })
    }


    private fun listenToFollowingShouts() {
        shoutsFollowingAdapter.clear()

        db.collection("shouts").whereArrayContains("xfollowers", currentUser.uid).whereEqualTo("visible", true)
            .orderBy("last_interaction", Query.Direction.DESCENDING).limit(25)
            .get().addOnSuccessListener {
                if (it.size() == 0) {
                    freshMessage.visibility = View.VISIBLE
                } else {
                    freshMessage.visibility = View.GONE


                    for (oneShout in it) {
                        val shoutDoc = oneShout.toObject(Shout::class.java)
                        shoutsFollowingAdapter.add(SingleShout(shoutDoc, currentUser, activity as MainActivity))
                    }
                    shoutsFollowingAdapter.notifyDataSetChanged()

                    followingLastVisible = it.documents[it.size() - 1]
                }
            }

        shoutsFollowingRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    followingIsScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val thisLayoutManager = recyclerView.layoutManager as LinearLayoutManager

                val firstVisibleItem = thisLayoutManager.findFirstCompletelyVisibleItemPosition()
                val visibleItemCount = thisLayoutManager.childCount
                val totalItemCount = thisLayoutManager.itemCount

                followingSwipeRefresh.isEnabled = firstVisibleItem == 0

                val countCheck = firstVisibleItem + visibleItemCount == totalItemCount

                if (followingIsScrolling && countCheck && !followingIsLastItemReached) {
                    followingIsScrolling = false

                    db.collection("shouts").whereArrayContains("xfollowers", currentUser.uid)
                        .whereEqualTo("visible", true)
                        .orderBy("last_interaction", Query.Direction.DESCENDING).startAfter(followingLastVisible)
                        .limit(25).get()
                        .addOnSuccessListener { querySnapshot ->

                            if (!querySnapshot.isEmpty) {

                                for (oneShout in querySnapshot) {
                                    val shoutDoc = oneShout.toObject(Shout::class.java)
                                    shoutsFollowingAdapter.add(
                                        SingleShout(
                                            shoutDoc,
                                            currentUser,
                                            activity as MainActivity
                                        )
                                    )
                                }

                                shoutsFollowingAdapter.notifyDataSetChanged()

                                followingLastVisible = querySnapshot.documents[querySnapshot.size() - 1]

                                if (querySnapshot.size() < 25) {
                                    followingIsLastItemReached = true
                                }
                            } else {
                                followingIsLastItemReached = true
                            }
                        }
                }
            }
        })
    }

    private fun switchRecyclers() {
        if (feedStateTitle.tag == "following") {
            feedStateTitle.tag = "all"
            feedStateTitle.text = "All"
            allSwipeRefresh.visibility = View.VISIBLE
            followingSwipeRefresh.visibility = View.GONE
        } else {
            feedStateTitle.tag = "following"
            feedStateTitle.text = "Following"
            allSwipeRefresh.visibility = View.GONE
            followingSwipeRefresh.visibility = View.VISIBLE
        }
    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(activity as MainActivity, permissions, 0)
    }

    fun hideShoutButtons() {
        shoutButton.visibility = View.GONE
        shoutCard.visibility = View.GONE
        shoutRemoveImage.visibility = View.GONE
        shoutAddImage.visibility = View.GONE
    }

    private fun goToSavedShouts(activity: MainActivity) {
        activity.subFm.beginTransaction().hide(activity.shoutsNotificationsFragment)
            .show(activity.savedShoutsFragment).commit()
        activity.subActive = activity.savedShoutsFragment
        activity.switchVisibility(1)
//        activity.isBoardNotificationsActive = true
    }

    private fun goToNotifications(activity: MainActivity) {
        activity.subFm.beginTransaction().hide(activity.savedShoutsFragment)
            .show(activity.shoutsNotificationsFragment)
            .commit()
        activity.subActive = activity.shoutsNotificationsFragment

        activity.switchVisibility(1)
//        activity.isBoardNotificationsActive = true
    }
}
