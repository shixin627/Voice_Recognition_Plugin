#import "VoiceRecognitionPlugin.h"
#if __has_include(<voice_recognition/voice_recognition-Swift.h>)
#import <voice_recognition/voice_recognition-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "voice_recognition-Swift.h"
#endif

@implementation VoiceRecognitionPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftVoiceRecognitionPlugin registerWithRegistrar:registrar];
}
@end
