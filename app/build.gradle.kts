plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.secretpanda"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.secretpanda"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }

        unitTests.all {
            val testTask = this as? org.gradle.api.tasks.testing.Test
            testTask?.jvmArgs("-Xmx2048m", "-Xms512m")
        }
    }



    // Configuración de la firma compartida para el ID cliente de Google OAuth
    signingConfigs {
        getByName("debug") {
            // Fichero con la firma
            storeFile = file("compartido.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        // Se le asigna al modo debug
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/xml_panda/home/achievements",
                "src/main/xml_panda/home/classification",
                "src/main/xml_panda/home/profile",
                "src/main/xml_panda/shop",
                "src/main/xml_panda/auth",
                "src/main/xml_panda/customization",
                "src/main/xml_panda/game/createMatch",
                "src/main/xml_panda/game/join",
                "src/main/xml_panda/game/match",
                "src/main/xml_panda/game/waitingRoom",
                "src/main/xml_panda/home/options"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.espresso.contrib)

    // Testing básico
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 🔥 NUEVAS DEPENDENCIAS PARA EL TEST (Navegación y MockBackend) 🔥
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    // Peticiones HTTP
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Librería STOMP para Android
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    // RxJava para manejar las suscripciones asíncronas de STOMP
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // para convertir JSON en java
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Glide para carga de imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.lifecycle:lifecycle-process:2.6.2")

    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
}