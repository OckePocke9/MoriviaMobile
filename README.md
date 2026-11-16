<p align="center"><img src="assets/branding/morivia-symbol.png" alt="Morivia" width="160" /></p>

<h1 align="center">Morivia Android</h1>
<p align="center">Your media. Your layout. Your Android app.</p>

## Version 1.0.0

A personal Android media app with addon catalogs, search, profiles, a library, subtitles, playback controls and watch progress.

## Install

1. Open this repository's **Latest release: Morivia Android 1.0.0**.
2. Download `Morivia-Android-1.0.0-arm64-v8a.apk` on your phone.
3. Open the APK and allow installation from your browser/file manager when Android asks.

Requires Android 7.0 or newer and a 64-bit ARM device. The app uses package ID `com.morivia.app` and can coexist with Nuvio. This first personal release uses a persistent development signing key.

## Appearance

- Installed name: **Morivia**.
- Launcher: purple, blue and cyan symbol on black.
- In-app and GitHub logo: transparent background.
- Nuvio account sign-in: original provider name and logo.

## Accounts and services

Nuvio account sign-in remains a third-party integration. It requires valid backend configuration; reusing the source does not include backend credentials. Guest mode is available. Catalog and tracking integrations may require their own configuration or API keys.

## Build

Use Java 17 and the Android SDK version defined in `gradle/libs.versions.toml`.

```sh
touch local.properties
./gradlew :androidApp:assembleFullRelease -Pnuvio.android.distribution=full -PreleaseMinifyEnabled=false
```

GitHub Actions builds the Android APK and publishes its matching release. Runtime configuration can be supplied with the repository secret `MORIVIA_LOCAL_PROPERTIES_BASE64`.

## Version policy

| Change | Example | Use |
| --- | --- | --- |
| Patch | 1.0.0 → 1.0.1 | Bugs, crashes and small text/design fixes |
| Minor | 1.0.0 → 1.1.0 | New compatible features |
| Major | 1.0.0 → 2.0.0 | Major redesign or changes to the app experience |

Before every app update, run `python3 scripts/bump-version.py patch`, `minor` or `major`, update `CHANGELOG.md`, then commit. Each release is titled **Morivia Android X.Y.Z** and includes the APK, README and changelog. Existing versions are never overwritten.

## Project structure

- `androidApp`: Android application and manifest.
- `composeApp/src/android*`: Android implementation and distributions.
- `composeApp/src/commonMain` and `fullCommonMain`: shared Kotlin code used by Android.
- `baselineprofile`: Android startup/performance tooling.
- `assets/branding`: Morivia artwork.

## Credits and license

Morivia is an independent modified version of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile). Original authorship, third-party notices and the [GNU GPL v3 license](LICENSE) are preserved. Technical namespaces and service protocols retain compatible identifiers where necessary.
