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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Stripe SDK ProGuard rules
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Stripe SDK classes
-keep class com.stripe.android.** { *; }
-keep class com.stripe.android.model.** { *; }
-keep class com.stripe.android.view.** { *; }

# Keep required classes for payment methods
-keepclassmembers class * implements com.stripe.android.view.CardInputListener {
    <methods>;
}

# Keep PaymentConfiguration
-keep class com.stripe.android.PaymentConfiguration { *; }

# Keep 3DS2 classes
-keep class com.stripe.android.stripe3ds2.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Room entities
-keep class com.example.clicknote.data.local.entity.** { *; }

# Keep Retrofit and network models
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Stripe
-keep class com.stripe.android.** { *; }

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }

# Keep TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }

# Keep Google Cloud Storage
-keep class com.google.cloud.storage.** { *; }

# Keep OpenAI client
-keep class com.aallam.openai.** { *; }

# Keep Vosk
-keep class org.vosk.** { *; }

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep MixPanel
-keep class com.mixpanel.android.** { *; }

# Keep Media3 ExoPlayer
-keep class androidx.media3.** { *; }

# Keep JTransforms
-keep class edu.emory.mathcs.jtransforms.** { *; }

# Keep Google Play Billing
-keep class com.android.billingclient.** { *; }

# Keep Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-keep class retrofit2.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Keep libphonenumber
-keep class com.google.i18n.phonenumbers.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}