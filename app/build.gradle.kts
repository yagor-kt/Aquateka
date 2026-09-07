import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    id("androidx.room") version "2.7.0"
}

val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(localFile.inputStream())
}

android {
    namespace = "com.subefu.aquateka"
    compileSdk = 36

    signingConfigs {
        create("customDebug") {
            val props = localProperties
            storeFile = file(props.getProperty("debug.storeFile", "key_store.jks"))
            storePassword = props.getProperty("debug.storePassword", "3310S0209")
            keyAlias = props.getProperty("debug.keyAlias", "key0")
            keyPassword = props.getProperty("debug.keyPassword", "3310S0209")
        }
    }

    defaultConfig {
        applicationId = "com.subefu.aquateka"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MAP_API_kEY", "\"${getMapApiKey()}\"")

        ndk {
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true     // Включает R8 (ProGuard) для удаления лишнего кода карт
            isShrinkResources = true   // Удаляет неиспользуемые картинки и ресурсы
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("customDebug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}


fun getMapApiKey(): String{
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if(localPropertiesFile.exists()){
        localPropertiesFile.inputStream().use { properties.load(it) }
    }
    return properties.getProperty("MAP_API_KEY", "")
}
android.buildFeatures.buildConfig = true

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.scenecore)
    ksp(libs.androidx.room.compiler)  // Используем ksp вместо kapt

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("com.yandex.android:maps.mobile:4.2.2-full")

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}