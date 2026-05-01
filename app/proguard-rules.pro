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

# Keep all data models for serialization
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class com.hfad.lacasapgmanagement.data.** {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}
-keep class com.hfad.lacasapgmanagement.data.** { *; }

# Kotlin Serialization
-keepclassmembers class * {
    *** Companion;
    *** serializer(...);
}

# Ktor & OkHttp (if used via Ktor)
-keepattributes RuntimeVisible*Annotations
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.serialization.json.** { *; }
