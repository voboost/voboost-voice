# Voboost Voice Assistant - Context

## Project Overview
Voice assistant replacement for IVI automotive system (package: `ru.voboost.voice`)

## Current State (2026-05-23)
- ✅ NLU models moved from assets to external SD card storage
- ✅ Build system fully automated via `scripts/build-project.bat [1|2|3]`
- ✅ **Sherpa-ONNX v1.12.34 local JAR** for TTS (v1.13 not used due to ONNX runtime conflicts)
- ✅ Vosk ASR working without conflicts
- ⚠️ NLU ONNX not working due to native library conflicts with Sherpa
- ✅ Parser mode NLU as fallback (fast, stable, <5ms response time)
- ✅ **Echo cancellation implemented** via AudioPolicyService + PhoneCallPoller (verified working 2026-05-23)
- ✅ **App ID migrated** from `ru.voboost.voiceassistant` → `ru.voboost.voice`
- ✅ **Migration script** created: `scripts/install/migrate-from-voiceassistant.bat`
- ✅ **Hidden API Policy** configured (`hidden_api_policy=1`) for AIDL access
- ✅ **Documentation updated**: INSTALLATION.md + TROUBLESHOOTING.md (pushed to GitHub)

## Speech Engines Architecture

### ASR (Speech Recognition)
| Engine | Status | Notes |
|--------|--------|-------|
| **Vosk** | ✅ Active | Java API `com.alphacephei:vosk-android:0.3.45`, uses JNA for native access |
| Sherpa-ONNX | ❌ Not Used | Uses ONNX Runtime internally, but has library conflicts with Microsoft ONNX |

### TTS (Text-to-Speech)
| Engine | Status | Notes |
|--------|--------|-------|
| **Sherpa-ONNX** | ✅ Active | v1.12.34 local JAR via `libs/sherpa-onnx-v1.12.34-java8.jar`, uses MiniLM for semantic similarity |
| System TTS | Available | Android native API |

**Note:** Sherpa v1.13 not used due to ONNX runtime library conflicts with Microsoft ONNX dependency

### NLU (Natural Language Understanding)
| Engine | Status | Notes |
|--------|--------|-------|
| **Parser** | ✅ Active | Regex-based exact match, <5ms response time |
| ONNX (MiniLM) | ❌ Broken | Conflicts with Sherpa's native ONNX library (`OrtGetApiBase` error) |
| LLM (MediaPipe) | Available | Too slow (20+ seconds), not recommended |

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
| jna | 5.13.0@aar | JNA for Vosk native access |

###Sherpa Dependencies (TTS Only)
| Library | Version | Purpose | Status |
|---------|---------|---------|--------|
| sherpa-onnx-java8.jar | v1.12.34 | TTS with MiniLM | ✅ Active (local JAR) |

**Note:** Sherpa v1.13 Maven dependency not used due to ONNX runtime conflicts

### Removed/Commented Dependencies
| Dependency | Reason |
|------------|--------|
| `onnxruntime-android:1.17.1` | Native library conflict withSherpa |
| `mediapipe:tasks-genai:0.10.35` | LLM too slow (20+ seconds) |
| `sherpa-onnx:1.13.1` (Maven) | Causes ONNX runtime conflicts |

**Note:** Sherpa v1.12.34 local JAR is used for TTS without conflicts

### Dependencies in build.gradle (Current)
```gradle
dependencies {
    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.5'
    
    // Vosk ASR (active)
    implementation('com.alphacephei:vosk-android:0.3.45') {
        exclude group: 'net.java.dev.jna', module: 'jna'
    }
    
    // Sherpa TTS (v1.12.34 local JAR - v1.13 has conflicts)
    implementation files('libs/sherpa-onnx-v1.12.34-java8.jar')
    
    // JNA for Vosk
    implementation 'net.java.dev.jna:jna:5.13.0@aar'
    
    // LLM (too slow, not used)
    implementation 'com.google.mediapipe:tasks-genai:0.10.35'
    
    // ONNX Runtime (commented due to conflicts)
    // implementation 'com.microsoft.onnxruntime:onnxruntime-android:1.17.1'
}
```

### NLU ONNX Not Working
**Problem:** Cannot use ONNX-based NLU (MiniLM model) due to native library conflict.

**Error Log:**
```
java.lang.UnsatisfiedLinkError: dlopen failed: cannot locate symbol "OrtGetApiBase"
referenced by "...libonnxruntime4j_jni.so"
```

