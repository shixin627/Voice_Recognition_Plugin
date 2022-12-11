import 'package:flutter/foundation.dart';

class RecognitionResult {
  String? text;
  RecognitionResult({this.text = ''});

  factory RecognitionResult.fromMap(Map<String, dynamic> map) {
    final String val = map['text'];
    return RecognitionResult(text: val);
  }

  Map<String, dynamic> toMap() {
    Map<String, dynamic> map = {};
    map['text'] = text;
    return map;
  }

  @override
  String toString() {
    return "RecognitionResult: text=$text";
  }
}