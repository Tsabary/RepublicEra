package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.republicera.models.Community

class CurrentCommunityIdViewModel : ViewModel() {
    var currentCommunity = MutableLiveData<String>()
}