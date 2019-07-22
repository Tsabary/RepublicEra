package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.republicera.models.Question

class QuestionViewModel: ViewModel()  {
    var questionObject = MutableLiveData<Question>()
}