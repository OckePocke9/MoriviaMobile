<p align="center"><img src="assets/branding/morivia-symbol.png" alt="Morivia" width="160" /></p>

<h1 align="center">Morivia Android</h1>
<p align="center">Your media. Your layout. Your Android app.</p>

## Version 1.0.1

A personal Android media app with addon catalogs, search, profiles, a library, subtitles, playback controls and watch progress.

## Install

1. Open [Latest release: Morivia Android 1.0.1](https://github.com/OckePocke9/Morivia-Android/releases/latest).
2. Download `Morivia-Android-1.0.1-arm64-v8a.apk` on your phone.
3. Open the APK and allow installation from your browser/file manager when Android asks.

Requires Android 7.0 or newer and a 64-bit ARM device. The app uses package ID `com.morivia.app` and can coexist with Nuvio. This first personal release uses a persistent development signing key.

## Appearance

- Installed name: **Morivia**.
- Launcher: purple, blue and cyan symbol on black.
- In-app and GitHub logo: transparent background.
- Nuvio account sign-in: original provider name and logo.

## Accounts and services

Nuvio account sign-in remains a third-party integration. Email/password and Sign in with link use the official Nuvio Cloud endpoint and its publicly documented publishable client key by default. No account password or administrative key is bundled. Guest mode is available. Catalog and tracking integrations may require their own configuration or API keys.

## Build

Use Java 17 and the Android SDK version defined in `gradle/libs.versions.toml`.

```sh
touch local.properties
./gradlew :androidApp:assembleFullRelease -Pnuvio.android.distribution=full -PreleaseMinifyEnabled=false
```

GitHub Actions builds the Android APK and publishes its matching release. Optional runtime overrides can be supplied with the repository secret `MORIVIA_LOCAL_PROPERTIES_BASE64`. Set `NUVIO_SUPABASE_URL` and `NUVIO_SUPABASE_ANON_KEY` together; leaving both unset uses Nuvio Cloud. Partial overrides fail the build.

## Version policy

| Change | Example | Use |
| --- | --- | --- |
| Patch | 1.0.0 → 1.0.1 | Bugs, crashes and small text/design fixes |
| Minor | 1.0.0 → 1.1.0 | New compatible features |
| Major | 1.0.0 → 2.0.0 | Major redesign or changes to the app experience |

Before every app update, run `python3 scripts/bump-version.py patch`, `minor` or `major`, update `CHANGELOG.md`, then commit. Each release is titled **Morivia Android X.Y.Z** and includes the APK, README and changelog. Existing versions are never overwritten.

## Project structure

| Folder | Contents |
| --- | --- |
| androidApp/ | Android application and manifest |
| composeApp/ | App logic, Compose interface and Android implementations |
| baselineprofile/ | Android startup and performance tooling |
| assets/branding/ | Morivia logos and launcher artwork |
| docs/ | Project guides and addon reference |
| scripts/ | Versioning and Android helper commands |
| gradle/ | Gradle wrapper and dependency catalog |
| testFixtures/ | Subtitle test samples |
| vendor/ | External Android dependencies |
| .github/ | Builds, releases and repository automation |
| .ci/ | Personal development-signing support |

All app source is in the app folders. README, LICENSE, CHANGELOG, AGENTS and Gradle entry-point files remain at root because GitHub and build tools expect them there. See [project documentation](docs/README.md).

## Languages and formats

GitHub Languages describes source code, not the languages available in the app interface.

| Language or format | Purpose |
| --- | --- |
| Kotlin | Android app logic and the Compose interface |
| Kotlin DSL (.gradle.kts) | Build and dependency configuration; counted as Kotlin |
| XML | Android manifest, themes, resources and translated text |
| Python | Versioning and helper scripts |
| Shell / Batch | Build and developer commands |
| YAML | GitHub Actions workflows |
| JSON / TOML | Configuration, metadata and dependency versions |

Swift belonged to the removed iOS application. It is no longer part of main. GitHub groups small helper-language shares under Other; generated, vendored and data files may be excluded from its language statistics.

## Branches

main is the independent Morivia Android development and release branch. cmp-rewrite is an inherited upstream branch with 44 commits not present in main, including subtitle, tracking, TorBox and translation work. It is not needed to build Morivia, but is retained for reviewing useful upstream changes. Do not merge it wholesale, since that would reintroduce other-platform files and upstream branding.

## Credits and license

Morivia is an independent modified version of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile). Original authorship, third-party notices and the [GNU GPL v3 license](LICENSE) are preserved. Technical namespaces and service protocols retain compatible identifiers where necessary.
