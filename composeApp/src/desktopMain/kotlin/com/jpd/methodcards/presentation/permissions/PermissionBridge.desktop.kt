package com.jpd.methodcards.presentation.permissions

actual interface PermissionsBridgeListener {
    actual fun requestMicPermission(callback: PermissionResultCallback)
    actual fun isMicPermissionGranted(): Boolean
}
