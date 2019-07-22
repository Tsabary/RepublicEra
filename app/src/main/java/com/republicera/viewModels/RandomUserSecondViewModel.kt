package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.republicera.models.CommunityProfile

class RandomUserSecondViewModel: ViewModel()  {
    var randomUserObject = MutableLiveData<CommunityProfile>()

}