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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# SLF4J ProGuard rules

-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*
-dontwarn javax.annotation.**
-dontwarn org.jcodec.**
-dontwarn org.jaudiotagger.**


-keep class org.jaudiotagger.** { *; }
-keep class org.jcodec.** { *; }

# Glide ProGuard rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Music player app ProGuard rules
# keep all model classes
-keep class com.atomykcoder.atomykplay.data.** { *; }
-keep class com.atomykcoder.atomykplay.models.** { *; }

# keep Glide-generated code
-keep public class * implements com.bumptech.glide.GeneratedAppGlideModule {
  public *;
}

# keep Glide's annotated classes
-keep @com.bumptech.glide.annotation.GlideExtension class * {
  *;
}

# keep Glide's options classes
-keep public class * extends com.bumptech.glide.load.Options {
  public static <fields>;
}

# keep Glide's generated API
-keep class * implements com.bumptech.glide.module.GlideModule {
  public void applyOptions(android.content.Context, com.bumptech.glide.GlideBuilder);
  public void registerComponents(android.content.Context, com.bumptech.glide.Glide, com.bumptech.glide.Registry);
}

# keep Glide's annotations
-keepnames @com.bumptech.glide.annotation.* class *
# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation interface com.google.gson.annotations.SerializedName
##---------------End: proguard configuration for Gson  ----------

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontnote okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.**{*;}
-keep interface com.atomykcoder.atomykplay.interfaces.ApiService { *; }


-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
