# Proguard rules for Android Compose and Jetpack Glance
-keepattributes *Annotation*
-dontwarn org.xmlpull.v1.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
