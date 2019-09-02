package com.republicera.groupieAdapters

import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
import com.republicera.viewModels.CurrentCommunityIdViewModel
import com.republicera.viewModels.CurrentCommunityViewModel
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.SharingHelper
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import io.branch.referral.util.ShareSheetStyle
import kotlinx.android.synthetic.main.community_option_layout.view.*

class SingleCommunityOption(val community: Community, val activity: MainActivity) : Item<ViewHolder>() {

    private lateinit var buo: BranchUniversalObject
    private lateinit var lp: LinkProperties

    lateinit var communityViewModel : CurrentCommunityIdViewModel

    override fun getLayout(): Int {
        return R.layout.community_option_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        activity.let {
            communityViewModel = ViewModelProviders.of(it).get(CurrentCommunityIdViewModel::class.java)
        }

        val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.root.context!!)

        val title = viewHolder.itemView.community_option_title
        val description = viewHolder.itemView.community_option_description
        val memberCount = viewHolder.itemView.community_option_members_count
        val menuButton = viewHolder.itemView.community_option_share
        val image = viewHolder.itemView.community_option_image

        title.text = community.title
        description.text = community.description
        memberCount.text = "${community.members}"
        Glide.with(activity).load(community.image).into(image)

        buo = BranchUniversalObject()
            .setCanonicalIdentifier(community.id)
            .setTitle(community.title)
            .setContentDescription(community.description)
            .setContentImageUrl(community.image)//this is just a bad temporary solution
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setContentMetadata(ContentMetadata().addCustomMetadata("type", "community"))

        lp = LinkProperties()

        buo.listOnGoogleSearch(viewHolder.root.context)

        val popup = PopupMenu(viewHolder.root.context, menuButton)
        popup.inflate(R.menu.community)

        menuButton.setOnClickListener {
            popup.show()
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.community_edit -> {

                    communityViewModel.currentCommunity.postValue(community.id)

                    activity.userFm.beginTransaction()
                        .add(
                            R.id.user_home_frame_container,
                            activity.editCommunityFragment,
                            "editCommunityFragment"
                        )
                        .addToBackStack("editCommunityFragment")
                        .commit()

                    activity.userActive = activity.editCommunityFragment

                    true
                }

                R.id.community_share -> {
                    val ss = ShareSheetStyle(activity, "Republic invite", "Join me in this republic.")
                        .setCopyUrlStyle(
                            activity.resources.getDrawable(android.R.drawable.ic_menu_send),
                            "Copy",
                            "Added to clipboard"
                        )
                        .setMoreOptionStyle(
                            activity.resources.getDrawable(android.R.drawable.ic_menu_search),
                            "Show more"
                        )
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
                            firebaseAnalytics.logEvent("community_shared_$channelName", null)
                        }
                    })
                    true
                }

                R.id.community_report -> {
                    true
                }

                else -> {
                    true
                }
            }
        }

    }
}