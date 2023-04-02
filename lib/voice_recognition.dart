import 'dart:async';
import 'voice_recognition_platform_interface.dart';

class VoiceRecognition {
  Future<String?> startVoiceRecognition(String bluetoothAddress) {
    return VoiceRecognitionPlatform.instance.startVoiceRecognition(bluetoothAddress);
  }

  Future<String?> stopVoiceRecognition() {
    return VoiceRecognitionPlatform.instance.stopVoiceRecognition();
  }

  Stream<String> get recognitionResultStream {
    return VoiceRecognitionPlatform.instance.recognitionResultStream;
  }
}
