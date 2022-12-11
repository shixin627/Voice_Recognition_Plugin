import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:voice_recognition/RecognitionResult.dart';
import 'package:voice_recognition/voice_recognition.dart';

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
  String _platformVersion = 'Unknown';
  String _state = 'initState';
  String _textResult = '...';
  bool _isListening = false;
  final _voiceRecognitionPlugin = VoiceRecognition();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _voiceRecognitionPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> startVoiceRecognition() async {
    String state;
    try {
      state = await _voiceRecognitionPlugin.startVoiceRecognition() ??
          'Unknown state';
    } on PlatformException {
      state = 'Failed to start voice recognition.';
    }

    if (!mounted) return;

    setState(() {
      _state = state;
    });
  }

  Future<void> stopVoiceRecognition() async {
    String state;
    try {
      state = await _voiceRecognitionPlugin.stopVoiceRecognition() ??
          'Unknown state';
    } on PlatformException {
      state = 'Failed to stop voice recognition.';
    }

    if (!mounted) return;

    setState(() {
      _state = state;
    });
  }

  void toggleVoiceRecognition() {
    if (_isListening == false) {
      startVoiceRecognition();
      setState(() {
        _isListening = true;
      });
    } else {
      stopVoiceRecognition();
      setState(() {
        _isListening = false;
      });
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
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              const SizedBox(
                height: 30,
              ),
              Text('Running on: $_platformVersion\n'),
              Text('Statement: $_state\n'),
              IconButton(
                  onPressed: toggleVoiceRecognition,
                  icon: Icon(
                    Icons.mic,
                    color: _isListening ? Colors.purpleAccent : Colors.grey,
                    size: 35,
                  )),
              StreamBuilder<String>(
                  stream: _voiceRecognitionPlugin.recognitionResultStream,
                  builder: (context, snapshot) {
                    if (snapshot.connectionState == ConnectionState.active &&
                        snapshot.data != null) {
                      _textResult = snapshot.data ?? "no text";
                    }
                    return Text(_textResult,
                        style: const TextStyle(fontSize: 30));
                  })
            ],
          ),
        ),
      ),
    );
  }
}
