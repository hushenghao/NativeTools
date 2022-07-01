package com.dede.nativetools.donate

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

/**
 * Created by shhu on 2022/7/1 13:50.
 *
 * @since 2022/7/1
 */
class DonateListViewModel : ViewModel() {

    private val repository = DonateListRepository()

    val donateList: LiveData<List<DonateInfo>> = liveData {
        emit(repository.getDonateList())
    }
}