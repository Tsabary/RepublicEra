package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.republicera.models.Answer

class AnswerViewModel: ViewModel() {
    var sharedAnswerObject = MutableLiveData<Answer>()
}