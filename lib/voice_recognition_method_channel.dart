import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:voice_recognition/RecognitionResult.dart';

import 'voice_recognition_platform_interface.dart';

/// An implementation of [VoiceRecognitionPlatform] that uses method channels.
class MethodChannelVoiceRecognition extends VoiceRecognitionPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('voice_recognition');

  MethodChannelVoiceRecognition() {
    methodChannel.setMethodCallHandler(_handler);
  }

  Future<dynamic> _handler(MethodCall methodCall) {
    if ("onReturnResult" == methodCall.method) {
      debugPrint("onReturnResult in Flutter");
      debugPrint("----------${methodCall.arguments}------------");
      _recognitionResultController.add(methodCall.arguments.toString());
    }
    return Future.value(true);
  }

  final StreamController<String> _recognitionResultController =
      StreamController.broadcast();
  @override
  Stream<String> get recognitionResultStream =>
      _recognitionResultController.stream;

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> startVoiceRecognition() async {
    final state =
        await methodChannel.invokeMethod<String>('startVoiceRecognition');
    return state;
  }

  @override
  Future<String?> stopVoiceRecognition() async {
    final state =
        await methodChannel.invokeMethod<String>('stopVoiceRecognition');
    return state;
  }
}
