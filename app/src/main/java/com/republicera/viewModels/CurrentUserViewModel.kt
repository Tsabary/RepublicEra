package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.republicera.models.User

class CurrentUserViewModel : ViewModel() {
    var currentUserObject = MutableLiveData<User>()
}