import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:voice_recognition/voice_recognition.dart';
import 'package:voice_recognition/voice_recognition_mode.dart';

import 'theme.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final String _bluetoothAddress = "17:E9:9E:CA:37:52";
  String _textResult = '...';
  bool _isListening = false;
  final _voiceRecognitionPlugin = VoiceRecognition();

  @override
  void initState() {
    super.initState();
  }

  Future<void> startVoiceRecognition(String bluetoothAddress) async {
    String state;
    try {
      bool successful = await _voiceRecognitionPlugin.startVoiceRecognition(
          VoiceRecognitionMode.voice2Text, bluetoothAddress);
      state = successful ? 'Successful' : 'Failed';
    } on PlatformException {
      state = 'Failed to start voice recognition.';
    }
    debugPrint('startVoiceRecognition: $state');
  }

  Future<void> stopVoiceRecognition() async {
    String state;
    try {
      await _voiceRecognitionPlugin.stopVoiceRecognition();
      state = 'Successes to stop voice recognition.';
    } on PlatformException {
      state = 'Fail to stop voice recognition.';
    }
    debugPrint('stopVoiceRecognition: $state');
  }

  void toggleVoiceRecognition() {
    if (_isListening == false) {
      startVoiceRecognition(_bluetoothAddress);
    } else {
      stopVoiceRecognition();
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      themeMode: ThemeMode.system,
      theme: ThemeClass.lightTheme,
      darkTheme: ThemeClass.darkTheme,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Voice Recognition Plugin'),
        ),
        body: Center(
          child: Column(
            children: [
              StreamBuilder<bool>(
                  initialData: _isListening,
                  stream: _voiceRecognitionPlugin.recognitionStateStream,
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      _isListening = snapshot.data!;
                    } else {
                      return const Text('recognitionState: null');
                    }
                    return Column(
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                          children: [
                            ElevatedButton(
                              onPressed: (_isListening)
                                  ? null
                                  : () {
                                      startVoiceRecognition(_bluetoothAddress);
                                    },
                              child: const Text('Start'),
                            ),
                            ElevatedButton(
                              onPressed: (_isListening)
                                  ? () {
                                      stopVoiceRecognition();
                                    }
                                  : null,
                              child: const Text('Stop'),
                            ),
                          ],
                        ),
                        Text('recognitionState: $_isListening'),
                      ],
                    );
                  }),
              const Divider(height: 20),
              StreamBuilder<String>(
                  stream: _voiceRecognitionPlugin.recognitionResultStream,
                  builder: (context, snapshot) {
                    if (snapshot.connectionState == ConnectionState.active &&
                        snapshot.data != null) {
                      _textResult = snapshot.data ?? "no text";
                    }
                    return Text(_textResult, style: const TextStyle());
                  }),
            ],
          ),
        ),
      ),
    );
  }
}
