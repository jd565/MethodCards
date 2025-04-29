package com.jpd.methodcards.presentation.permissions

import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "PermissionRequestProtocol")
actual interface PermissionsBridgeListener {
    actual fun requestMicPermission(callback: PermissionResultCallback)
    actual fun isMicPermissionGranted(): Boolean

}
