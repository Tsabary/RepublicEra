package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InterestsViewModel : ViewModel() {
    var interestList = MutableLiveData<MutableList<String>>()

}