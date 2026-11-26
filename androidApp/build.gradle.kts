import java.util.Properties



plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.androidxBaselineProfile)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sentry.android.gradle)
}

val localProps = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}
val releaseStoreFile = localProps.getProperty("MORIVIA_RELEASE_STORE_FILE")?.takeIf { it.isNotBlank() }
val releaseStorePassword = localProps.getProperty("MORIVIA_RELEASE_STORE_PASSWORD")?.takeIf { it.isNotBlank() }
val releaseKeyAlias = localProps.getProperty("MORIVIA_RELEASE_KEY_ALIAS")?.takeIf { it.isNotBlank() }
val releaseKeyPassword = localProps.getProperty("MORIVIA_RELEASE_KEY_PASSWORD")?.takeIf { it.isNotBlank() }
val releaseKeystore = releaseStoreFile?.let(rootProject::file)
fun envOrLocalProperty(key: String): String? =
    providers.environmentVariable(key).orNull?.trim()?.takeIf { it.isNotBlank() }
        ?: localProps.getProperty(key)?.trim()?.takeIf { it.isNotBlank() }

val sentryAuthToken = envOrLocalProperty("SENTRY_AUTH_TOKEN")
val sentryOrg = envOrLocalProperty("SENTRY_ORG")
val sentryProject = envOrLocalProperty("SENTRY_PROJECT")
val sentryMappingUploadEnabled = sentryAuthToken != null && sentryOrg != null && sentryProject != null
val releaseAppVersionName = providers.gradleProperty("morivia.versionName").get()
val releaseAppVersionCode = providers.gradleProperty("morivia.versionCode").get().toInt()
val requestedTaskNames = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }
val buildsReleaseApks = requestedTaskNames.any {
    it.startsWith("assemble", ignoreCase = true) && it.endsWith("Release", ignoreCase = true)
}

android {
    namespace = "com.morivia.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileSdkMinor = libs.versions.android.compileSdkMinor.get().toInt()

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file(".ci/morivia-development.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("release") {
            if (releaseKeystore != null && releaseStorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null) {
                storeFile = releaseKeystore
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.morivia.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = releaseAppVersionCode
        versionName = releaseAppVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("full") {
            dimension = "distribution"
        }
        create("playstore") {
            dimension = "distribution"
        }
    }

    sourceSets.getByName("full") {
        manifest.srcFile("src/full/AndroidManifest.xml")
        jniLibs.directories.add("../composeApp/src/full/jniLibs")
    }

    sourceSets.getByName("androidTest").assets.srcDir("../testFixtures")
    sourceSets.getByName("main").baselineProfiles.directories.add("src/main/generated/baselineProfiles")

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += listOf(
                "lib/*/libc++_shared.so",
                "lib/*/libavcodec.so",
                "lib/*/libavutil.so",
                "lib/*/libswscale.so",
                "lib/*/libswresample.so"
            )
        }
    }

    androidResources {
        noCompress += "cvr"
    }

    splits {
        abi {
            isEnable = buildsReleaseApks
            reset()
            // Every phone and tablet (owner, 2026-11-26): one APK per processor type, and a universal
            // APK for anyone unsure which to take. The in-app updater picks the one for the device.
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true
        }
    }

    buildTypes {
        getByName("release") {
            val minifyRelease = providers.gradleProperty("releaseMinifyEnabled")
                .map(String::toBooleanStrict)
                .getOrElse(true)
            isMinifyEnabled = minifyRelease
            isShrinkResources = minifyRelease
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "../composeApp/proguard-rules.pro",
            )
            // Morivia releases from 1.6 are signed with the private release key set up in
            // local.properties (MORIVIA_RELEASE_*); without it (someone building from source) the
            // development key in .ci/ signs the APK. publish-release.ps1 checks the release key.
            signingConfig = if (releaseKeystore != null && releaseStorePassword != null &&
                releaseKeyAlias != null && releaseKeyPassword != null
            ) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            matchingFallbacks += "release"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        variant.applicationId.set("com.morivia.app")
    }
    onVariants(selector().withBuildType("benchmark")) { variant ->
        variant.applicationId.set("com.morivia.app")
    }
    onVariants { variant ->
        val buildType = variant.buildType.orEmpty()
        if (buildType.startsWith("nonMinified") || buildType.startsWith("benchmark") && buildType != "benchmark") {
            variant.applicationId.set("com.morivia.app")
            variant.signingConfig.setConfig(android.signingConfigs.getByName("debug"))
        }
    }
}

baselineProfile {
    automaticGenerationDuringBuild = false
    saveInSrc = true
    mergeIntoMain = true
    baselineProfileOutputDir = "generated/baselineProfiles"
}

sentry {
    // Only with a Sentry upload configured: sentry-cli cannot run from a path with spaces
    // ("Morivia Android"), and without an upload the mapping ID is never used.
    includeProguardMapping.set(sentryMappingUploadEnabled)
    autoUploadProguardMapping.set(sentryMappingUploadEnabled)
    uploadNativeSymbols.set(false)
    autoUploadNativeSymbols.set(false)
    includeNativeSources.set(false)
    includeSourceContext.set(false)
    autoUploadSourceContext.set(false)
    includeDependenciesReport.set(false)
    telemetry.set(false)
    sentryAuthToken?.let(authToken::set)
    sentryOrg?.let(org::set)
    sentryProject?.let(projectName::set)
    ignoredBuildTypes.set(setOf("debug", "benchmark"))
    autoInstallation {
        enabled.set(false)
    }
    tracingInstrumentation {
        enabled.set(false)
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.runtime)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    baselineProfile(project(":baselineprofile"))
    debugImplementation(libs.compose.uiTooling)
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.compose.foundation)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${libs.versions.composeMultiplatform.get()}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${libs.versions.composeMultiplatform.get()}")
}
