# Voboost Voice - Code Style (CRITICAL)

## Global
- Follows ALL common rules from ../voboost-codestyle/AGENTS.md

## Commands
- `./gradlew assembleDebug`: Build debug APK

## Project Structure
- Application code: `app/src/main/java/ru/voboost/voice/`
- AIDL vehicle services: `app/src/main/aidl/`
- Native libraries: `app/src/main/jniLibs/arm64-v8a/`
- Resources: `app/src/main/res/`

## Architecture
- Voice pipeline: audio source -> recognition engine -> NLU -> command executor -> vehicle AIDL
- Speech engines are pluggable: sherpa-onnx, Vosk, system (factory-selected)

## Build Environment
- Kotlin 2.2.x, AGP 9.2.x, Java 17
- compileSdk 36, minSdk 28, target ABI arm64-v8a only
- Models and config are excluded from the APK; they live on the SD card
