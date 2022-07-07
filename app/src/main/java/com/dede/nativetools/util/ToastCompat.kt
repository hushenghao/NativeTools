package com.dede.nativetools.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import java.lang.reflect.Method

/**
 * Created by hsh on 2019-09-12 11:39
 *
 * fix 在Android N，显示原生toast抛出BadTokenException而引起的崩溃
 *
 * 日志如下:
 * <pre>
 * android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@5d9e24c is not valid; is your activity running?
 *      at android.view.ViewRootImpl.setView(ViewRootImpl.java:679)
 *      at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:342)
 *      at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:94)
 *      at android.widget.Toast$TN.handleShow(Toast.java:459)
 *      at android.widget.Toast$TN$2.handleMessage(Toast.java:342)
 *      at android.os.Handler.dispatchMessage(Handler.java:102)
 * </pre>
 * 复现步骤:
 * <pre>
 *      Toast.makeText(v.getContext(),"test",Toast.LENGTH_LONG).show();
 *      try {
 *          Thread.sleep(50000);// 阻塞主线程，时间大于toast显示时间，使WindowManager token过期
 *      } catch (InterruptedException e) {
 *      }
 *</pre>
 * 原因:
 * 在Android N，Toast显示和隐藏交由NMS[NotificationManagerService]来处理，NMS最终调用WMS[WindowManagerService]操作屏幕上的View
 * 在toast显示时会创建当前toast所对应的token，用于和WMS交互
 * 在toast隐藏后会移除token，此时token算超时过期.
 * 如果显示Toast的过程中碰到主线程阻塞（手机卡顿等原因）
 * 阻塞过久就会碰到正常显示的toast的token过期，等到显示的时候token已经过期，所以会抛出这个异常.
 *
 * 在Android N以后，Toast隐藏后加了11s[NotificationManagerService.FINISH_TOKEN_TIMEOUT]的延迟，然后再移除token，同时在[Toast.TN.handleShow]内添加了try-catch包裹.
 * @see [NotificationManagerService.cancelToastLocked]
 *
 * 解决方案:
 * 方案1. try-catch包裹[Toast.TN.handleShow]方法
 * 反射修改mTN的mHandler的实例为我们自定义的实例，处理类型为[Toast.TN.SHOW]的消息，其他消息类型继续通过原来的handler处理.处理消息时使用try-catch包裹
 * </br>
 * 方案2. 自定义Toast的上下文，重写[Context.getSystemService]方法，将WMS进行一层包装，在[android.view.WindowManager.addView]添加try-catch
 * @see 方案2 https://github.com/PureWriter/ToastCompat
 *
 */
object ToastCompat {

    /**
     * 显示toast时的栈信息
     */
    private const val STACK_TRACE_SHOW_TOAST = "android.widget.Toast\$TN#handleShow"

    /**
     * wrapperSystemService的栈信息
     */
    private val STACK_TRACE_WRAPPER = ToastCompat::class.java.name + "#wrapperSystemService"


    private val isN =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O

    /**
     * 修复android N Toast 方案2
     * 在Android N中，对Toast用到的WindowManager进行包装
     *
     * 复写[android.app.Application.getSystemService]
     */
    @JvmStatic
    fun wrapperSystemService(service: Any): Any {
        if (!isN) return service

        if (service is WindowManager && service !is WindowManagerWrapper && isCallFromShowToast()) {
            return WindowManagerWrapper(service)
        }

        return service
    }

    /**
     * 调用方法是否从toast调用的
     */
    private fun isCallFromShowToast(): Boolean {
        val stackTraces = getStackTraces()
        val indexOf = stackTraces.indexOf(STACK_TRACE_WRAPPER)// 查找wrapperSystemService方法在栈中索引
        val offset = 2// handleShow方法相对于wrapperSystemService的偏移量
        return indexOf != -1 &&
                indexOf + offset < stackTraces.size &&// 判断当前栈位置下是否还有两个方法，分别是getSystemService和handleShow
                stackTraces[indexOf + offset] == STACK_TRACE_SHOW_TOAST // 判断当前栈位置下的两个方法是否时show
    }

    /**
     * 获取当前栈内方法数组，格式ClassName#MethodName字符串
     */
    private fun getStackTraces(): List<String> {
        val ex = Throwable()
        val stackTraces = ex.stackTrace ?: return emptyList()
        return stackTraces.map { it.className + "#" + it.methodName }
    }

    /**
     * 忽略显示Toast异常的WindowManager
     */
    private class WindowManagerWrapper(val base: WindowManager) : WindowManager by base {

        private fun isToast(params: ViewGroup.LayoutParams?): Boolean {
            @Suppress("DEPRECATION")
            return params is WindowManager.LayoutParams &&
                    params.type == WindowManager.LayoutParams.TYPE_TOAST &&
                    params.title == "Toast"
        }

        override fun addView(view: View?, params: ViewGroup.LayoutParams?) {
            if (!isToast(params)) {
                base.addView(view, params)
                return
            }

            try {
                Log.i("WindowManagerWrapper", "Show Toast")
                base.addView(view, params)
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 修复android N Toast, WindowManager$BadTokenException 方案1
     * 修复android N 以上 Toast, IllegalStateException [android.view.WindowManagerGlobal.addView]
     *      同一个Toast对象，第一次addView失败后，第二次再次添加时会抛出异常
     */
    private fun fixNToast(toast: Toast): Toast {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // Android N 以下
            return toast
        }
        val clazz = Toast::class.java
        try {
            val mTNField = clazz.getDeclaredField("mTN")
            mTNField.isAccessible = true
            val mTN = mTNField.get(toast) ?: return toast
            val tnClass = mTN.javaClass/*Class.forName("android.widget.Toast\$TN")*/
            val mHandlerField = tnClass.getDeclaredField("mHandler")
            mHandlerField.isAccessible = true
            val mHandler = mHandlerField.get(mTN) as Handler
            val handleShowMethod = tnClass.getMethod("handleShow", IBinder::class.java)
            val newHandler = FixNToastShowHandler(mTN, handleShowMethod, mHandler)
            mHandlerField.set(mTN, newHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return toast
    }

    private class FixNToastShowHandler(val mTN: Any, val handleShow: Method, val base: Handler) :
        Handler(checkNotNull(Looper.myLooper()), null) {

        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                // SHOW的消息类型为0
                try {
                    val token = msg.obj as? IBinder
                    handleShow.invoke(mTN, token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return
            }

            base.handleMessage(msg)
        }
    }

    @JvmStatic
    fun wrapperToast(toast: Toast): Toast {
        return fixNToast(toast)
    }

    @SuppressLint("ShowToast")
    @JvmStatic
    fun makeText(context: Context, text: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast {
        val toast = Toast.makeText(context, text, duration)
        return fixNToast(toast)
    }

    @JvmStatic
    fun toast(context: Context, text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        makeText(context, text, duration).show()
    }
}
