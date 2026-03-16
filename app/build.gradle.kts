plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.secretpanda"

    // ¡AQUÍ ESTÁ EL CAMBIO CLAVE!
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.secretpanda"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
// Librería STOMP para Android
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
// RxJava para manejar las suscripciones asíncronas de STOMP
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    // para convertir JSON en java
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
}