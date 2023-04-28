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
-keep public class com.atomykcoder.atomykplay.data.** { *; }

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