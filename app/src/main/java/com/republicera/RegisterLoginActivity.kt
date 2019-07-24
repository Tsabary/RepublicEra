package com.republicera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.republicera.fragments.NewRegisterFragment
import com.republicera.fragments.RegisterFragment
import com.republicera.pagerAdapters.RegisterLoginPagerAdapter
import kotlinx.android.synthetic.main.activity_register_login.*

class RegisterLoginActivity : AppCompatActivity() {

    val fm = supportFragmentManager
    var active: Fragment? = null

    lateinit var registerFragment: NewRegisterFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_login)

        FirebaseApp.initializeApp(this)

        registerFragment = NewRegisterFragment()
        fm.beginTransaction()
            .add(R.id.register_login_frame_container, registerFragment, "registerFragment")
            .addToBackStack("registerFragment")
            .commit()
        active = registerFragment


//
//        val viewPager = register_login_viewpager
//        viewPager.adapter = RegisterLoginPagerAdapter(supportFragmentManager)
//        val tabLayout = register_login_tablayout
//        tabLayout.setupWithViewPager(viewPager)
    }
}
