package com.dede.nativetools.diagnosis

import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentDiagnosisBinding
import com.dede.nativetools.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 诊断页 */
class DiagnosisFragment : Fragment(R.layout.fragment_diagnosis) {

    // 诊断服务，进程 netspeed
    class Service : android.app.Service(), HandlerCallback {

        private lateinit var messenger: Messenger

        override fun onHandleMessage(msg: Message) {
            val rMsg =
                Message.obtain().apply { data = bundleOf("data" to Logic.collectionDiagnosis()) }
            try {
                msg.replyTo.send(rMsg)
            } catch (ignore: RemoteException) {}
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

    private val handler =
        LifecycleHandler(
            Looper.getMainLooper(),
            this,
            handlerMessage = {
                val data = data.getString("data")
                setData(data)
            }
        )
    private val responseMessenger = Messenger(handler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressCircular.isVisible = true
        requireContext()
            .bindService(
                intent = Intent(requireContext(), Service::class.java),
                onConnected = {
                    // 绑定成功
                    val requestMessenger = Messenger(it)
                    val msg = Message.obtain().apply { replyTo = responseMessenger }
                    try {
                        requestMessenger.send(msg)
                    } catch (e: RemoteException) {
                        collectionNow()
                        e.printStackTrace()
                    }
                },
                onFailed = {
                    // 绑定失败时在主进程收集诊断信息
                    collectionNow()
                },
                lifecycleOwner = this
            )
    }

    private fun collectionNow() {
        lifecycleScope.launchWhenCreated {
            val result = withContext(Dispatchers.IO) { Logic.collectionDiagnosis() }
            setData(result)
        }
    }

    private fun setData(data: String?) {
        binding.tvDiagnosisMsg.text = data
        binding.progressCircular.isGone = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_diagnosis, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val result = binding.tvDiagnosisMsg.text.toString()
        return when (item.itemId) {
            R.id.action_copy -> {
                if (result.isNotEmpty()) {
                    requireContext().copy(result)
                }
                true
            }
            R.id.action_share -> {
                if (result.isNotEmpty()) {
                    requireContext().share(result)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
