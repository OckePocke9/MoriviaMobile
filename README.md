<h1>Morivia <img src="assets/branding/morivia-symbol.png" alt="" height="52" align="right" /><br clear="right" /></h1>

A personal media app for Android phones and tablets, iPhone and iPad.
Bring your own sources: Morivia turns your addons into a library with artwork, profiles, subtitles and your place saved on every device. Licensed GPLv3, like the project it is built on.

**Version <!-- version -->1.9<!-- /version -->** · [Download the latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest) · [Changelog](CHANGELOG.md)

## What's new in 1.9

- **iPhone and iPad support.** Install the unsigned iOS package with Sideloadly. The iOS player uses mpv.
- **Trakt and SIMKL.** Connect through Morivia’s own sign-in links and see the last sync in Settings. Trakt has a manual sync button, and SIMKL can renew expired sessions when a refresh token is available.

See the [changelog](CHANGELOG.md) for every change.

## Get Morivia

- Download `Morivia-Android-X.Y-arm64-v8a.apk` from the [latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest), open it and allow the install. New versions install over the old one and keep your data, and the app shows a banner on Home when one is out. Android 7.0 or newer on a 64-bit ARM device.
- **iPhone and iPad:** Download `Morivia-iOS-X.Y-release.ipa` from the same release. Requires iOS or iPadOS 16.1 or newer. Install the unsigned IPA with [Sideloadly](https://sideloadly.io/) on a PC or Mac and sign it with your Apple Account.
- A fresh install starts signed out: sign in with your Nuvio account or continue as a guest.

The app ID is `com.morivia.app`, so Morivia can be installed next to Nuvio.

## What Morivia does

Morivia is a modified version of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile), a Kotlin Multiplatform and Compose Multiplatform app, with its own look and features:

- **Addon catalogs** – add any Stremio addon by its `manifest.json` link; its catalogs become rows on Home.
- **Home** – one hero (with trailers if you like), Continue Watching, rows based on the genres you watch, Featured, Popular and every addon catalog in one page you scroll freely. Settings → Layout → Home & Layout changes the hero, posters and rows.
- **Search** – one "Movies and Shows" field across every search-capable catalog, with Trending, genre tiles and Discover by type, catalog and genre.
- **Library** – Recently Added, Movies, TV Shows and Continue Watching in a poster grid, sorted by date added, name or recently watched, with Trakt, Simkl or MDBList as the source if you use them.
- **Profiles** – up to six profiles with their own library, progress, theme, app icon and initials colour, a PIN lock that also protects editing, and a profile picker in the style of the earlier Morivia app.
- **Themes** – Original, 31 Morivia colour themes and a custom colour, AMOLED black, and 31 matching app icons that can follow the theme.
- **Watch Party** – watch together in rooms of up to six, with playback that starts and stays in sync with the host.
- **Accounts and sync** – works as a guest, or sign in with a Nuvio account to sync profiles, library and progress through Nuvio Cloud. Trakt, Simkl, MDBList and TMDB connect with their own accounts or keys.
- **In your language** – the whole app in 31 languages, plus your device language. Addon content keeps its own language.

## Player

- **Playback engines** – Android offers ExoPlayer and mpv. iPhone and iPad use mpv. External playback options vary by platform.
- **Subtitles** – styled subtitles, automatic sync that follows the speech for the whole film, and preferred audio and subtitle languages.
- **Controls** – chapters, playback speed, aspect fit and zoom, picture-in-picture and Continue Watching that remembers where you stopped.

## Build from source

```bash
git clone https://github.com/OckePocke9/MoriviaMobile.git
cd MoriviaMobile
```

Create an empty `local.properties` in the repository root. Optional keys, such as a TMDB key or another Nuvio backend, go there and never enter the repository.

Requires Java 21 or newer and the Android SDK (Android Studio is easiest).

```bash
./gradlew :androidApp:assembleFullRelease -Pmorivia.android.distribution=full
```

The APK is written to `androidApp/build/outputs/apk/full/release/`.

### iPhone and iPad test build

Run the **Morivia iPhone and iPad** workflow by hand in GitHub Actions (Actions, Morivia iPhone and iPad, Run workflow). It builds on GitHub's free macOS runners and saves an unsigned device IPA in the workflow artifacts. To install it, connect your iPhone or iPad to your Windows PC, open the IPA in [Sideloadly](https://sideloadly.io/), and sign in with your Apple Account. A free account can sign the app for seven days at a time.

### Releases

Releases are named **Morivia Android vX.Y** and go up by 0.1 (0.9, 1.0, 1.1 …). The Android APK is built on a PC, and the iOS IPA is built by hand with the **Morivia iPhone and iPad** workflow in GitHub Actions. Attach the matching IPA with `scripts/publish-release.ps1 -IpaPath <file>`. See [AGENTS.md](AGENTS.md) for the full process.

## Contributors

- [OckePocke9](https://github.com/OckePocke9) – owner, design and testing
- [Claude](https://claude.com/claude-code) by Anthropic – development, releases and documentation

## License

[GNU General Public License v3.0](LICENSE). Morivia keeps the original Nuvio authors' copyright and third-party notices. Internal code names such as the `com.nuvio.app` package and the `nuvio://` link scheme stay unchanged so the app keeps working with Nuvio's services.
