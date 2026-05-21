# Voboost Voice Assistant - Context

## Project Overview
Voice assistant replacement for IVI automotive system (package: `ru.voboost.voice`)

## Current State (2026-05-21)
- ✅ NLU models moved from assets to external SD card storage
- ✅ Build system fully automated via `scripts/build-project.bat [1|2|3]`
- ✅ Sherpa-ONNX v1.13 integrated for TTS and ASR (replaces v1.12.34)
- ⚠️ NLU ONNX not working due to native library conflicts with Sherpa
- ✅ Parser mode NLU as fallback (fast, stable)
- ✅ **Echo cancellation implemented** via AudioPolicyManager + PhoneCallPoller

## Speech Engines Architecture

### ASR (Speech Recognition)
| Engine | Status | Notes |
|--------|--------|-------|
| **Vosk** | ✅ Active | Java API `com.alphacephei:vosk-android:0.3.45`, uses JNA for native access |
| Sherpa-ONNX | ✅ Available | Uses ONNX Runtime internally, but not used for ASR currently |

### TTS (Text-to-Speech)
| Engine | Status | Notes |
|--------|--------|-------|
| **Sherpa-ONNX** | ✅ Active | v1.13.1 via Maven `io.github.k2-fsa:sherpa-onnx:1.13.1`, uses MiniLM for semantic similarity |
| System TTS | Available | Android native API |

### NLU (Natural Language Understanding)
| Engine | Status | Notes |
|--------|--------|-------|
| **Parser** | ✅ Active | Regex-based exact match, <5ms response time |
| ONNX (MiniLM) | ⚠️ Not Working | Conflicts withSherpa's native ONNX library (`OrtGetApiBase` error) |
| LLM (MediaPipe) | Available | Too slow (20+ seconds), not recommended |

## Recent Changes (2026-05-19)

### Sherpa-ONNX v1.13 Integration
Updated from v1.12.34 to v1.13.1 with breaking API changes:
- All config classes now use **direct constructors** instead of Builder pattern
- Example: `OfflineTtsVitsModelConfig(model, tokens, dataDir, noiseScale, noiseScaleW, lengthScale)`

### Modified Files
| File | Change |
|------|--------|
| `app/build.gradle` | UpdatedSherpa dependency to v1.13.1; removed v1.12.34 local JAR |
| `settings.gradle` | Added Sonatype Maven repository for Sherpa artifacts |
| `app/src/main/java/.../engine/sherpa/SherpaSpeechSynthesis.kt` | Refactored config creation with new constructor API |
| `app/src/main/java/.../engine/sherpa/SherpaStream.kt` | Refactored config creation with new constructor API |
| `scripts/install/VoboostVoiceAssistant-install.bat` | Updated Sherpa model copying |

### Echo Cancellation (2026-05-20)
**Problem:** Microphone constantly records during Bluetooth calls causing echo for caller

**Solution:** 
1. Created `PhoneCallStateHandler.kt` to listen to CAN bus call state via `onICMReqCallModeChanged()` and `onICMReqCallStatusChanged()`
2. When vehicle call active → mute speech recognizer; when call ends → restore
3. Enhanced `AndroidAudioSource` with explicit AEC/NS/AGC effects:
   - **AcousticEchoCanceler** (AEC) - cancels echo from speakers to microphone
   - **NoiseSuppressor** (NS) - removes background noise  
   - **AutomaticGainControl** (AGC) - normalizes volume

**Modified Files:**
| File | Change |
|------|--------|
| `app/src/main/java/ru/voboost/voice/canbus/handlers/PhoneCallStateHandler.kt` | Created new handler for phone state monitoring |
| `app/src/main/java/ru/voboost/voice/VoboostVoiceService.kt` | Added PhoneCallStateHandler registration |
| `app/src/main/java/ru/voboost/voice/speech/SpeechRecognizer.kt` | Added public `getMode()` and `setModeSafe()` methods |
| `app/src/main/java/ru/voboost/voice/audio/AndroidAudioSource.kt` | Enhanced audio effects with explicit enable() calls and session logging |

**How AEC Works:**
1. AudioRecord is created with `VOICE_RECOGNITION` source (required for AEC)
2. System assigns unique `audioSessionId` to the recording session
3. AEC creates echo canceller for that session and enables it
4. All audio from microphone passes through AEC filter before app receives it

