<p align="center">
  <img src="assets/branding/morivia-symbol.png" alt="" width="128" />
</p>

<h1 align="center">Morivia</h1>

<p align="center">
  Your media. Your layout. One app for Android and iOS.<br />
  Android phones and tablets · iPhone · iPad
</p>

<p align="center">
  <b>Version <!-- version -->1.7.0<!-- /version --></b> ·
  <a href="https://github.com/OckePocke9/MoriviaMobile/releases/latest"><b>Download the latest release</b></a> ·
  <a href="CHANGELOG.md">Changelog</a>
</p>

---

Morivia is a personal media app for phones and tablets. It brings your addon catalogs, search, profiles, library, watch progress and a full-featured player together in one place, with the same experience on Android and iOS.

## What's new in 1.7.0

- **Who's watching follows your theme.** The background glow and the Morivia logo take the colours of the theme you choose, and the active profile gets a ring in the theme colours.
- **Clearer profile picker.** The content sits in the middle of the screen, every profile circle has the same ring, initials are a white letter on the profile colour, profiles with a PIN show a padlock, Add Profile is always there when there is room, a long press opens a profile's settings, and a line at the bottom shows whether your profiles sync with Nuvio or stay on this device.
- **Theme logos keep their details.** Every theme's app icon and logo is the original Morivia logo – ribbons, shading and play button – in the theme's colours.
- **Appearance like the earlier Morivia app.** The current theme at the top, nine themes with an arrow for the rest, Reset to Original, Custom theme, Match app icon to theme and a theme preview. Settings show the theme background behind black cards, and AMOLED Black is on by default.

See the [changelog](CHANGELOG.md) for every change.

## Features

- **Catalogs from addons** – browse movies and series from the addons you install.
- **Search and discover** – find titles across every installed catalog.
- **Profiles** – separate libraries, progress and settings for everyone in the household.
- **Library and Continue Watching** – pick up where you left off, with filters for genre, year and watched status.
- **Player** – styled subtitles that follow the speech for the whole film, audio and subtitle language preferences, chapters, picture-in-picture and external players.
- **In your language** – the interface is translated into 31 language choices (with English fallback for untranslated text).
- **Sync** – optional Nuvio account sign-in and integrations such as Trakt, Simkl, MDBList and TMDB.

## Download

| Device | File | Requires |
| --- | --- | --- |
| Android phones and tablets | `Morivia-Android-X.Y.Z-arm64-v8a.apk` | Android 7.0 or newer on a 64-bit ARM device |
| iPhone and iPad | `Morivia-iOS-X.Y.Z.ipa` | iOS / iPadOS 16.1 or newer |

Every release has the Android app. The iOS app needs a Mac to build, so a release has it only when it was built on one; 1.3.0 has both.

This repository is private. The Android app is also published in the public [MoriviaUpdates](https://github.com/OckePocke9/MoriviaUpdates/releases/latest) repository, which holds only the APK and release notes; the app's update banner reads it.

### Install on Android

1. Open the [latest release](https://github.com/OckePocke9/MoriviaUpdates/releases/latest) on your phone or tablet.
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

Every app change is published as a GitHub release named **Morivia X.Y.Z**. GitHub Actions is turned off for this repository, so releases are built and published from a PC: `scripts/publish-release.ps1` runs the Android tests, builds and checks the APK and attaches it with this README, the changelog, the license and SHA-256 checksums. An iOS IPA built on a Mac can be added with `-IpaPath`. The script then copies the APK and release notes to the public [MoriviaUpdates](https://github.com/OckePocke9/MoriviaUpdates) repository (`scripts/publish-update-mirror.ps1`), where the app looks for new versions. A published version is never replaced.

| Change | Example | Use |
| --- | --- | --- |
| Patch | 1.1.0 → 1.1.1 | Bug fixes and small text or design adjustments |
| Minor | 1.1.0 → 1.2.0 | New features |
| Major | 1.1.0 → 2.0.0 | A redesign or a large change to the app |

To release a change:

```powershell
.\scripts\bump-version.ps1 patch   # or minor / major (python3 scripts/bump-version.py does the same)
```

The script updates the version in Gradle, Xcode and this README and starts a new *What's new* section here. Describe the update in that section and in a `## X.Y.Z` section of [CHANGELOG.md](CHANGELOG.md), commit and push to `main`, then publish:

```powershell
.\scripts\publish-release.ps1                      # Android only
.\scripts\publish-release.ps1 -IpaPath <file.ipa>  # with an iOS app built on a Mac
```

The script refuses to publish until the README and changelog are written and the work is pushed. See [AGENTS.md](AGENTS.md) for the full rules.

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

Optional settings, such as a TMDB key or another Nuvio backend, go in `local.properties`. `NUVIO_SUPABASE_URL` and `NUVIO_SUPABASE_ANON_KEY` must be set together; leaving both out uses Nuvio Cloud.

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
| `.github/` | Issue templates and manual-only workflows (GitHub Actions is turned off) |
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
| YAML | Issue templates and the manual-only workflows |

Python, Objective-C and C are only a few kilobytes, so they are left out of GitHub's language bar (see `.gitattributes`) instead of being grouped under "Other".

## Credits and license

Morivia is an independent, modified version of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile) and is released under the [GNU General Public License v3.0](LICENSE). The original authors' copyright and third-party notices are kept. Some internal code names, such as the `com.nuvio.app` Kotlin package and the `nuvio://` link scheme, stay unchanged so the app keeps working with Nuvio's services.
