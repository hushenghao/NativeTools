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

    var animatored = false

    val followCount = MutableLiveData(0)

    fun setFollowCount(count: Int) {
        followCount.value = count
    }

}