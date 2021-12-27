package com.dede.nativetools.open_source

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class OpenSourceViewModel : ViewModel() {

    private val openSourceRepository = OpenSourceRepository()

    val openSourceList: LiveData<List<OpenSource>> = liveData {
        emit(openSourceRepository.getOpenSourceList())
    }
}