import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:voice_recognition/voice_recognition_mode.dart';
import 'package:voice_recognition/voice_recognition_platform_interface.dart';
import 'package:voice_recognition/voice_recognition_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockVoiceRecognitionPlatform
    with MockPlatformInterfaceMixin
    implements VoiceRecognitionPlatform {
  @override
  Future<bool> startVoiceRecognition(
      VoiceRecognitionMode mode, String bluetoothAddress) {
    // TODO: implement startVoiceRecognition
    throw UnimplementedError();
  }

  @override
  Future<String?> stopVoiceRecognition() {
    // TODO: implement stopVoiceRecognition
    throw UnimplementedError();
  }

  @override
  // TODO: implement recognitionResultStream
  Stream<String> get recognitionResultStream => throw UnimplementedError();

  @override
  Future<String?> pairBluetoothDeviceByName(String bluetoothName) {
    // TODO: implement pairBluetoothDeviceByName
    throw UnimplementedError();
  }

  @override
  void Function(String address)? bluetoothAddressCallback;
}

void main() {
  final VoiceRecognitionPlatform initialPlatform =
      VoiceRecognitionPlatform.instance;

  test('$MethodChannelVoiceRecognition is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelVoiceRecognition>());
  });
}
