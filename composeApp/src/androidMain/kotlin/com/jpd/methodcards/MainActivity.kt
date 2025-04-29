package com.jpd.methodcards

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jpd.methodcards.di.MethodCardNonWebDi
import com.jpd.methodcards.presentation.App
import com.jpd.methodcards.presentation.permissions.PermissionBridge
import com.jpd.methodcards.presentation.permissions.PermissionResultCallback
import com.jpd.methodcards.presentation.permissions.PermissionsBridgeListener

class MainActivity : ComponentActivity(), PermissionsBridgeListener {

    private var micPermissionResultCallback: PermissionResultCallback? = null

    private val requestMicPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                micPermissionResultCallback?.onPermissionGranted()
            } else {
                val permanentlyDenied =
                    !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                micPermissionResultCallback?.onPermissionDenied(permanentlyDenied)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MethodCardNonWebDi.appContext = applicationContext
        PermissionBridge.setListener(this)

        setContent {
            App()
        }
    }

    override fun requestMicPermission(callback: PermissionResultCallback) {
        val permission = Manifest.permission.RECORD_AUDIO
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                callback.onPermissionGranted()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                callback.onPermissionDenied(false)
            }

            else -> {
                micPermissionResultCallback = callback
                requestMicPermissionLauncher.launch(permission)
            }
        }
    }

    override fun isMicPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}
