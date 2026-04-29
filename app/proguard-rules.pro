# ══════════════════════════════════════════════
# APK Factory Pro — ProGuard Security Rules
# ══════════════════════════════════════════════

# Android basics
-keep class * extends android.webkit.WebViewClient
-keep class * extends android.webkit.WebChromeClient
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
-keepattributes JavascriptInterface

# Keep activity names (must match AndroidManifest)
-keep public class com.example.myapp.** extends android.app.Activity
-keep public class com.example.myapp.** extends android.app.Service

# Obfuscation settings
-dontoptimize
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification
-repackageclasses ''

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }

# Custom rules

