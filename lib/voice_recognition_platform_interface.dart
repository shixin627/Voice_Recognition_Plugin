import 'dart:async';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'voice_recognition_method_channel.dart';
import 'voice_recognition_mode.dart';

abstract class VoiceRecognitionPlatform extends PlatformInterface {
  /// Constructs a VoiceRecognitionPlatform.
  VoiceRecognitionPlatform() : super(token: _token);

  static final Object _token = Object();

  static VoiceRecognitionPlatform _instance = MethodChannelVoiceRecognition();

  /// The default instance of [VoiceRecognitionPlatform] to use.
  ///
  /// Defaults to [MethodChannelVoiceRecognition].
  static VoiceRecognitionPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [VoiceRecognitionPlatform] when
  /// they register themselves.
  static set instance(VoiceRecognitionPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  void Function(String address)? bluetoothAddressCallback;

  Future<String?> pairBluetoothDeviceByName(String bluetoothName) {
    throw UnimplementedError(
        'pairBluetoothDeviceByName() has not been implemented.');
  }

  Future<bool> startVoiceRecognition(
      VoiceRecognitionMode mode, String bluetoothAddress) {
    throw UnimplementedError(
        'startVoiceRecognition() has not been implemented.');
  }

  Future<void> stopVoiceRecognition() {
    throw UnimplementedError(
        'stopVoiceRecognition() has not been implemented.');
  }

  Stream<String> get recognitionResultStream {
    return const Stream.empty();
  }

  Stream<bool> get recognitionStateStream {
    return const Stream.empty();
  }
}
