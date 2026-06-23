plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

val buildIos = project.findProperty("build.ios")?.toString()?.toBoolean() ?: false

if (buildIos) {
    apply(plugin = "org.jetbrains.kotlin.native.cocoapods")
}

composeCompiler {
    targetKotlinPlatforms.set(
        setOf(org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm)
    )
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    if (buildIos) {
        iosArm64()
        iosSimulatorArm64()
        iosX64 {
            binaries.all {
                linkerOpts += listOf(
                    "-L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphonesimulator",
                    "-L/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk/usr/lib/swift"
                )
            }
        }

        extensions.configure<org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension> {
            name = "ads"
            summary = "juooy AdMob KMP library — Android & iOS"
            homepage = "https://github.com/juooy/juooy-ads"
            version = "1.0"
            ios.deploymentTarget = "14.0"
            framework {
                baseName = "ads"
                isStatic = false
            }
            pod("Google-Mobile-Ads-SDK") {
                version = "~> 10.0"
                moduleName = "GoogleMobileAds"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {}
        androidMain.dependencies {
            implementation("com.google.android.gms:play-services-ads:24.4.0")
            implementation("com.google.android.ump:user-messaging-platform:3.2.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
            implementation("androidx.compose.ui:ui:1.7.8")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
            implementation("androidx.lifecycle:lifecycle-process:2.8.7")
        }
        if (buildIos) {
            iosMain.dependencies {}
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

android {
    namespace = "kr.co.juooy.ads"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

