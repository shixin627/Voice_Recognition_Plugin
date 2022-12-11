import 'dart:async';

import 'package:flutter/services.dart';

import 'RecognitionResult.dart';
import 'voice_recognition_platform_interface.dart';

class VoiceRecognition {
  Future<String?> getPlatformVersion() {
    return VoiceRecognitionPlatform.instance.getPlatformVersion();
  }

  Future<String?> startVoiceRecognition() {
    return VoiceRecognitionPlatform.instance.startVoiceRecognition();
  }

  Future<String?> stopVoiceRecognition() {
    return VoiceRecognitionPlatform.instance.stopVoiceRecognition();
  }

  Stream<String> get recognitionResultStream {
    return VoiceRecognitionPlatform.instance.recognitionResultStream;
  }
}
