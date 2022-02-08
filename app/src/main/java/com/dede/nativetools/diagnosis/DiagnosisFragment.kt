package com.dede.nativetools.diagnosis

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentDiagnosisBinding
import com.dede.nativetools.util.HandlerCallback
import com.dede.nativetools.util.LifecycleHandlerCallback
import com.dede.nativetools.util.Logic

/**
 * 诊断页
 */
class DiagnosisFragment : Fragment(R.layout.fragment_diagnosis), ServiceConnection {

    // 诊断服务，进程 netspeed
    class Service : android.app.Service(), HandlerCallback {

        private lateinit var messenger: Messenger

        override fun onHandleMessage(msg: Message) {
            val rMsg = Message.obtain().apply {
                data = bundleOf("result" to Logic.collectionDiagnosis(NativeToolsApp.getInstance()))
            }
            Thread.sleep(5000)
            try {
                msg.replyTo.send(rMsg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onCreate() {
            super.onCreate()
            val handlerThread = HandlerThread("DiagnosisService_HandlerThread")
            handlerThread.start()
            val handler = Handler(handlerThread.looper, this)
            messenger = Messenger(handler)
        }

        override fun onBind(intent: Intent?): IBinder? {
            return messenger.binder
        }
    }

    private val binding by viewBinding(FragmentDiagnosisBinding::bind)

    private val callback = LifecycleHandlerCallback(this) {
        binding.tvDiagnosisMsg.text = data.getString("result")
        binding.progressCircular.isGone = true
    }
    private val handler = Handler(Looper.getMainLooper(), callback)
    private val responseMessenger = Messenger(handler)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressCircular.isVisible = true
        val intent = Intent(requireContext(), Service::class.java)
        requireContext().bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val requestMessenger = Messenger(service)
        val msg = Message.obtain().apply { replyTo = responseMessenger }
        requestMessenger.send(msg)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    override fun onDestroyView() {
        requireContext().unbindService(this)
        super.onDestroyView()
    }

}