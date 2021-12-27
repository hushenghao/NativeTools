@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.dede.nativetools.util

import android.text.TextUtils
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@kotlin.internal.InlineOnly
inline fun String?.isEmpty(): Boolean {
    contract {
        returns(false) implies (this@isEmpty != null)
    }
    return TextUtils.isEmpty(this)
}

@OptIn(ExperimentalContracts::class)
@kotlin.internal.InlineOnly
inline fun String?.isNotEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNotEmpty != null)
    }
    return !TextUtils.isEmpty(this)
}

fun Pair<String, String>.splicing(): String = this.first + this.second
