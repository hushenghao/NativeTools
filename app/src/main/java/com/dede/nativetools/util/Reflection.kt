@file:Suppress("NOTHING_TO_INLINE")

package com.dede.nativetools.util

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


@Throws(NoSuchMethodException::class, SecurityException::class)
fun <T> Class<T>.method(name: String, vararg parameterTypes: Class<*>): Method {
    return this.getMethod(name, *parameterTypes).apply {
        isAccessible = true
    }
}

@Throws(NoSuchMethodException::class, SecurityException::class)
fun <T> Class<T>.declaredMethod(name: String, vararg parameterTypes: Class<*>): Method {
    return this.getDeclaredMethod(name, *parameterTypes).apply {
        isAccessible = true
    }
}

@Throws(IllegalAccessException::class,
    IllegalArgumentException::class,
    InvocationTargetException::class)
@Suppress("UNCHECKED_CAST")
inline fun <T> Method.invokeWithReturn(obj: Any?, vararg parameters: Any?): T {
    return this.invoke(obj, *parameters) as T
}

@Throws(NoSuchFieldException::class, SecurityException::class)
fun <T> Class<T>.field(name: String): Field {
    return this.getField(name).apply { isAccessible = true }
}

@Throws(NoSuchFieldException::class, SecurityException::class)
fun <T> Class<T>.declaredField(name: String): Field {
    return this.getDeclaredField(name).apply { isAccessible = true }
}

@Throws(IllegalArgumentException::class, IllegalAccessException::class)
@Suppress("UNCHECKED_CAST")
inline fun <T> Field.getNotnull(obj: Any?): T {
    return this.get(obj) as T
}

@Throws(IllegalArgumentException::class, IllegalAccessException::class)
@Suppress("UNCHECKED_CAST")
inline fun <T> Field.getNullable(obj: Any?): T? {
    return this.get(obj) as? T
}

@Throws(NoSuchMethodException::class, SecurityException::class)
fun <T> Class<T>.declaredConstructor(vararg parameterTypes: Class<*>): Constructor<T> {
    return this.getDeclaredConstructor(*parameterTypes).apply {
        isAccessible = true
    }
}

inline fun <reified T : Annotation> Any.annotation(): T? {
    return this.javaClass.getAnnotation(T::class.java)
}