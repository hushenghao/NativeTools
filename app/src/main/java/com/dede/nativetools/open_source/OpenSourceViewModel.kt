package com.dede.nativetools.open_source

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope

class OpenSourceViewModel : ViewModel() {

    private val openSourceRepository = OpenSourceRepository()

    val openSourceList: LiveData<List<OpenSource>> = liveData(viewModelScope.coroutineContext) {
        emit(openSourceRepository.getOpenSourceList())
    }
}