plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "ir.fallahpoor.eks.data"
    compileSdk = SdkVersions.compileSdk

    defaultConfig {
        minSdk = SdkVersions.minSdk
        targetSdk = SdkVersions.targetSdk
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    // TODO remove it once the reason for this Lint error is found
    lint {
        disable += "DialogFragmentCallbacksDetector"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

dependencies {
    implementation(kotlinStdLib)
    implementation(core)
    implementation(dataStore)
    implementation(Lifecycle.liveData)
    implementation(Coroutines.android)
    implementation(inject)
    implementation(jsoup)

    implementation(Room.runtime)
    kapt(Room.compiler)
    implementation(Room.ktx)

    testImplementation(junit)
    testImplementation(truth)
    testImplementation(Coroutines.test)
    testImplementation(coreTesting)
    testImplementation(AndroidXTest.core)
    testImplementation(AndroidXTest.junit)
    testImplementation(robolectric)
    testImplementation(Mockito.inline)
    testImplementation(reflection)
}