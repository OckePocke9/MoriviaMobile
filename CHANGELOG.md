# Changelog

## 1.7.1

- The Original launcher icon, adaptive foreground, app icon, in-app symbol, iOS app icon and the README logo are redrawn from the earlier Morivia app's launcher artwork (`ic_launcher_symbol.png`, the same artwork as the logo on the profile picker), placed as there: the symbol in a 22 % inset on black.
- Every theme's icons are recoloured again from the new artwork with `scripts/recolor-theme-icons.ps1`.
- Raise both platforms to version 1.7.1, build 17.

## 1.7.0

- Profile picker, Add Profile, Edit Profile, the launch overlay and every Settings page draw the earlier app's background in the selected theme's colours (`profileGlowColors`: the theme accent and the last gradient colour at 32 % over black; Original keeps #2F0451 / #004051). Settings use it through `MoriviaThemedBackground` with a transparent screen background, so cards keep their colour.
- The logo next to "Morivia" on the profile picker follows the theme (`morivia_brand_symbol_<colour>.png`, `AppIconOption.brandSymbolResource`); custom colours use the closest preset.
- Profile picker: content centred vertically; the same ring on every profile circle, the active profile's ring in the theme gradient; initials as a white letter on the profile colour; a see-through back button with a thin light edge; a padlock badge on profiles with a PIN; the Add Profile tile whenever there is room (not only in Manage Profiles); long press opens Edit Profile; a short grow animation on tap; an account line at the bottom ("Signed in with Nuvio · profiles sync" or "Guest mode · profiles stay on this device").
- Theme app icons and logos keep the original logo's details (ribbons, shading, play button) and only change colour: new `scripts/recolor-theme-icons.ps1` recolours the original artwork for the Android launcher and adaptive foreground, the iOS app icons, the in-app symbols and the profile logo. `scripts/generate-wordmarks.ps1` now draws wordmarks for all 14 colours, and each theme uses its own wordmark.
- Appearance gains the earlier app's settings: "Current theme", nine themes with an arrow for the rest, Reset to Original, a Custom theme button, Match app icon to theme (on by default, kept on the device; with it off, themes and app icons are chosen separately) and a theme preview. English, Swedish and Finnish texts.
- AMOLED Black is on by default, as in the earlier app.
- Raise both platforms to version 1.7.0, build 16.

## 1.6.2

- Profile picker (Who's watching), Add Profile and Edit Profile use the background of the earlier Morivia app (1.4.130) with its Original theme: a violet glow (#920CFC at 32 %) at the top left and a cyan glow (#00C8FC at 32 %) at the top right on black (`Modifier.earlierMoriviaBackground()`). The launch overlay uses it too; supporter background images are unchanged. Edit and Add Profile keep their black cards.
- Profile picker: the earlier app's logo (`morivia_brand_symbol`, 34 x 28 dp) with "Morivia" at 25 sp and "Who's watching?" at 30 sp, both weight 800; names at 17 sp and the Manage Profiles button at 15.5 sp, weight 600.
- The earlier app's font Plus Jakarta Sans (variable, SIL Open Font License; licence in `androidMain/assets/licenses/plus-jakarta-sans-OFL.txt`) for those texts.
- The earlier app's colours: near-black initials circles tinted 14 % with the profile colour and a 55 % ring, a near-black add tile with a dark grey ring, and grey Manage Profiles / dark Done buttons. Sizes and spacing are unchanged.
- Raise both platforms to version 1.6.2, build 15.

## 1.6.1

- Player: back to Nuvio's original controls in every player (ExoPlayer, libmpv on Android and iOS). The "Bio Pro" design (`PlayerCinemaControls`) and its strings are removed, because it covered too much of the film.
- Kept from 1.6.0: whole-film speech sync for subtitles, Fit as the start mode and no stretching (Fill crops), and every other 1.6.0 change.
- Raise both platforms to version 1.6.1, build 14.

## 1.6.0

- New player design "Bio Pro" (`PlayerCinemaControls`) for ExoPlayer, libmpv on Android and libmpv on iOS, replacing the standard layout (the legacy layout and the foldable layout are unchanged): back button, local clock and only the available actions at the top (Watch Party, switch engine, external player, video settings, submit intro, stream info, lock); an Up next card with thumbnail, title and time left for aired next episodes; at the bottom a SEASON · EPISODE line, large title, episode title, release year and quality badges; labelled buttons for Subtitles (language, highlighted when on), Audio (language), Sources (provider), Episodes (season), Chapters, Speed and Picture (Fit/Fill); and a play row with seek, chapter timeline, duration or time left, and "Ends at" (adjusted for speed).
- Subtitles follow the speech for the whole film: `AudioSubtitleSync.follow` checks the last four minutes of audio every minute and shifts the rest of the subtitle when the same drift is measured twice in a row (lyrics and voices alike). It also runs when the whole-film match finds nothing or the subtitle is already in sync. Wired into the libmpv coordinator and the ExoPlayer sidecar and libass paths; libmpv keeps the track's title and language when it replaces an earlier AutoSync copy (Android and iOS).
- Picture: every film starts in Fit; Fill now fills the screen with the right proportions by cropping (ExoPlayer `RESIZE_MODE_ZOOM`, libmpv `panscan=1`) and nothing stretches the picture; the resize button switches between Fit and Fill.
- Home without catalogs: the top bar respects the inset the update banner already uses, so it sits right under it; the wordmark (34 dp) and the refresh arrow are equally far from the screen edges.
- Wordmark sizes like Nuvio's: loading screen 51 dp, Who's watching 40 dp (48 dp on tablets), Profile footer 40 dp (48 dp).
- `docs/subtitle-autosync.md` describes the follow-up. New strings in English, Swedish and Finnish.
- Raise both platforms to version 1.6.0, build 13.

## 1.5.3

- Text styles: back to Nuvio's typography everywhere – bold page headings (the Settings sidebar title too), "Who's watching?" 30 sp bold, the profile name on the Profile tab SemiBold, the version line in Nuvio's small style, and Nuvio's card styles for "No content sources", its message and the Add Addon button.
- New Morivia wordmark images (`app_logo_wordmark*.png`, every icon colour): the Morivia logo with "morivia" on its right, drawn in the rounded, heavy lowercase style of Nuvio's wordmark and sized to match it. `scripts/generate-wordmarks.ps1` draws them.
- The wordmark replaces the separate icon and "Morivia" text on the Profile tab footer (Nuvio's size, 26/30 dp), the profile selection screen (34/42 dp) and the Home top bar without catalogs (26 dp); the loading screen uses it too.
- Raise both platforms to version 1.5.3, build 12.

## 1.5.2

- Fix: "No content sources" flashed on Home, Search and Library at start-up, after a profile switch and while addons were pulled from Nuvio, because the addon list is briefly empty then. The empty view now appears only after there has been no catalog source for 1.5 seconds; until the first source state is known the screens stay blank.
- Fix: Add Profile offered a PIN lock to guests, where Nuvio cannot store it, and silently ignored a failed PIN. The PIN card on Add Profile now appears only when signed in to a Nuvio account, and a failed PIN shows a message (the profile is still created).
- The update-check error no longer asks to sign in to the private MoriviaMobile repository; it says GitHub could not be reached (English, Swedish, Finnish).
- Raise both platforms to version 1.5.2, build 11.

## 1.5.1

- In-app updates: the updater reads releases from the new public repository `OckePocke9/MoriviaUpdates` instead of the private MoriviaMobile repository, which GitHub hides from the app. The update banner now appears at the top of the app when a new version is out and downloads and installs the APK, as in Nuvio.
- `scripts/publish-update-mirror.ps1` copies a published release (APK, checksums and notes only) to MoriviaUpdates; `scripts/publish-release.ps1` runs it after every release. 1.5.0 was copied there too.
- README: install links point to MoriviaUpdates; release steps in README and AGENTS.md describe the copy.
- Raise both platforms to version 1.5.1, build 10.

## 1.5.0

- New `Original` theme from the Morivia logo colours (violet #9A15F9, #6B4BF5, blue #3182F6, cyan #01E8F6). It is the default for new installs and profiles without a saved theme, comes first among the standard themes and uses the default launcher icon. The theme name is synced; Nuvio's own apps fall back to their default theme for it.
- Android launcher icon: solid black background instead of the leftover green Android Studio grid for every icon; the default icon shows the original logo and alternative app icons keep their own colours.
- AMOLED black now makes cards and surfaces pure black and tints the screen background with the selected theme.
- Profile selection: "Morivia" as text (24 sp, normal weight) instead of the wordmark image; "Who's watching?" 38 sp, normal weight; the standard background follows the theme instead of the profile colour (supporter background images are unchanged).
- Edit Profile and Add Profile: always use the theme background with black cards, theme-coloured switches and buttons; Add Profile can set and remove a PIN lock (set on the new profile right after it is created; needs a Nuvio account as before); the chosen avatar and its name are shown under "Choose an avatar"; every avatar shows its name.
- Edit Profile and Add Profile: "Use initials" with a fallback colour picker, as on nuvio.tv; the profile then shows its first letter on that colour.
- Text: page headings 38 sp and not bold; the profile name on the Profile tab 24 sp and not bold; the Morivia name 24 sp and not bold; the version line 14 sp.
- Without a catalog source (an enabled addon with catalogs, such as Cinemeta, or a media server library), Home shows a top bar with the Morivia icon and name and a refresh button, and a "No content sources" view with an Add Addon button in the theme gradient that opens Addons settings; Continue Watching is hidden and Search and Library show only that view. Home, Search and Library with catalogs are unchanged.
- New strings in English, Swedish and Finnish for the Original theme, initials, fallback colour, Add Addon and Refresh.
- Tests: AMOLED and Original theme tests; theme, launcher icon and search tests updated.
- Raise both platforms to version 1.5.0, build 9.

## 1.4.1

- Show the Morivia icon, name and version only on the Profile tab, under the Settings card; remove the same footer from the Settings and About pages.

## 1.4.0

- Add a dedicated Profile tab with the active profile, existing Switch Profile and Account destinations, and a separate Settings card.
- Remove account/profile shortcuts from Settings and its search; keep Watch Party in a standard settings card.
- Replace the experimental Watch Party join warning with the host instruction and remove its duplicate.
- Show a larger Morivia name, centred theme icon and live version in the footer; remove the made-with line from this UI while retaining copyright and licence notices.
- Add Swedish and Finnish app-language options with 452 translated strings each and the existing English fallback for remaining strings (31 language choices plus device language).
- Centre the existing ribbon artwork and generate matching common, Android and iOS icon palettes for every preset theme; custom themes use the nearest compiled palette.
- Synchronise preset theme selection and the launcher icon while preserving all existing Android launcher component names.
- Wrap collection names and folder labels in management, editing and home collection rows.
- Open MoriviaMobile’s latest stable release from the private-repository updater fallback; retain normal APK download, permission and install handling.
- Raise both platforms to version 1.4.0, build 7, retaining their application IDs and signing identity.


## 1.3.0

- Subtitle speech sync: when a video has no embedded subtitle timing, AutoSync measures the speech in the playing audio and lines the selected addon subtitle up with it, correcting a constant delay and frame-rate drift (23.976, 24 and 25 fps). Works in ExoPlayer (pass-through voice-band audio processor) and libmpv on Android and iOS (FFmpeg `astats`/`ametadata` filter added after the file loads; libmpv keeps the old audio chain if it cannot start). Results are applied only when the match stands clearly above every other shift, after two minutes of played audio and again as more audio arrives. See docs/subtitle-autosync.md.
- The iOS player bridge can add and remove an audio filter (`addAudioFilter`, `removeAudioFilter`).
- Every movie and episode starts in the Fill resize mode (full screen without zoom); the resize button still cycles Fit, Fill and Zoom during playback.
- docs/subtitle-autosync.md: AutoSync is on by default and covers libmpv on Android and iOS.
- iOS: the foldable iPhone hinge and vertical bar code needs the iOS 27.1 SDK; `scripts/build-ios-ipa.sh` builds it only when Xcode has that SDK (condition `MORIVIA_IOS_27_1_SDK`), so the app also builds with Xcode 26 on GitHub's Intel Macs.
- Release workflow: the iOS job gives Gradle and Kotlin/Native 9 GB, because linking the release framework ran out of the 4 GB that Android builds use. 1.1.0 and 1.2.0 were never published because their iOS builds did not finish (no free Apple silicon runner, then this memory limit); 1.3.0 is the first release with both apps and contains all of their changes.

## 1.2.0

Brings in all 47 changes from Nuvio 1.1.0-beta.1 while keeping Morivia's branding and fixes:

- Library: remember the selected list and type; filter by genre, year and watched status.
- Addons: support Stremio player and library tracking addons, with a consent prompt; events are queued per addon and not sent for series watched reconciliation.
- Subtitles: outline width setting; the styled overlay keeps subtitle colours, positions, formatting and top placement.
- Player: fall back from the next episode's automatically selected source when it fails; on Android, bottom controls ignore the gesture navigation inset.
- TorBox: match alternative-hash cache answers.
- iOS: keep downloads out of iCloud and computer backups.
- Languages: add Simplified Chinese and Ukrainian (Android and iOS); complete Brazilian Portuguese; update Hebrew, Portuguese, French and Urdu; fix mixed right-to-left and left-to-right text in Search and Watch Party.
- New and updated translations use the Morivia name, except where they refer to Nuvio's own services.
- Nuvio's AltStore source (`store.json`) is not included: AltStore needs public downloads, and this repository is private.

## 1.1.0

- New: Morivia for iPhone and iPad. Every release now contains an unsigned iOS app (`Morivia-iOS-X.Y.Z.ipa`, iOS/iPadOS 16.1 or newer) next to the Android app for phones and tablets.
- The iOS app uses the Morivia name, icon and bundle ID `com.morivia.app`, with the same features as the full Android app.
- Android and iOS share one version: `scripts/bump-version.py` updates both.
- Releases are now named "Morivia X.Y.Z" and are built by one workflow that tests Android, builds both apps and publishes them together.
- Rewritten README with download and install steps for Android and iOS.
- Removed leftover workflows from the original project that closed new issues and pull requests with Nuvio messages, and one-time setup workflows.
- Issue template links now point to Morivia's releases and README instead of Nuvio's.
- `.gitignore` no longer hides AGENTS.md, the scripts folder or the docs folder.
- The debug log script uses Morivia's app ID by default.

## 1.0.2

- Create a persistent local default profile for first-time guests instead of leaving them at an empty profile picker.
- Keep MPV playback view updates on the main thread after native snapshot reads.
- Register and remove foreground lifecycle observers on the main thread, including cancellation during navigation.
- Restore HTTPS certificate and hostname validation for media requests and prevent Authorization headers from being reattached after redirects to another origin.
- Use MoriviaMobile for update checks and offer the GitHub releases page when private releases cannot be accessed in the app.
- Never offer an APK built only for an incompatible CPU architecture.
- Run Android host tests and lint before publishing releases and on pull requests to main.
- Use Java 21 in CI so Android 16/API 36 Robolectric tests can run.
- Update the legacy progress migration test to use a real content duration rather than a filtered one-second placeholder.
- Add Android instrumentation coverage for guest startup and production FileProvider subtitle sharing; run the POSIX-only FileProvider host test on Linux CI.

## 1.0.1

- Restore Nuvio Cloud configuration for email/password and Sign in with link.
- Use the official public publishable client key when no backend override is configured.
- Reject incomplete backend overrides during the build instead of producing a broken login.

## 1.0.0

- First Morivia Android release.
- Morivia app name, launcher icon, transparent in-app logo and repository branding.
- Original Nuvio account sign-in identity retained.
- Android-only source tree and build configuration; Apple and desktop files removed.
- Independent Android versioning and a consistent development signing identity.
- Semantic version policy for every future app update.
