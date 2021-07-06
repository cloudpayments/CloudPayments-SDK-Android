# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# com.squareup.retrofit2
-dontwarn javax.annotation.**
-dontwarn retrofit2.**
-keep, includedescriptorclasses class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# com.squareup.okhttp3
-keepattributes Signature
-keepattributes *Annotation*
-keep, includedescriptorclasses class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.internal.platform.** { *; }
-dontwarn okhttp3.internal.platform.**
-dontnote okhttp3.internal.platform.**
-dontobfuscate

# com.google.gson
-keep class com.google.gson.internal.** { *; }
-dontwarn com.google.gson.internal.**
-dontnote com.google.gson.internal.**

# ru.cloudpayments.sdk
-keep class ru.cloudpayments.demo.** { *; }
-dontwarn ru.cloudpayments.demo.**
-dontnote ru.cloudpayments.demo.**