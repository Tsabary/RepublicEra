package com.republicera

import android.app.Application
import io.branch.referral.Branch

class CustomApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()

        // Branch logging for debugging
        Branch.enableDebugMode()

        // Branch object initialization
        Branch.getAutoInstance(this)
    }
}