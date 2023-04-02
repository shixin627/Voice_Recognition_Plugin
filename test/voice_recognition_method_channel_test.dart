import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:voice_recognition/voice_recognition_method_channel.dart';

void main() {
  MethodChannelVoiceRecognition platform = MethodChannelVoiceRecognition();
  const MethodChannel channel = MethodChannel('voice_recognition');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

}
