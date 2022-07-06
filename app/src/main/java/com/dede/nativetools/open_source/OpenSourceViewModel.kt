package com.dede.nativetools.open_source

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dede.nativetools.network.loading

class OpenSourceViewModel : ViewModel() {

    private val openSourceRepository = OpenSourceRepository()

    val openSourceList: LiveData<Result<List<OpenSource>>> =
        liveData(viewModelScope.coroutineContext) {
            emit(Result.loading())
            try {
                emit(Result.success(openSourceRepository.getOpenSourceList()))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
}