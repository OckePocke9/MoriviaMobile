<p align="center">
  <img src="assets/branding/morivia-symbol.png" alt="" width="128" />
</p>

<h1 align="center">Morivia</h1>

<p align="center">
  Your media. Your layout. One app for Android and iOS.<br />
  Android phones and tablets · iPhone · iPad
</p>

<p align="center">
  <b>Version <!-- version -->1.3.0<!-- /version --></b> ·
  <a href="https://github.com/OckePocke9/MoriviaMobile/releases/latest"><b>Download the latest release</b></a> ·
  <a href="CHANGELOG.md">Changelog</a>
</p>

---

Morivia is a personal media app for phones and tablets. It brings your addon catalogs, search, profiles, library, watch progress and a full-featured player together in one place, with the same experience on Android and iOS.

## What's new in 1.3.0

- **Subtitles that follow the actors.** Addon subtitles are now lined up with the speech in the film. While you watch, Morivia listens to when people talk and, after about two minutes, moves the subtitle so it appears when the words are spoken. It also fixes subtitles that slowly drift away because they were made for another frame rate. This works in every built-in player: ExoPlayer and MPV on Android, and MPV on iPhone and iPad. When a match is not certain, the subtitle keeps its own timing; the subtitle shows **Auto synced** when it has been corrected.
- **Full screen from the start.** Every movie and episode now starts in *Fill*, using the whole screen without zooming in. The resize button still switches between Fit, Fill and Zoom while you watch.

Version 1.2.0 brought in everything from Nuvio 1.1.0 beta 1: library filters, tracking addons, better subtitle styling, more reliable playback and new languages.

See the [changelog](CHANGELOG.md) for every change.

## Features

- **Catalogs from addons** – browse movies and series from the addons you install.
- **Search and discover** – find titles across every installed catalog.
- **Profiles** – separate libraries, progress and settings for everyone in the household.
- **Library and Continue Watching** – pick up where you left off, with filters for genre, year and watched status.
- **Player** – styled subtitles that sync to the speech automatically, audio and subtitle language preferences, picture-in-picture and external players.
- **In your language** – the interface is translated into more than 25 languages.
- **Sync** – optional Nuvio account sign-in and integrations such as Trakt, Simkl, MDBList and TMDB.

## Download

Every release contains both apps:

| Device | File | Requires |
| --- | --- | --- |
| Android phones and tablets | `Morivia-Android-X.Y.Z-arm64-v8a.apk` | Android 7.0 or newer on a 64-bit ARM device |
| iPhone and iPad | `Morivia-iOS-X.Y.Z.ipa` | iOS / iPadOS 16.1 or newer |

The repository is private. Sign in to GitHub with an account that has access to download the files.

### Install on Android

1. Open the [latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest) on your phone or tablet.
2. Download the `.apk` file under **Assets**.
3. Open it and allow installation from your browser or file manager when Android asks.

New versions install over the old one and keep your data. The app ID is `com.morivia.app`, so Morivia can be installed next to Nuvio.

### Install on iPhone and iPad

The iOS app is published unsigned, because it is not distributed through the App Store. A signing tool installs it with your own Apple ID:

1. Download the `.ipa` file from the [latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest).
2. Install it with a sideloading tool such as [AltStore](https://altstore.io), [SideStore](https://sidestore.io) or [Sideloadly](https://sideloadly.io).
3. With a free Apple ID the app must be refreshed every 7 days; a paid Apple Developer account lasts a year.

## Accounts and services

Morivia works without an account in guest mode. Signing in with a Nuvio account uses Nuvio Cloud and keeps Nuvio's own name and logo on that sign-in screen, because it is Nuvio's service. No passwords or private keys are included in the app. Integrations such as Trakt, Simkl, MDBList and TMDB need their own accounts or API keys.

## Releases and versions

Every app change is published as a GitHub release named **Morivia X.Y.Z**. GitHub Actions builds the Android APK and the iOS IPA, checks them and attaches both with this README, the changelog, the license and SHA-256 checksums. A published version is never replaced.

| Change | Example | Use |
| --- | --- | --- |
| Patch | 1.1.0 → 1.1.1 | Bug fixes and small text or design adjustments |
| Minor | 1.1.0 → 1.2.0 | New features |
| Major | 1.1.0 → 2.0.0 | A redesign or a large change to the app |

To release a change:

```sh
python3 scripts/bump-version.py patch   # or minor / major
```

The script updates the version in Gradle, Xcode and this README and starts a new *What's new* section here. Describe the update in that section and in a `## X.Y.Z` section of [CHANGELOG.md](CHANGELOG.md), then commit and push to `main`. The release is not built until both are written. See [AGENTS.md](AGENTS.md) for the full rules.

## Build from source

You need Java 21 or newer and the Android SDK named in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). Create an empty `local.properties` file in the repository root first.

**Android** (Windows, macOS or Linux):

```sh
./gradlew :androidApp:assembleFullRelease -Pnuvio.android.distribution=full -PreleaseMinifyEnabled=false
```

The APK is written to `androidApp/build/outputs/apk/full/release/`.

**iOS** (macOS with Xcode):

```sh
./scripts/prepare-ios-dependencies.sh   # downloads MPVKit and the streaming engine
./scripts/build-ios-ipa.sh              # writes an unsigned IPA to build/ios-ipa/
```

You can also open `iosApp/iosApp.xcodeproj` in Xcode and set your team in `iosApp/Configuration/Config.xcconfig` to run Morivia on your own device.

Optional settings, such as a TMDB key or another Nuvio backend, go in `local.properties`. GitHub Actions reads them from the repository secret `MORIVIA_LOCAL_PROPERTIES_BASE64`. `NUVIO_SUPABASE_URL` and `NUVIO_SUPABASE_ANON_KEY` must be set together; leaving both out uses Nuvio Cloud.

## Project structure

| Folder | Contents |
| --- | --- |
| `composeApp/` | Shared Kotlin and Compose Multiplatform code, with the Android and iOS implementations |
| `androidApp/` | Android application, manifest and launcher icons |
| `iosApp/` | iOS application (Xcode project, Swift player and widgets) |
| `baselineprofile/` | Android startup and performance profiles |
| `assets/branding/` | Morivia logos and launcher artwork |
| `scripts/` | Version, build and debugging helpers |
| `docs/` | Project guides and the addon reference |
| `vendor/`, `MPVKit` | External player dependencies |
| `.github/` | Release and pull request builds, issue templates |
| `.ci/` | Development signing key for Android |

## Languages

GitHub's language bar shows the languages of the source code, not the languages of the app's interface (the app itself is translated into many languages).

| Language | Used for |
| --- | --- |
| Kotlin | The app: shared logic and the Compose interface for Android and iOS, plus the Gradle build files (`.gradle.kts`) |
| Swift | The iOS app shell: player, picture-in-picture, Now Playing, widgets and system integration |
| Shell | Build, iOS packaging and debugging scripts in `scripts/` |
| Python | The version script and an audio test helper in `scripts/` |
| Objective-C and C | Small bridges between Kotlin and iOS (app icons and encryption) |
| YAML | GitHub Actions workflows and issue templates |

Python, Objective-C and C are only a few kilobytes, so they are left out of GitHub's language bar (see `.gitattributes`) instead of being grouped under "Other".

## Credits and license

Morivia is an independent, modified version of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile) and is released under the [GNU General Public License v3.0](LICENSE). The original authors' copyright and third-party notices are kept. Some internal code names, such as the `com.nuvio.app` Kotlin package and the `nuvio://` link scheme, stay unchanged so the app keeps working with Nuvio's services.
