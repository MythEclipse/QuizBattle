# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ================================
# Quiz Battle - ProGuard Rules
# ================================

# Preserve line number information for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# ================================
# Kotlin & Coroutines
# ================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ================================
# Jetpack Compose
# ================================
-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt { *; }
-dontwarn androidx.compose.**

# ================================
# Retrofit & OkHttp
# ================================
# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# OkHttp WebSocket
-keep class okhttp3.internal.ws.** { *; }
-keep class okhttp3.WebSocket { *; }
-keep interface okhttp3.WebSocketListener { *; }

# ================================
# Gson
# ================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all data classes and models
-keep class com.mytheclipse.quizbattle.data.** { *; }
-keepclassmembers class com.mytheclipse.quizbattle.data.** { *; }

# Prevent stripping of generic signatures
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# ================================
# Room Database
# ================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** getInstance(...);
}

-dontwarn androidx.room.paging.**

# Keep Room entity classes
-keep class com.mytheclipse.quizbattle.data.local.entity.** { *; }
-keepclassmembers class com.mytheclipse.quizbattle.data.local.entity.** { *; }

# Keep Room DAO classes
-keep class com.mytheclipse.quizbattle.data.local.dao.** { *; }
-keepclassmembers class com.mytheclipse.quizbattle.data.local.dao.** { *; }

# ================================
# DataStore
# ================================
-keep class androidx.datastore.*.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# ================================
# Application Data Models
# ================================
# Keep all remote models (API responses)
-keep class com.mytheclipse.quizbattle.data.remote.model.** { *; }
-keepclassmembers class com.mytheclipse.quizbattle.data.remote.model.** { *; }

# Keep WebSocket message classes
-keep class com.mytheclipse.quizbattle.data.remote.websocket.** { *; }
-keepclassmembers class com.mytheclipse.quizbattle.data.remote.websocket.** { *; }

# Keep API service interfaces
-keep interface com.mytheclipse.quizbattle.data.remote.api.** { *; }

# ================================
# ViewModels & Repositories
# ================================
-keep class com.mytheclipse.quizbattle.viewmodel.** { *; }
-keep class com.mytheclipse.quizbattle.data.repository.** { *; }

# Keep ViewModel constructors
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ================================
# Coil (Image Loading)
# ================================
-keep class coil.** { *; }
-keep interface coil.** { *; }
-keep class * implements coil.intercept.Interceptor { *; }

# ================================
# Navigation Component
# ================================
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# ================================
# Serialization
# ================================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ================================
# Parcelable
# ================================
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ================================
# Enum Classes
# ================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================================
# R8 Full Mode Compatibility
# ================================
-keep class com.mytheclipse.quizbattle.** { *; }

# Keep companion objects
-keep class com.mytheclipse.quizbattle.**$Companion { *; }

# Keep sealed classes
-keep class com.mytheclipse.quizbattle.**$* { *; }

# ================================
# Remove Logging in Release
# ================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ================================
# Crashlytics (if added later)
# ================================
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ================================
# General Android
# ================================
-keep class android.** { *; }
-keep class androidx.** { *; }
-dontwarn android.**
-dontwarn androidx.**

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}