**Root Cause:**Sherpa-ONNX v1.12 bundles its own `libonnxruntime.so`, which conflicts withMicrosoft ONNX Runtime.

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
3. Check if microphone is muted during call (see PhoneCallPoller logs)
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
1. ✅ Test current configuration (Parser NLU + Vosk ASR + Sherpa TTS) - **TESTED 2026-05-23**
2. ✅ Echo cancellation testing - verify AEC is working during Bluetooth calls - **WORKING**
3. If needed: Implement custom NLU using ai.djl.huggingface (DJL) instead of ONNX Runtime

## Changelog

### 2026-05-23 - AudioPolicyService Integration (SUCCESS!)

**Discovery:** Found `AudioPolicyManager.isInCall()` in system logs shows it detects calls for packages like `com.qinggan.bluetoothphone`.

**Problem Solved:**
- Original approach used CAN bus events (`onICMReqCallModeChanged()`) but events weren't arriving
- System has internal AudioPolicy service that tracks all active audio streams including voice calls

**Solution:** Use `IAudioPolicyService.aidl` which has method:
```aidl
boolean isInCall(String callingPackage);
```

This returns `true` if the specified package has an active call in the audio policy system.

**Implementation:**

#### AudioPolicyServiceManager.kt (Refactored)
- Removed auto-connect from `init` block → replaced with explicit `connect()` method
- Added delayed connection after CanBus initialization
- Fixed `CALLING_PACKAGE_NAME`: `"ru.voboost.voice"` instead of `"com.qinggan.app.launcher"`
- Updated log tags to reference AudioPolicyService instead of CanBusService

#### PhoneCallPoller.kt (Refactored)
- **Package:** `ru.voboost.voice.audio` (moved from `canbus`)
- **Simplified:** Now takes pre-connected `AudioPolicyServiceManager` instead of creating new instance
- **Polling loop:** Checks `audioPolicyManager.isInCall()` every 500ms using Kotlin Coroutines
- When call active (`inCall=true`) → mutes speech recognizer (`MUTED` mode)
- When call ends (`inCall=false`) → restores keyword detection (`KEYWORD` mode)

#### CanBusServiceManager.kt (Refactored)
- Removed auto-connect from `init` block → explicit `connect()` method
- Added check in `bindToService()`: returns early if already bound/connecting

#### VoboostVoiceService.kt (Updated)
```kotlin
// 1. Initialize CanBus first (auto-connects)
canBusManager = CanBusServiceManager(this)

// 2. Create AudioPolicyManager and connect AFTER CanBus
audioPolicyManager = AudioPolicyServiceManager(this)
audioPolicyManager.connect()

// 3. Create PhoneCallPoller with pre-connected manager
phoneCallPoller = PhoneCallPoller(speechRecognizer, audioPolicyManager)

// 4. Start polling (happens in onCreate())
phoneCallPoller.start()
```

**Connection Order:**
1. `canBusManager.connect()` — connects to CAN bus service immediately
2. `audioPolicyManager = AudioPolicyServiceManager(this)` — creates manager
3. `audioPolicyManager.connect()` — connects to AudioPolicy service (500ms timeout)
4. `phoneCallPoller.start()` — begins polling `isInCall()` every 500ms

**Log Output on Connection:**
```
I/CanBusServiceManager: bindService result: true
I/CanBusServiceManager: Connected to CanBusService
I/AudioPolicyServiceManager: bindService result: true
I/AudioPolicyServiceManager: Connected to AudioPolicyService
I/PhoneCallPoller: ✅ Phone call polling started
```

**Log Output During Call Detection:**
```
I/PhoneCallPoller: 📞 Call active - muting recognizer
I/PhoneCallPoller: 📞 Call ended - restoring keyword mode
```

**Files Modified:**
- `app/src/main/java/ru/voboost/voice/audio/AudioPolicyServiceManager.kt` — Refactored (delayed connection)
- `app/src/main/java/ru/voboost/voice/canbus/CanBusServiceManager.kt` — Refactored (explicit connect)
- `app/src/main/java/ru/voboost/voice/VoboostVoiceService.kt` — Updated initialization order
- `app/src/main/java/ru/voboost/voice/audio/PhoneCallPoller.kt` — Simplified, takes connected manager

**Testing:**
```bash
# Start service:
adb shell am startservice ru.voboost.voice/.VoboostVoiceService

# Watch logs:
adb logcat | grep -E "AudioPolicy|CanBus|PhoneCall"

# Expected: isInCall should detect Bluetooth/phone calls and mute recognition
```

