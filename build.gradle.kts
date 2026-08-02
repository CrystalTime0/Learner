// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    // Plugin pour KSP (nécessaire pour Room)
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false

    // Plugin pour Firebase
    id("com.google.gms.google-services") version "4.5.0" apply false
}