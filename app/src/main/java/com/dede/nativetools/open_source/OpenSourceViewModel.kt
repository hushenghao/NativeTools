package com.dede.nativetools.open_source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OpenSourceViewModel : ViewModel() {

    private val _openSourceList = MutableLiveData<List<OpenSource>>()

    val openSourceList: LiveData<List<OpenSource>> = _openSourceList

    private val openSourceRepository = OpenSourceRepository()

    init {
        _openSourceList.value = openSourceRepository.getOpenSourceList()
    }
}