package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnswerImagesViewModel : ViewModel(){
    var imageList = MutableLiveData<MutableList<String>>()
}