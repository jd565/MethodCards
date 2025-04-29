package com.jpd.methodcards.presentation.permissions

expect interface PermissionsBridgeListener {
    fun requestMicPermission(callback: PermissionResultCallback)
    fun isMicPermissionGranted(): Boolean
}

object PermissionBridge {
    private var listener: PermissionsBridgeListener? = null

    fun setListener(listener: PermissionsBridgeListener) {
        this.listener = listener
    }

    fun requestMicPermission(callback: PermissionResultCallback) {
        listener?.requestMicPermission(callback) ?: error("Callback handler not set")
    }

    fun isMicPermissionGranted(): Boolean {
        return listener?.isMicPermissionGranted() ?: false
    }
}

interface PermissionResultCallback {
    fun onPermissionGranted()
    fun onPermissionDenied(isPermanentDenied: Boolean)
}
