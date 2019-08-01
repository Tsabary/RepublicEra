package com.republicera.groupieAdapters

import com.google.firebase.analytics.FirebaseAnalytics
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.models.Community
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

class SingleCommunityOption(val community: Community, val activity : MainActivity) : Item<ViewHolder>() {

    private lateinit var buo: BranchUniversalObject
    private lateinit var lp: LinkProperties

    override fun getLayout(): Int {
        return R.layout.community_option_layout
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val firebaseAnalytics = FirebaseAnalytics.getInstance(viewHolder.root.context!!)

        val title = viewHolder.itemView.community_option_title
        val description = viewHolder.itemView.community_option_description
        val memberCount = viewHolder.itemView.community_option_members_count
        val share= viewHolder.itemView.community_share

        title.text = community.title
        description.text = community.description
        memberCount.text = "${community.members}"


        buo = BranchUniversalObject()
            .setCanonicalIdentifier(community.id)
            .setTitle(community.title)
            .setContentDescription("")
//            .setContentImageUrl("https://img1.10bestmedia.com/Image/Photos/352450/GettyImages-913753556_55_660x440.jpg")
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setContentMetadata(ContentMetadata().addCustomMetadata("type", "community"))

        lp = LinkProperties()


        share.setOnClickListener {
            val ss = ShareSheetStyle(activity, "Republic invite", "Join me in this republic.")
                .setCopyUrlStyle(activity.resources.getDrawable(android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
                .setMoreOptionStyle(activity.resources.getDrawable(android.R.drawable.ic_menu_search), "Show more")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK_MESSENGER)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER)
                .setAsFullWidthStyle(true)
                .setSharingTitle("Share With")

            buo.showShareSheet(activity, lp, ss, object : Branch.BranchLinkShareListener {
                override fun onShareLinkDialogLaunched() {}
                override fun onShareLinkDialogDismissed() {}
                override fun onLinkShareResponse(sharedLink: String, sharedChannel: String, error: BranchError) {}
                override fun onChannelSelected(channelName: String) {
                    firebaseAnalytics.logEvent("community_shared_$channelName", null)
                }
            })
        }

    }
}