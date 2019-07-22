package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FollowersViewModel : ViewModel() {
    var followers = MutableLiveData<MutableList<String>>()

}