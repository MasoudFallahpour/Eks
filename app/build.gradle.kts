import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
}

val props = Properties()
val localPropertiesFile = File(rootProject.rootDir, "local.properties")
if (localPropertiesFile.exists()) {
    props.load(FileInputStream(localPropertiesFile))
} else {
    throw GradleException("local.properties file not found.")
}
val sp: String = props.getProperty("storePassword") ?: ""
val kp: String = props.getProperty("keyPassword") ?: ""

android {
    namespace = "ir.fallahpoor.eks"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ir.fallahpoor.eks"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../Eks.jks")
            storePassword = sp
            keyAlias = "key0"
            keyPassword = kp
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs["release"]
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        animationsDisabled = true

        unitTests {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
            }
        }
    }

    packaging { resources { excludes += "/META-INF/*" } }
}

base {
    archivesName = "Eks"
}

dependencies {
    implementation(project(":data"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.workManager)
    implementation(libs.timber)
    implementation(libs.immutableCollections)

    implementation(libs.hilt.android)
    implementation(libs.hilt.workManager)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.liveData)

    lintChecks(libs.composeLintChecks)

    testImplementation(project(":commonTest"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    androidTestImplementation(project(":commonTest"))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.compose.uiTest.junit)
    debugImplementation(libs.androidx.compose.uiTest.manifest)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.bundles.espresso)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.workManager.test)
    kspAndroidTest(libs.hilt.android.compiler)
}