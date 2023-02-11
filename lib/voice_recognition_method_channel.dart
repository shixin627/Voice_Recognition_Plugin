import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'voice_recognition_platform_interface.dart';

/// An implementation of [VoiceRecognitionPlatform] that uses method channels.
class MethodChannelVoiceRecognition extends VoiceRecognitionPlatform {
  /// The method channel used to interact with the native platform.
  final methodChannel = const MethodChannel('voice_recognition');

  MethodChannelVoiceRecognition() {
    methodChannel.setMethodCallHandler(_handler);
  }

  Future<dynamic> _handler(MethodCall methodCall) {
    if ("onReturnResult" == methodCall.method) {
      String result = methodCall.arguments.toString();
      debugPrint("Pass ReturnResult----$result----to Flutter");
      _recognitionResultController.add(result);
    }
    return Future.value(true);
  }

  final StreamController<String> _recognitionResultController =
      StreamController.broadcast();
  @override
  Stream<String> get recognitionResultStream =>
      _recognitionResultController.stream;

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
