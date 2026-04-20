# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.filzaardika.tradingbot.**$$serializer { *; }
-keepclassmembers class com.filzaardika.tradingbot.** {
    *** Companion;
}
-keepclasseswithmembers class com.filzaardika.tradingbot.** {
    kotlinx.serialization.KSerializer serializer(...);
}
