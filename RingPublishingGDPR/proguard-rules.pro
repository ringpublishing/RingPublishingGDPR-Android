# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/mskrabacz/develop/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class vendor to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class com.ringpublishing.gdpr.** { *; }
-keep interface com.ringpublishing.gdpr.** { *; }

-keep class com.ringpublishing.gdpr.internal.network.** { *; }
-dontwarn com.ringpublishing.gdpr.internal.network.UserAgentInfo
-dontwarn com.ringpublishing.gdpr.internal.network.UserAgentInterceptor

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.* {*;}

-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.UnsignedBytes$**

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}