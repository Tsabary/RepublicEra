package com.republicera.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.republicera.models.ContactInfo

class ContactDetailsViewModel : ViewModel() {
    val contactMap = MutableLiveData<MutableMap<String, ContactInfo>>()
}