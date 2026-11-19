<h1>Morivia <img src="assets/branding/morivia-symbol.png" alt="" height="52" align="right" /><br clear="right" /></h1>

A personal media app for your Android phone and tablet.
Bring your own sources: Morivia turns your addons into a library with artwork, profiles, subtitles and your place saved on every device. Licensed GPLv3, like the project it is built on.

**Version <!-- version -->1.5<!-- /version -->** · [Download the latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest) · [Changelog](CHANGELOG.md)

## What's new in 1.5

- **The app icon from the earlier Morivia app.** The home screen icon and the Original app icon use the same logo, at the same size, as the earlier Morivia app, and every theme's icon is that logo in the theme's colours.

See the [changelog](CHANGELOG.md) for every change.

## Get Morivia

- Download `Morivia-Android-X.Y-arm64-v8a.apk` from the [latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest), open it and allow the install. New versions install over the old one and keep your data. Android 7.0 or newer on a 64-bit ARM device.

The app ID is `com.morivia.app`, so Morivia can be installed next to Nuvio. The app shows a banner on Home when a new version is out.

## What Morivia does

Morivia is a modified version of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile), a Kotlin Multiplatform and Compose Multiplatform app, with its own look and features:

- **Addon catalogs** – add any Stremio addon by its `manifest.json` link; its catalogs become rows on Home.
- **Home** – a hero at the top, Continue Watching and every addon catalog in one scrolling page.
- **Search and Discover** – one field for movies and shows across every search-capable catalog, and Discover by type, catalog and genre.
- **Library** – your saved titles, filtered and sorted the way you like, with Trakt, Simkl or MDBList as the source if you use them.
- **Profiles** – up to six profiles with their own library, progress, theme and app icon, a PIN lock and a profile picker in the style of the earlier Morivia app.
- **Themes** – Original and the Morivia colour themes, AMOLED black and an app icon that follows the theme.
- **Accounts and sync** – works as a guest, or sign in with a Nuvio account to sync profiles, library and progress through Nuvio Cloud. Trakt, Simkl, MDBList and TMDB connect with their own accounts or keys.
- **In your language** – 31 language choices, with English for untranslated text.

## Player

- **Two engines** – ExoPlayer and mpv, with external players as an option.
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
./gradlew :androidApp:assembleFullRelease -Pnuvio.android.distribution=full -PreleaseMinifyEnabled=false
```

The APK is written to `androidApp/build/outputs/apk/full/release/`.

### Releases

Releases are named **Morivia Android vX.Y** and go up by 0.1 (0.9, 1.0, 1.1 …). They are built and published from a PC with `scripts/publish-release.ps1`, because GitHub Actions is turned off for this repository. See [AGENTS.md](AGENTS.md) for the full process.

## Contributors

- [OckePocke9](https://github.com/OckePocke9) – owner, design and testing
- [Claude](https://claude.com/claude-code) by Anthropic – development, releases and documentation

## License

[GNU General Public License v3.0](LICENSE). Morivia keeps the original Nuvio authors' copyright and third-party notices. Internal code names such as the `com.nuvio.app` package and the `nuvio://` link scheme stay unchanged so the app keeps working with Nuvio's services.
