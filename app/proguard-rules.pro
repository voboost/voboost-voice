# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt

# Keep Vosk classes
-keep class com.alphacephei.** { *; }
-dontwarn com.alphacephei.**

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep model classes
-keep class com.voboost.voiceassistant.config.** { *; }
-keep class com.voboost.voiceassistant.nlu.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
