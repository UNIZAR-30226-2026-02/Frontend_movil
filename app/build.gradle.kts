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
}