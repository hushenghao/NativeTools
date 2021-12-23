package com.dede.nativetools.donate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DonateViewModel : ViewModel() {

    private val repository = DonateRepository()

    private val _paymentList = MutableLiveData<List<Payment>>()

    val paymentList: LiveData<List<Payment>> = _paymentList

    init {
        _paymentList.value = repository.getPaymentList()
    }

}