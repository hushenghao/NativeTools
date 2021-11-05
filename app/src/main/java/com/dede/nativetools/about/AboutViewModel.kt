package com.dede.nativetools.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dede.nativetools.about.AboutFragment.Companion.MAX_FOLLOW_COUNT

/**
 * 关于页，保存followCount
 *
 * @author hsh
 * @since 2021/8/13 9:49 上午
 */
class AboutViewModel : ViewModel() {

    val followCount = MutableLiveData(0)

    fun appendFollowCount() {
        var count = followCount.value ?: 0
        if (count++ > MAX_FOLLOW_COUNT) {
            return
        }
        followCount.value = count
    }

}