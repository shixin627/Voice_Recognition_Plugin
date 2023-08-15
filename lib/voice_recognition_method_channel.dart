import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'voice_recognition_mode.dart';
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
    } else if ("onReturnCmd" == methodCall.method && methodCall.arguments.toString() == "/cmd/end") {
      _recognitionResultController.add("/cmd/end");
    } else if ("targetAddress" == methodCall.method) {
      String address = methodCall.arguments.toString();
      debugPrint("Pass Target Bluetooth Audio Address---$address---to Flutter");
      bluetoothAddressCallback?.call(address);
    }
    return Future.value(true);
  }

  @override
  void Function(String address)? bluetoothAddressCallback;

  final StreamController<String> _recognitionResultController =
      StreamController.broadcast();

  @override
  Stream<String> get recognitionResultStream =>
      _recognitionResultController.stream;

  @override
  Future<String?> pairBluetoothDeviceByName(String bluetoothName) async {
    Map<String, dynamic> data = {"bluetoothName": bluetoothName};
    final state = await methodChannel.invokeMethod<String>(
        'pairBluetoothDeviceByName', data);
    return state;
  }

  @override
  Future<bool> startVoiceRecognition(
      VoiceRecognitionMode mode, String bluetoothAddress) async {
    Map<String, dynamic> data = {
      "mode": mode.index,
      "bluetoothAddress": bluetoothAddress
    };
    final started =
        await methodChannel.invokeMethod<bool>('startVoiceRecognition', data);
    return started ?? false;
  }

  @override
  Future<void> stopVoiceRecognition() async {
    await methodChannel.invokeMethod<void>('stopVoiceRecognition');
  }
}
