package com.republicera.fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.github.nisrulz.sensey.Sensey
import com.republicera.MainActivity
import com.republicera.R
import com.republicera.pagerAdapters.ChooseCommunityPagerAdapter
import com.republicera.viewModels.CurrentCommunityViewModel
import kotlinx.android.synthetic.main.fragment_choose_community.*

class ChooseCommunityFragment : Fragment() {

    private lateinit var communityViewModel: CurrentCommunityViewModel
    lateinit var viewPager: ViewPager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = choose_community_viewpager
        viewPager.adapter = ChooseCommunityPagerAdapter(childFragmentManager)

        val activity = activity as MainActivity
        val title = choose_community_title

        activity.let {
            communityViewModel = ViewModelProviders.of(it).get(CurrentCommunityViewModel::class.java)
            communityViewModel.currentCommunity.observe(
                activity,
                Observer { communityName ->
                    title.text = communityName.title
                })
        }



        val tabLayout = choose_community_tab_layout
        tabLayout.setupWithViewPager(viewPager)
    }


}