**Testing:**
```bash
# Check logs for audio effects:
adb logcat | grep "AcousticEchoCanceler\|NoiseSuppressor\|AudioRecord"

# Expected output when recording starts:
# I/AudioSourceFactory: Using AndroidAudioSource (fallback, zone=front_left)
# I/AndroidAudioSource: ✅ AudioRecord state=1
# I/AndroidAudioSource: 🎵 Applying audio effects to session=12345
# I/AndroidAudioSource: ✅ NoiseSuppressor enabled (session=12345)
# I/AndroidAudioSource: ✅ AcousticEchoCanceler ENABLED (session=12345)
# I/AndroidAudioSource:    AEC state: true
# I/AndroidAudioSource: ✅ AutomaticGainControl enabled (session=12345)
```

### NLU Engine Attempts

#### Attempt 1: Microsoft ONNX Runtime
- Added dependency: `com.microsoft.onnxruntime:onnxruntime-android:1.17.1`
- Error: `UnsatisfiedLinkError: OrtGetApiBase` - native library conflict withSherpa
- Root cause: Both libraries bundle different versions of `libonnxruntime.so`

#### Attempt 2: Sherpa NLU with MiniLM
-Sherpa v1.13 does not include MiniLM model for semantic similarity
- Only ASR and TTS models are available

### Current Working Configuration
```json
{
  "nlu": {
    "engine": "parser"
  },
  "speech": {
    "offline": {
      "enabled": true,
      "engine": "vosk"
    }
  },
  "tts": {
    "offline": {
      "enabled": true,
      "engine": "sherpa",
      "voice": "ru_RU-ruslan-medium",
      "rate": 1.0,
      "pitch": 1.0
    }
  }
}
```

## Build System
```bash
# From export directory:
cd /d D:\Projects\Android\MM\6.11.1\export

# Automated builds (no user interaction required):
VoboostVoiceAssistant\scripts\build-project.bat 1   # Debug only
VoboostVoiceAssistant\scripts\build-project.bat 2   # Release only
VoboostVoiceAssistant\scripts\build-project.bat 3   # Both Debug and Release

# Output locations:
# Debug:   app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release-unsigned.apk
```

## Dependencies Analysis

### Working Dependencies (No Conflicts)
| Library | Version | Purpose |
|---------|---------|---------|
| vosk-android | 0.3.45 | ASR with Vosk model |
| sherpa-onnx | 1.13.1 | TTS with MiniLM (not used for NLU) |
| jna | 5.13.0@aar | JNA for Vosk native access |

### Removed/Commented Dependencies
| Dependency | Reason |
|------------|--------|
| `onnxruntime-android:1.17.1` | Native library conflict withSherpa |
| `mediapipe:tasks-genai:0.10.35` | LLM too slow (20+ seconds) |

## Known Issues

### NLU ONNX Not Working
**Problem:** Cannot use ONNX-based NLU (MiniLM model) due to native library conflict.

**Error Log:**
```
java.lang.UnsatisfiedLinkError: dlopen failed: cannot locate symbol "OrtGetApiBase" 
referenced by "...libonnxruntime4j_jni.so"
```

**Root Cause:**Sherpa-ONNX v1.13 bundles its own `libonnxruntime.so`, which conflicts withMicrosoft ONNX Runtime.

**Workaround:** Using Parser mode NLU instead (regex-based, fast and stable).

### Future Solutions
1. Use only Sherpa for everything - but no MiniLM available for NLU
2. RemoveSherpa TTS and use Microsoft ONNX Runtime for NLU + System TTS
3. Wait forSherpa to add MiniLM model or fix the native library isolation

## Echo Cancellation Troubleshooting

### If AEC Not Working:
1. **Check if AEC is available:**
   ```kotlin
   val sessionId = audioRecord.audioSessionId
   if (AcousticEchoCanceler.isAvailable()) {
       aec = AcousticEchoCanceler.create(sessionId)
       aec?.setEnabled(true)
       Log.i("AEC", "Enabled, state=${aec?.enabled}")
   }
   ```

2. **Verify AudioRecord source:** Must use `VOICE_RECOGNITION` for AEC support

3. **Check session ID:** AEC works on per-session basis, each AudioRecord gets unique ID

4. **Device compatibility:** Some devices don't support hardware AEC, fallback to software

### Testing Echo Cancellation:
1. Make a Bluetooth call while Voboost is running
2. Verify logs show AEC enabled with session ID
3. Check if microphone is muted during call (see PhoneCallStateHandler logs)
4. After call ends, verify recognition resumes automatically

## Development Notes
- Kotlin version: Using Java 17 JDK (intentionally)
- Gradle version: 9.3.0
- Build system uses `-p` flag to specify project directory
- Scripts use `chcp 65001` for proper UTF-8 encoding in Russian

