package com.dede.nativetools.donate

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dede.nativetools.network.loading

/**
 * Created by shhu on 2022/7/1 13:50.
 *
 * @since 2022/7/1
 */
class DonateListViewModel : ViewModel() {

    private val repository = DonateListRepository()

    val donateList: LiveData<Result<List<DonateInfo>>> = liveData(viewModelScope.coroutineContext) {
        emit(Result.loading())
        try {
            emit(Result.success(repository.getDonateList()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}