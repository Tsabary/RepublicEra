package com.republicera.pagerAdapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.republicera.fragments.ChooseCommunityFragment
import com.republicera.fragments.ExploreCommunitiesFragment
import com.republicera.fragments.MyCommunitiesFragment

class ChooseCommunityPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val tabTitles = arrayOf("My communities", "Explore")

    override fun getItem(p0: Int): Fragment {


        return when (p0) {
            0 -> MyCommunitiesFragment.newInstance()
            1 -> ExploreCommunitiesFragment.newInstance()

            else -> MyCommunitiesFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }


    override fun getCount(): Int {
        return 2
    }

    companion object {
        fun newInstance(): ChooseCommunityFragment = ChooseCommunityFragment()
    }
}