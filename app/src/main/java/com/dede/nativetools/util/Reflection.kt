package com.dede.nativetools.util

import java.lang.reflect.Field
import java.lang.reflect.Method


fun <T> Class<T>.method(name: String, vararg parameterTypes: Class<*>): Method {
    return this.getMethod(name, *parameterTypes).apply {
        isAccessible = true
    }
}

fun <T> Class<T>.declaredMethod(name: String, vararg parameterTypes: Class<*>): Method {
    return this.getDeclaredMethod(name, *parameterTypes).apply {
        isAccessible = true
    }
}

fun <T> Class<T>.declaredField(name: String): Field {
    return this.getDeclaredField(name).apply { isAccessible = true }
}