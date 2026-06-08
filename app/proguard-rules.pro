# Keep Room entities
-keep class com.offlinelearninghub.data.local.** { *; }

# Keep DataStore
-keep class * extends androidx.datastore.preferences.core.Preferences { *; }

# Keep ExoPlayer/Media3
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# Keep PDF Viewer
-dontwarn com.github.barteksc.pdfviewer.**
-keep class com.github.barteksc.pdfviewer.** { *; }