**Status:** ✅ **WORKING** — Call detection verified, microphone mutes during calls, resumes after

### 2026-05-21 - SpeechRecognizer Mode Management (SUCCESS!)

**Enhancement:** Added mode management to `SpeechRecognizer` for external control.

**Changes:**
1. Added public `getMode(): Mode` method — returns current recognition state
2. Added `setModeSafe(newMode: Mode)` — coroutine-based safe mode switching from external threads

**Implementation:**
```kotlin
// SpeechRecognizer.kt (current)
enum class Mode {
    KEYWORD,   // Ждём ключевое слово
    COMMAND,   // Ждём команду
    MUTED      // TTS активен — игнорируем вход
}

override fun getMode(): Mode = mode

override fun setModeSafe(newMode: Mode) {
    scope.launch {
        setMode(newMode)
    }
}
```

**Why:** `PhoneCallPoller` runs in separate coroutine scope and needs thread-safe access to change recognition mode.

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
- `app/src/main/java/ru/voboost/voice/canbus/PhoneCallPoller.kt` - NEW (polls AudioPolicyManager, moved to audio/)
- `app/src/main/java/ru/voboost/voice/VoboostVoiceService.kt` - Updated to use PhoneCallPoller
- `app/src/main/java/ru/voboost/voice/speech/SpeechRecognizer.kt` - Added public getMode() and setModeSafe()

**Current Implementation (2026-05-23):**
```kotlin
// PhoneCallPoller.kt (full implementation)
class PhoneCallPoller(private var speechRecognizer: ISpeechRecognizer,
                      private val audioPolicyManager: AudioPolicyServiceManager) {
    companion object {
        const val TAG = "PhoneCallPoller"
        private const val CHECK_INTERVAL_MS = 500L
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var pollingJob: Job? = null

    fun start() { /* polling loop with delay */ }
    fun stop() { /* cleanup */ }
}
```

**Next Steps:**
1. ✅ Enable and test PhoneCallPoller polling logic in VoboostVoiceService - **COMPLETED 2026-05-23**
2. ✅ Test with Bluetooth call to verify echo cancellation works - **WORKING**
3. Add more detailed logging to debug call state detection

### 2026-05-21 - Documentation: Hidden API Policy

**Added documentation for Hidden API Policy configuration:**

**Files Modified:**
- `docs/SETUP/INSTALLATION.md` — Added "Hidden API Policy (для работы с AIDL)" section
- `docs/SETUP/TROUBLESHOOTING.md` — Added Problem 9: "AIDL не работает (CAN bus / AudioPolicy)"

**Hidden API Policy Commands:**
```bash
adb shell "settings put global hidden_api_policy 1"
adb shell "settings put global hidden_api_policy_pre_p 1"
adb shell "settings put global hidden_api_policy_pre_q 1"
adb shell "settings put global hidden_api_policy_pre_r 1"

# Verify
adb shell settings get global hidden_api_policy  # Should return: 1
```

**Why Required:**
- Android blocks access to hidden APIs (marked as `@hide`)
- AIDL interfaces (`IAudioPolicyService`, `ICanBusService`) use hidden methods
- Without this setting: echo cancellation and CAN bus events don't work

**Status:** ✅ Committed and pushed to GitHub (`feature/development`)

### 2026-05-21 - App ID Migration Script

**Migration script created:** `scripts/install/migrate-from-voiceassistant.bat`

**What it does:**
1. Uninstalls old app (`ru.voboost.voiceassistant`)
2. Migrates models (vosk, sherpa, nlu, llm) and config.json to new directory
3. Deletes old directory
4. Installs new APK (`ru.voboost.voice`)
5. Grants permissions
6. Sets Hidden API Policy
7. Starts service

**Status:** ✅ Created, committed, and pushed to GitHub

### 2026-05-23 - Sherpa-ONNX Version Clarification

**Discovery:** After testing, confirmed thatSherpa v1.13 causes ONNX runtime conflicts.

**Current Configuration:**
| Component | Engine | Version | Status |
|-----------|--------|---------|--------|
| TTS | Sherpa-ONNX | v1.12.34 (local JAR) | ✅ Working |
| ASR | Vosk | 0.3.45 | ✅ Working |
| NLU | Parser | Regex-based | ✅ Working |

**Why v1.12.34 is Used:**
- v1.13 bundles `libonnxruntime.so` causing `OrtGetApiBase` error
- Local JAR (v1.12.34) works without conflicts
- TTS functionality identical between versions

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


