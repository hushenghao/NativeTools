package com.dede.nativetools.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 关于页，保存followCount
 *
 * @author hsh
 * @since 2021/8/13 9:49 上午
 */
class AboutViewModel : ViewModel() {

    private val _followCount = MutableLiveData(0)

    val followCount: LiveData<Int> = _followCount

    fun addFollowCount() {
        val count = _followCount.value ?: 0
        _followCount.value = count + 1
    }
}
