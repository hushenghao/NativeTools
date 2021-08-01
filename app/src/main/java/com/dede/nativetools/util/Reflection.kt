package com.dede.nativetools.util

import java.lang.reflect.Method


fun Class<*>.method(name: String, vararg parameterTypes: Class<*>): Method {
    return this.getMethod(name, *parameterTypes).apply {
        isAccessible = true
    }
}

fun Class<*>.declaredMethod(name: String, vararg parameterTypes: Class<*>): Method {
    return this.getDeclaredMethod(name, *parameterTypes).apply {
        isAccessible = true
    }
}