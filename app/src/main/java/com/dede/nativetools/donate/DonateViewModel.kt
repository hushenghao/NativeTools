package com.dede.nativetools.donate

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class DonateViewModel : ViewModel() {

    private val repository = DonateRepository()

    val paymentList: LiveData<List<Payment>> = liveData {
        emit(repository.getPaymentList())
    }

}