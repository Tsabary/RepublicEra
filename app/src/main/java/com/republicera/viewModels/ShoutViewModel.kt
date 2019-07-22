package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.republicera.models.Shout

class ShoutViewModel: ViewModel()  {
    var shoutObject = MutableLiveData<Shout>()
}