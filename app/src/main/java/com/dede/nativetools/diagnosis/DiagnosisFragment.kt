package com.dede.nativetools.diagnosis

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dede.nativetools.NativeToolsApp
import com.dede.nativetools.R
import com.dede.nativetools.databinding.FragmentDiagnosisBinding
import com.dede.nativetools.util.Logic
import com.dede.nativetools.util.exceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 诊断页
 */
class DiagnosisFragment : Fragment(R.layout.fragment_diagnosis), ServiceConnection {

    class Binder : IDiagnosisInterface.Stub() {
        override fun collection(): String {
            return Logic.collectionDiagnosis(NativeToolsApp.getInstance())
        }
    }

    class Service : android.app.Service() {
        override fun onBind(intent: Intent?): IBinder {
            return Binder()
        }
    }

    private val binding by viewBinding(FragmentDiagnosisBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = Intent(requireContext(), Service::class.java)
        requireContext().bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = IDiagnosisInterface.Stub.asInterface(service)
        lifecycleScope.launchWhenCreated {
            val result = if (binder == null) {
                collectionDiagnosis()
            } else {
                collectionDiagnosis(binder)
            }
            binding.tvDiagnosisMsg.text = result
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    override fun onDestroyView() {
        requireContext().unbindService(this)
        super.onDestroyView()
    }

    private suspend fun collectionDiagnosis(binder: IDiagnosisInterface): String {
        return withContext(exceptionHandler + Dispatchers.IO) {
            binder.collection()
        }
    }

    private suspend fun collectionDiagnosis(): String {
        return withContext(exceptionHandler + Dispatchers.IO) {
            Logic.collectionDiagnosis(NativeToolsApp.getInstance())
        }
    }

}