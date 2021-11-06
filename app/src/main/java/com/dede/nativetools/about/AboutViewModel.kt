package com.dede.nativetools.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 关于页，保存followCount
 *
 * @author hsh
 * @since 2021/8/13 9:49 上午
 */
class AboutViewModel : ViewModel() {

    val followCount = MutableLiveData(0)

    fun addFollowCount() {
        val count = followCount.value ?: 0
        followCount.value = count + 1
    }

}