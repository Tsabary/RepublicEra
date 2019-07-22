package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FollowedAccountsViewModel : ViewModel() {
    var followedAccounts = MutableLiveData<MutableList<String>>()

}