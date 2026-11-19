<h1>Morivia <img src="assets/branding/morivia-symbol.png" alt="" height="52" align="right" /><br clear="right" /></h1>

A personal media app for your Android phone and tablet.
Bring your own sources: Morivia turns your addons into a library with artwork, profiles, subtitles and your place saved on every device. Licensed GPLv3, like the project it is built on.

**Version <!-- version -->1.6<!-- /version -->** · [Download the latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest) · [Changelog](CHANGELOG.md)

## What's new in 1.6

- Local builds 29–31: AMOLED black makes every card black and Home, Search and Library pure black; the app icon picker looks like the theme picker; and all 31 app languages now cover the whole app (not addon content or integrations).

- Local build 28 matches initials colours exactly to the Appearance themes and app icons, everywhere in the app, and fixes profile sync with Nuvio.

- Local build 27 keeps the PIN keypad in place during confirmation and matches Add Profile to the management controls.

- Local build 26 restores the Add Profile circle, groups avatar choices and uses the active theme for the edit pencil.

- **Local test build 25:** The profile chooser follows the supplied Nuvio reference for typography, outlined profile circles and management controls, retaining Morivia's name, logo and background. Nuvio sign-in remains locked to build 24. This build has not been published.
- **A new Home.** One hero at the top with the Home button, the title, genre, year and description on the left, View Details, info and next. Below it Continue Watching, **Movies Based on Your Genres** and **Series Based on Your Genres** (from the movie and the series you watched last), a large Featured row, Popular and Featured movies and series, and every other catalog with up to 30 titles each, larger posters and Show all, in one page you scroll freely.
- **Search and Library like the earlier Morivia app.** Search has the Search button, a "Movies and Shows" field, Trending and genre tiles. Library has Recently Added, Movies, TV Shows and Continue Watching, a count of movies and shows, Sort, and a grid of three posters per row.
- **New settings.** Settings → Layout has Home & Layout (Play Trailers in Hero, Auto-Rotate Hero, the genre rows, posters, Continue Watching and rows), Search and Library.
- **Profiles.** The profile picker follows Nuvio's layout. Manage Profiles opens and closes editing and Add Profile, a profile with a PIN asks for it before it can be edited, a new PIN must be entered twice, guests cannot set a PIN, and the picker shows the theme and app icon of the profile used last.
- **The app no longer closes when you change the app icon**; the new icon appears when you leave the app.
- **Install once more:** 1.6 is signed with Morivia's new private key, so remove the old Morivia before installing it. Sign in with Nuvio again and your profiles, library and progress come back. Later versions update as usual.

See the [changelog](CHANGELOG.md) for every change.

## Get Morivia

- Download `Morivia-Android-X.Y-arm64-v8a.apk` from the [latest release](https://github.com/OckePocke9/MoriviaMobile/releases/latest), open it and allow the install. New versions install over the old one and keep your data, and the app shows a banner on Home when one is out. Android 7.0 or newer on a 64-bit ARM device.
- A fresh install starts signed out: sign in with your Nuvio account or continue as a guest.

The app ID is `com.morivia.app`, so Morivia can be installed next to Nuvio.

## What Morivia does

Morivia is a modified version of [Nuvio Mobile](https://github.com/NuvioMedia/NuvioMobile), a Kotlin Multiplatform and Compose Multiplatform app, with its own look and features:

- **Addon catalogs** – add any Stremio addon by its `manifest.json` link; its catalogs become rows on Home.
- **Home** – one hero (with trailers if you like), Continue Watching, rows based on the genres you watch, Featured, Popular and every addon catalog in one page you scroll freely. Settings → Layout → Home & Layout changes the hero, posters and rows.
- **Search** – one "Movies and Shows" field across every search-capable catalog, with Trending, genre tiles and Discover by type, catalog and genre.
- **Library** – Recently Added, Movies, TV Shows and Continue Watching in a poster grid, sorted by date added, name or recently watched, with Trakt, Simkl or MDBList as the source if you use them.
- **Profiles** – up to six profiles with their own library, progress, theme and app icon, a PIN lock that also protects editing, and a profile picker in the style of the earlier Morivia app.
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
