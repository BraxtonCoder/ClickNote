import java.util.Properties
import java.io.FileInputStream

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.clicknote"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: keystoreProperties["KEYSTORE_PASSWORD"]?.toString()
                ?: throw GradleException("Required KEYSTORE_PASSWORD not found. Add it to keystore.properties or set it as an environment variable.")
            keyAlias = "clicknote"
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: keystoreProperties["KEY_PASSWORD"]?.toString()
                ?: throw GradleException("Required KEY_PASSWORD not found. Add it to keystore.properties or set it as an environment variable.")
        }
    }

    defaultConfig {
        applicationId = "com.example.clicknote"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"${project.findProperty("OPENAI_API_KEY") ?: ""}\""
        )
        buildConfigField(
            "String",
            "FIREBASE_WEB_API_KEY",
            "\"AIzaSyCnNGLFYihnlbFzlMbKdziQDzByjBEaCH0\""
        )
        buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"pk_test_51Qm0e4GWC5QPksKB0jW6MM63LlRgUW8pXYwXBCp604Tx7LZKd7a97t0imGcNLVUUlkd534MuJze90EeMwpLIREqB000J8ydsf0\"")

        // Room schema location
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

    buildTypes {
        debug {
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        disable += "ProtectedPermissions"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        dataBinding = false
        viewBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Exclude conflicting files
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("build/generated/ksp/main/kotlin")
        }
        getByName("test") {
            java.srcDirs("build/generated/ksp/test/kotlin")
        }
        getByName("debug") {
            java.srcDirs("build/generated/ksp/debug/kotlin")
        }
        getByName("release") {
            java.srcDirs("build/generated/ksp/release/kotlin")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Stripe SDK
    val stripeVersion = "20.35.0"
    implementation("com.stripe:stripe-android:$stripeVersion")
    implementation("com.stripe:payments-core:$stripeVersion")
    implementation("com.stripe:paymentsheet:$stripeVersion")
    
    // TensorFlow Lite dependencies for Whisper
    val tensorflowVersion = "2.14.0"
    implementation("org.tensorflow:tensorflow-lite-task-audio:0.4.4")
    implementation("org.tensorflow:tensorflow-lite:$tensorflowVersion")
    implementation("org.tensorflow:tensorflow-lite-gpu:$tensorflowVersion")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    
    // AWS
    implementation("com.amazonaws:aws-android-sdk-s3:2.73.0") {
        exclude(group = "com.google.android", module = "android")
    }
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.73.0") {
        exclude(group = "com.google.android", module = "android")
    }
    
    // Azure
    implementation("com.azure:azure-storage-blob:12.25.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("com.azure:azure-identity:1.11.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    
    // Google Cloud Storage
    implementation("com.google.cloud:google-cloud-storage:2.32.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "io.grpc", module = "grpc-core")
        exclude(group = "com.google.guava", module = "guava")
    }
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.datastore:datastore-preferences-core:1.0.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    
    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // OpenAI
    implementation("com.aallam.openai:openai-client:3.7.0")
    implementation("io.ktor:ktor-client-android:2.3.8")
    
    // Vosk (offline speech recognition)
    implementation("com.alphacephei:vosk-android:0.3.47") {
        exclude(group = "net.java.dev.jna", module = "jna")
    }

    // Gson for JSON handling
    implementation("com.google.code.gson:gson:2.10.1")
    
    // MixPanel Analytics
    implementation("com.mixpanel.android:mixpanel-android:7.3.1")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Media3 ExoPlayer
    val media3Version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media3:media3-common:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")

    // FFTW library for FFT calculations
    implementation("com.github.wendykierp:JTransforms:3.1")

    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:6.1.0")

    // Retrofit for Stripe API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("io.michaelrocks:libphonenumber-android:8.13.25")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }
}