package com.republicera.pagerAdapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.republicera.fragments.LoginFragment
import com.republicera.fragments.RegisterFragment


class RegisterLoginPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val tabTitles = arrayOf("Sign up", "Login")


    override fun getItem(p0: Int): Fragment {

        return when (p0) {
            0 -> RegisterFragment.newInstance()
            1 -> LoginFragment.newInstance()
            else -> RegisterFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }


    override fun getCount(): Int {
        return 2
    }
}