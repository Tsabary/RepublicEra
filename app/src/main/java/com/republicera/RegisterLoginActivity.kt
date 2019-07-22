package com.republicera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.republicera.pagerAdapters.RegisterLoginPagerAdapter
import kotlinx.android.synthetic.main.activity_register_login.*

class RegisterLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_login)

        FirebaseApp.initializeApp(this)


        val viewPager = register_login_viewpager
        viewPager.adapter = RegisterLoginPagerAdapter(supportFragmentManager)
        val tabLayout = register_login_tablayout
        tabLayout.setupWithViewPager(viewPager)
    }
}
