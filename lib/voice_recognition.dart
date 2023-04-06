import 'dart:async';
import 'voice_recognition_platform_interface.dart';

class VoiceRecognition {
  void setBluetoothAddressCallback(void Function(String)? callback) {
    VoiceRecognitionPlatform.instance.bluetoothAddressCallback = callback;
  }

  Future<String?> pairBluetoothDeviceByName(String bluetoothName) {
    return VoiceRecognitionPlatform.instance
        .pairBluetoothDeviceByName(bluetoothName);
  }

  Future<String?> startVoiceRecognition(String bluetoothAddress) {
    return VoiceRecognitionPlatform.instance
        .startVoiceRecognition(bluetoothAddress);
  }

  Future<String?> stopVoiceRecognition() {
    return VoiceRecognitionPlatform.instance.stopVoiceRecognition();
  }

  Stream<String> get recognitionResultStream {
    return VoiceRecognitionPlatform.instance.recognitionResultStream;
  }
}
