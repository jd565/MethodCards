import SwiftUI
import FirebaseCore
import ComposeApp
import AVFoundation

class AppDelegate: NSObject, UIApplicationDelegate, PermissionRequestProtocol {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
//         PermissionBridge.setListener(listener: self)
        return true
    }

    func requestMicPermission(callback: PermissionResultCallback) {
        let audioSession = AVAudioSession.sharedInstance()
        switch audioSession.recordPermission {
        case .undetermined:
            audioSession.requestRecordPermission { granted in
                DispatchQueue.main.async {
                    if granted {
                        callback.onPermissionGranted()
                        print("Microphone permission granted")
                    } else {
                        callback.onPermissionDenied(isPermanentDenied: false)
                        print("Microphone permission denied")
                    }
                }
            }
        case .denied:
            print("Microphone access is denied")
            DispatchQueue.main.async {
                callback.onPermissionDenied(isPermanentDenied: true)
            }
        case .granted:
            print("Microphone access already authorized")
            DispatchQueue.main.async {
                callback.onPermissionGranted()
            }
        @unknown default:
            fatalError("Unknown authorization status")
        }
    }

    func isMicPermissionGranted() -> Bool {
        let audioSession = AVAudioSession.sharedInstance()
        return audioSession.recordPermission == .granted
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
