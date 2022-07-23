# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-printconfiguration ./build/full-r8-config.txt

-keep public class * extends com.dede.nativetools.netspeed.stats.NetStats {
   public <init>();
}

-keep class me.weishu.reflection.* {*;}

-keepclassmembernames class androidx.appcompat.widget.AppCompatSpinner {
    private androidx.appcompat.widget.AppCompatSpinner$SpinnerPopup mPopup;
}
-keepclassmembernames class androidx.appcompat.widget.ListPopupWindow {
    android.widget.PopupWindow mPopup;
}

-dontwarn com.google.errorprone.annotations.Immutable
-keepnames class * extends kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class * extends kotlinx.coroutines.CoroutineExceptionHandler

-keep class com.dede.nativetools.network.Api {*;}

# OkHttp
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