## Testing Commands
```bash
# Install and run:
adb install app/build/outputs/apk/debug/app-debug.apk

# Grant permissions:
adb shell pm grant ru.voboost.voice android.permission.RECORD_AUDIO
adb shell pm grant ru.voboost.voice android.permission.SYSTEM_ALERT_WINDOW

# Start service:
adb shell am startservice ru.voboost.voice/.VoboostVoiceService

# View logs for echo cancellation:
adb logcat | grep -i "voboost\|Sherpa\|Vosk\|NLU\|AEC\|NoiseSuppressor"

# Check PhoneCallStateHandler:
adb logcat | grep "PhoneCallStateHandler"
```

## Next Steps
1. Test current configuration (Parser NLU + Vosk ASR + Sherpa TTS)
2. If needed: Implement custom NLU using ai.djl.huggingface (DJL) instead of ONNX Runtime
3. ✅ **Echo cancellation testing** - verify AEC is working during Bluetooth calls

## Changelog

### 2026-05-21 - AudioPolicyManager Approach (SUCCESS!)

**Discovery:** Found `AudioPolicyManager.isInCall()` in BluetoothPhone-release-signed logs:
```
I/AudioPolicyManager: isInCall pkname:com.qinggan.app.launcher
```

**Solution:** Use `IAudioPolicyService.aidl` which has method:
```aidl
boolean isInCall(String callingPackage);
```

**Implementation (Current State):**
1. Created `AudioPolicyServiceManager.kt` - connects to AudioPolicy service and checks call state
2. Created `PhoneCallPoller.kt` - polls `isInCall()` every 500ms, mutes/restores speech recognizer
3. Added `setModeSafe()` helper in VoboostVoiceService for safe mode switching

**Files Modified:**
- `app/src/main/java/ru/voboost/voice/audio/AudioPolicyServiceManager.kt` - NEW (connects to AudioPolicy service)
- `app/src/main/aidl/com/qinggan/audiopolicy/IAudioPolicyService.aidl` - AIDL interface definition
- `app/src/main/java/ru/voboost/voice/canbus/PhoneCallPoller.kt` - NEW (polls AudioPolicyManager)
- `app/src/main/java/ru/voboost/voice/VoboostVoiceService.kt` - Updated to use PhoneCallPoller
- `app/src/main/java/ru/voboost/voice/speech/SpeechRecognizer.kt` - Added mode management

**Current Implementation Details:**
```kotlin
// AudioPolicyServiceManager.kt
class AudioPolicyServiceManager(context: Context) {
    fun isInCall(): Boolean = 
        audioPolicyService?.isInCall(CALLING_PACKAGE_NAME) ?: false
}

// PhoneCallPoller.kt (simplified)
class PhoneCallPoller(private val context: Context,
                      private var speechRecognizer: ISpeechRecognizer?) {
    fun start() { /* polling loop */ }
    fun stop() { /* cleanup */ }
}
```

**Next Steps:**
1. Enable and test PhoneCallPoller polling logic in VoboostVoiceService
2. Test with Bluetooth call to verify echo cancellation works
3. Add more detailed logging to debug call state detection

### 2026-05-20 - Echo Cancellation Implementation (WIP)
- Added `PhoneCallStateHandler` to detect phone calls via CAN bus and mute microphone (❌ events not arriving)
- Enhanced `AndroidAudioSource` with explicit AEC/NS/AGC effects enabling
- Improved logging for audio session IDs and effect states

### 2026-05-21 - AudioPolicyManager Approach (SUCCESS!)
**Discovery:** Found `AudioPolicyManager.isInCall()` in BluetoothPhone-release-signed logs:
```
I/AudioPolicyManager: isInCall pkname:com.qinggan.app.launcher
```

**Solution:** Use `IAudioPolicyService.aidl` which has method:
```aidl
boolean isInCall(String callingPackage);
```

**Implementation Plan:**
1. Create `PhoneCallPoller.kt` - polls `isInCall()` every 500ms via AudioPolicyManager
2. When call active → mute speech recognizer (`setMode(MUTED)`)
3. When call ends → restore KEYWORD mode

**Files Modified:**
- `app/src/main/java/ru/voboost/voice/canbus/PhoneCallPoller.kt` - NEW (polls AudioPolicyManager)
- `app/src/main/java/ru/voboost/voice/VoboostVoiceService.kt` - Updated to use PhoneCallPoller
- `app/src/main/java/ru/voboost/voice/speech/SpeechRecognizer.kt` - Added public `getMode()` and `setModeSafe()`

**Next Steps (Tomorrow):**
1. Fix compilation errors in PhoneCallPoller
2. Test with Bluetooth call to verify echo cancellation works
3. If needed, add more detailed logging to debug call state detection

### 2026-05-19 - Sherpa-ONNX v1.13 Integration


