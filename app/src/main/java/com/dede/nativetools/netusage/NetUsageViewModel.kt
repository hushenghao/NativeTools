package com.dede.nativetools.netusage

import android.content.Context
import androidx.lifecycle.*


class NetUsageViewModel : ViewModel() {

    private val netUsageRepository = NetUsageRepository()

    private val coordinateMax: MutableLiveData<Long> = MutableLiveData()

    fun getCoordinateMax(): LiveData<Long> {
        return coordinateMax
    }

    fun getNetUsage(context: Context): LiveData<List<NetUsage>> {
        return liveData(viewModelScope.coroutineContext) {
            val list = netUsageRepository.loadNetUsage(context)
            val max = netUsageRepository.calculateMax(list)
            for (netUsage in list) {
                netUsage.calculateProgress(max)
            }
            coordinateMax.value = max
            emit(list)
        }
    }

}