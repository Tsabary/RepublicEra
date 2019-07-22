package com.republicera.viewModels

import androidx.lifecycle.ViewModel
import com.republicera.models.SingleTagForList

class TagsViewModel: ViewModel()  {
    var tagList = mutableListOf<SingleTagForList>()
}