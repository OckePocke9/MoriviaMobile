# Changelog

## Local test build 40 (1.6, unpublished)

- Brighten the 31 preset gradients and matching Morivia icons while retaining dark surfaces and saved colour identifiers.

## Local test build 39 (1.6, unpublished)

- Show the profile’s first letter as it is typed. Initial previews stay empty before a letter is entered.

## Local test build 38 (1.6, unpublished)

- Use the same 31 alphabetized presets across Theme, App Icon and Initials, with a distinct preview for each picker.

## Local test build 37 (1.6, unpublished)

- Show the profile’s first initial on the colour previews. Center Custom Theme and alphabetize all three pickers in both views.

## Local test build 36 (1.6, unpublished)

- Add 31 coordinated theme palettes and Android launcher icons, also available for profile initials. Copper now has a matching theme.
- Use icon previews and alphabetical expanded lists with a return to the expansion arrow. Add reset and custom controls across Theme, App Icon and Initials.
- Add Match theme to app icon, with independent manual selection. Remove Current theme from Appearance.

## Local test build 35 (1.6, unpublished)

- Add Back in sync after a personal seek, using a fresh playback position from the host.
- Offer to continue watching with the group when Morivia restores a saved guest room.
- Show the standard playback loading screen while joining and buffering.

## Local test build 34 (1.6, unpublished)

- Keep the guest’s transition black while joining and loading a matching stream, align playback before displaying it, and coordinate waiting-room starts.
- End for Everyone closes the player on connected devices. Leave keeps the remaining viewers in the movie or episode.
- Cap rooms at six viewers in the app, including the host.

## Local test build 33 (1.6, unpublished)

- Restore an available guest room when the app reopens, scoped to the signed-in account and server. Leaving manually forgets the room.
- Retry after temporary connection errors and let playback continue independently if the room ends.
- Open a shared or matching stream after joining, use the host’s position, audio and subtitles as starting choices, and keep seeks local to each player.

## Local test build 32 (1.6, unpublished)

- Accept 0 and 1 in Watch Party room codes, including pasted codes and invite links.

## Local test builds 29–31 (1.6, unpublished)

- AMOLED black: every card is pure black, Home, Search and Library get a pure black background, and Profile and Settings keep the theme-tinted AMOLED background. Nuvio sign-in and the profile chooser are unchanged.
- Appearance: the app icon picker is built like the theme picker, three per row on a phone (four on a tablet), the same chips, names and order as the themes, nine first and the rest behind the arrow.
- All 31 app languages are completed for the app itself: Swedish and Finnish from about 18 % to complete, and the remaining texts added in every other language. Addon and integration screens (TMDB, MDBList, debrid, Trakt, Simkl, tracking), the locked Nuvio sign-in and the profile chooser keep their texts; content from addons is not translated.

## Local test build 28 (1.6, unpublished)

- Initials colours are now exactly the Appearance app icons with their theme names, gradients and text colours (Copper included), shown as the same chips as the theme picker, nine first and the rest behind the arrow.
- Initials look the same everywhere: profile chooser, profile editing, the in-app profile switcher and Party avatars.
- Editing a profile no longer turns off "uses primary plugins" on Nuvio, a failed save keeps you on the screen with a message instead of looking saved, and a new profile only gets its PIN when it was really created.
- Profile sync also clears cached PINs changed or removed on another device.

## Local test build 27 (1.6, unpublished)

- Keep the PIN dialog open between entry and confirmation, and match Add Profile colours and typography to Manage Profiles and Done.

## Local test build 26 (1.6, unpublished)

- Restore the Add Profile circle, open editing only from the themed pencil, reuse Appearance gradients for initials, group avatar choices on theme-aware cards and animate the PIN prompt opening.

## Local test build 25 (1.6, unpublished)

- Match the profile chooser references with tinted, outlined initials circles, Nuvio typography, white pencil buttons and outlined Manage Profiles / Done controls. Add Profile is a text button without a circle, as requested.
- Retain Morivia's name, logo, background and existing profile selection, PIN and management behavior.
- Lock Nuvio sign-in to the build 24 baseline; keep version name 1.6 and increase only build number to 25.

## Local test build 24 (1.6, unpublished)

- Keep Nuvio sign-in independent of Morivia's app theme and AMOLED settings, retaining the fixed Nuvio wordmark and existing layout.
- Increase only the Android build number from 23 to 24; keep version name 1.6 for local phone testing.

## 1.6

Home
- One hero at the top, left-aligned like the earlier Morivia app: the Home pill below the clock, logo or title, a genre or Featured badge, "Movie · Action · 2022", the description, a View Details button in the theme colour, an information button, a next arrow and the dots. No ratings in the hero. The dots are as far from Continue Watching as from View Details.
- Home & Layout: Play Trailers in Hero (the title's trailer plays in the hero; without one the artwork shows) and Auto-Rotate Hero (on: next title every 8 seconds, 45 while a trailer plays; off: stays until you swipe), saved per profile.
- New rows Movies Based on Your Genres and Series Based on Your Genres: titles from the loaded catalogs that share genres with the movie, or the series, watched last; the two rows are independent, each has its own switch, shows up to 30 titles and Show all opens the whole list.
- Fixed order: Continue Watching, the two genre rows, a main Featured row (five large landscape cards, movies and series taking turns, no Show all), Popular – Movies, Popular – Series, Featured – Movies and Featured – Series (5 titles each), then every other catalog in Home rows order. Rows show up to 30 titles (was 18); posters are about 2.5 across the screen.
- Every row has the earlier app's "Show all" button; posters can show a "New" banner for titles released in the last 30 days (Poster banners).
- Home & Layout also brings Trailer sound, Featured section, Landscape posters, Poster labels, Hide unreleased content, Continue Watching sort, Episode thumbnails, Next up from furthest episode, Show unaired next up, Hide spoilers, Upcoming episode countdown, Home rows, Collections, Collection GIFs and Auto-refresh Home in one page.

Search and Library
- Search: the Search pill, a "Movies and Shows" field with a search icon, a Trending row (the popular titles of Home's catalogs) and Browse genre tiles that show their genre in the Discover grid. A Search settings page switches Trending, Browse and recent searches.
- Library (saved titles): the Library pill, chips for Recently Added, Movies, TV Shows and Continue Watching, the section name with "N movies · M shows", a Sort pill (Added, Name, Recently Watched) and a grid of three posters per row with titles and watched checks. A Library settings page sets the start section, sort and watched marks.

Profiles
- Profile picker in Nuvio's layout and heights, without rings; Manage Profiles opens and closes editing and Add Profile; long press or Manage opens Edit Profile only after the PIN of a locked profile; the account status line is gone.
- Add and Edit Profile: Confirm PIN after a new PIN; guests cannot set a PIN; cards follow AMOLED Black; a divider between Use initials and the pictures; initials colours are Morivia's theme colours with Original as the default (dark letters on light colours); the header hides content scrolling under it (also in Settings).
- The picker shows the theme and app icon of the profile used last; signed out, the app uses Original. New guests and accounts start with only Add Profile.

App
- Changing the app icon no longer closes the app: the new icon is switched when the app goes to the background.
- The in-app updater reads this repository's releases and understands two-part versions such as 1.6.
- Version numbers go up by 0.1. Releases 1.0.0–1.7.1 were renamed v0.1–v1.5.
- 1.6 is signed with Morivia's private release key, so it must be installed once after removing the old Morivia; later versions update over it.
- Morivia is Android only.
- Raise the version to 1.6, build 23.

- The Original launcher icon, adaptive foreground, app icon, in-app symbol, iOS app icon and the README logo are redrawn from the earlier Morivia app's launcher artwork (`ic_launcher_symbol.png`, the same artwork as the logo on the profile picker), placed as there: the symbol in a 22 % inset on black.
- Every theme's icons are recoloured again from the new artwork with `scripts/recolor-theme-icons.ps1`.
- Raise both platforms to version 1.5, build 17.

## 1.4

- Profile picker, Add Profile, Edit Profile, the launch overlay and every Settings page draw the earlier app's background in the selected theme's colours (`profileGlowColors`: the theme accent and the last gradient colour at 32 % over black; Original keeps #2F0451 / #004051). Settings use it through `MoriviaThemedBackground` with a transparent screen background, so cards keep their colour.
- The logo next to "Morivia" on the profile picker follows the theme (`morivia_brand_symbol_<colour>.png`, `AppIconOption.brandSymbolResource`); custom colours use the closest preset.
- Profile picker: content centred vertically; the same ring on every profile circle, the active profile's ring in the theme gradient; initials as a white letter on the profile colour; a see-through back button with a thin light edge; a padlock badge on profiles with a PIN; the Add Profile tile whenever there is room (not only in Manage Profiles); long press opens Edit Profile; a short grow animation on tap; an account line at the bottom ("Signed in with Nuvio · profiles sync" or "Guest mode · profiles stay on this device").
- Theme app icons and logos keep the original logo's details (ribbons, shading, play button) and only change colour: new `scripts/recolor-theme-icons.ps1` recolours the original artwork for the Android launcher and adaptive foreground, the iOS app icons, the in-app symbols and the profile logo. `scripts/generate-wordmarks.ps1` now draws wordmarks for all 14 colours, and each theme uses its own wordmark.
- Appearance gains the earlier app's settings: "Current theme", nine themes with an arrow for the rest, Reset to Original, a Custom theme button, Match app icon to theme (on by default, kept on the device; with it off, themes and app icons are chosen separately) and a theme preview. English, Swedish and Finnish texts.
- AMOLED Black is on by default, as in the earlier app.
- Raise both platforms to version 1.4, build 16.

## 1.3

- Profile picker (Who's watching), Add Profile and Edit Profile use the background of the earlier Morivia app (1.4.130) with its Original theme: a violet glow (#920CFC at 32 %) at the top left and a cyan glow (#00C8FC at 32 %) at the top right on black (`Modifier.earlierMoriviaBackground()`). The launch overlay uses it too; supporter background images are unchanged. Edit and Add Profile keep their black cards.
- Profile picker: the earlier app's logo (`morivia_brand_symbol`, 34 x 28 dp) with "Morivia" at 25 sp and "Who's watching?" at 30 sp, both weight 800; names at 17 sp and the Manage Profiles button at 15.5 sp, weight 600.
- The earlier app's font Plus Jakarta Sans (variable, SIL Open Font License; licence in `androidMain/assets/licenses/plus-jakarta-sans-OFL.txt`) for those texts.
- The earlier app's colours: near-black initials circles tinted 14 % with the profile colour and a 55 % ring, a near-black add tile with a dark grey ring, and grey Manage Profiles / dark Done buttons. Sizes and spacing are unchanged.
- Raise both platforms to version 1.3, build 15.

## 1.2

- Player: back to Nuvio's original controls in every player (ExoPlayer, libmpv on Android and iOS). The "Bio Pro" design (`PlayerCinemaControls`) and its strings are removed, because it covered too much of the film.
- Kept from 1.1: whole-film speech sync for subtitles, Fit as the start mode and no stretching (Fill crops), and every other 1.1 change.
- Raise both platforms to version 1.2, build 14.

## 1.1

- New player design "Bio Pro" (`PlayerCinemaControls`) for ExoPlayer, libmpv on Android and libmpv on iOS, replacing the standard layout (the legacy layout and the foldable layout are unchanged): back button, local clock and only the available actions at the top (Watch Party, switch engine, external player, video settings, submit intro, stream info, lock); an Up next card with thumbnail, title and time left for aired next episodes; at the bottom a SEASON · EPISODE line, large title, episode title, release year and quality badges; labelled buttons for Subtitles (language, highlighted when on), Audio (language), Sources (provider), Episodes (season), Chapters, Speed and Picture (Fit/Fill); and a play row with seek, chapter timeline, duration or time left, and "Ends at" (adjusted for speed).
- Subtitles follow the speech for the whole film: `AudioSubtitleSync.follow` checks the last four minutes of audio every minute and shifts the rest of the subtitle when the same drift is measured twice in a row (lyrics and voices alike). It also runs when the whole-film match finds nothing or the subtitle is already in sync. Wired into the libmpv coordinator and the ExoPlayer sidecar and libass paths; libmpv keeps the track's title and language when it replaces an earlier AutoSync copy (Android and iOS).
- Picture: every film starts in Fit; Fill now fills the screen with the right proportions by cropping (ExoPlayer `RESIZE_MODE_ZOOM`, libmpv `panscan=1`) and nothing stretches the picture; the resize button switches between Fit and Fill.
- Home without catalogs: the top bar respects the inset the update banner already uses, so it sits right under it; the wordmark (34 dp) and the refresh arrow are equally far from the screen edges.
- Wordmark sizes like Nuvio's: loading screen 51 dp, Who's watching 40 dp (48 dp on tablets), Profile footer 40 dp (48 dp).
- `docs/subtitle-autosync.md` describes the follow-up. New strings in English, Swedish and Finnish.
- Raise both platforms to version 1.1, build 13.

## 1.0

- Text styles: back to Nuvio's typography everywhere – bold page headings (the Settings sidebar title too), "Who's watching?" 30 sp bold, the profile name on the Profile tab SemiBold, the version line in Nuvio's small style, and Nuvio's card styles for "No content sources", its message and the Add Addon button.
- New Morivia wordmark images (`app_logo_wordmark*.png`, every icon colour): the Morivia logo with "morivia" on its right, drawn in the rounded, heavy lowercase style of Nuvio's wordmark and sized to match it. `scripts/generate-wordmarks.ps1` draws them.
- The wordmark replaces the separate icon and "Morivia" text on the Profile tab footer (Nuvio's size, 26/30 dp), the profile selection screen (34/42 dp) and the Home top bar without catalogs (26 dp); the loading screen uses it too.
- Raise both platforms to version 1.0, build 12.

## 0.9

- Fix: "No content sources" flashed on Home, Search and Library at start-up, after a profile switch and while addons were pulled from Nuvio, because the addon list is briefly empty then. The empty view now appears only after there has been no catalog source for 1.5 seconds; until the first source state is known the screens stay blank.
- Fix: Add Profile offered a PIN lock to guests, where Nuvio cannot store it, and silently ignored a failed PIN. The PIN card on Add Profile now appears only when signed in to a Nuvio account, and a failed PIN shows a message (the profile is still created).
- The update-check error no longer asks to sign in to the private MoriviaMobile repository; it says GitHub could not be reached (English, Swedish, Finnish).
- Raise both platforms to version 0.9, build 11.

## 0.8

- In-app updates: the updater reads releases from the new public repository `OckePocke9/MoriviaUpdates` instead of the private MoriviaMobile repository, which GitHub hides from the app. The update banner now appears at the top of the app when a new version is out and downloads and installs the APK, as in Nuvio.
- `scripts/publish-update-mirror.ps1` copies a published release (APK, checksums and notes only) to MoriviaUpdates; `scripts/publish-release.ps1` runs it after every release. 0.7 was copied there too.
- README: install links point to MoriviaUpdates; release steps in README and AGENTS.md describe the copy.
- Raise both platforms to version 0.8, build 10.

## 0.7

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
- Raise both platforms to version 0.7, build 9.

## 0.6

- Show the Morivia icon, name and version only on the Profile tab, under the Settings card; remove the same footer from the Settings and About pages.

## 0.5

- Add a dedicated Profile tab with the active profile, existing Switch Profile and Account destinations, and a separate Settings card.
- Remove account/profile shortcuts from Settings and its search; keep Watch Party in a standard settings card.
- Replace the experimental Watch Party join warning with the host instruction and remove its duplicate.
- Show a larger Morivia name, centred theme icon and live version in the footer; remove the made-with line from this UI while retaining copyright and licence notices.
- Add Swedish and Finnish app-language options with 452 translated strings each and the existing English fallback for remaining strings (31 language choices plus device language).
- Centre the existing ribbon artwork and generate matching common, Android and iOS icon palettes for every preset theme; custom themes use the nearest compiled palette.
- Synchronise preset theme selection and the launcher icon while preserving all existing Android launcher component names.
- Wrap collection names and folder labels in management, editing and home collection rows.
- Open MoriviaMobile’s latest stable release from the private-repository updater fallback; retain normal APK download, permission and install handling.
- Raise both platforms to version 0.5, build 7, retaining their application IDs and signing identity.


## 0.4

- Subtitle speech sync: when a video has no embedded subtitle timing, AutoSync measures the speech in the playing audio and lines the selected addon subtitle up with it, correcting a constant delay and frame-rate drift (23.976, 24 and 25 fps). Works in ExoPlayer (pass-through voice-band audio processor) and libmpv on Android and iOS (FFmpeg `astats`/`ametadata` filter added after the file loads; libmpv keeps the old audio chain if it cannot start). Results are applied only when the match stands clearly above every other shift, after two minutes of played audio and again as more audio arrives. See docs/subtitle-autosync.md.
- The iOS player bridge can add and remove an audio filter (`addAudioFilter`, `removeAudioFilter`).
- Every movie and episode starts in the Fill resize mode (full screen without zoom); the resize button still cycles Fit, Fill and Zoom during playback.
- docs/subtitle-autosync.md: AutoSync is on by default and covers libmpv on Android and iOS.
- iOS: the foldable iPhone hinge and vertical bar code needs the iOS 27.1 SDK; `scripts/build-ios-ipa.sh` builds it only when Xcode has that SDK (condition `MORIVIA_IOS_27_1_SDK`), so the app also builds with Xcode 26 on GitHub's Intel Macs.
- Release workflow: the iOS job gives Gradle and Kotlin/Native 9 GB, because linking the release framework ran out of the 4 GB that Android builds use. 0.4 and 0.4 were never published because their iOS builds did not finish (no free Apple silicon runner, then this memory limit); 0.4 is the first release with both apps and contains all of their changes.

Brings in all 47 changes from Nuvio 0.4-beta.1 while keeping Morivia's branding and fixes:

- Library: remember the selected list and type; filter by genre, year and watched status.
- Addons: support Stremio player and library tracking addons, with a consent prompt; events are queued per addon and not sent for series watched reconciliation.
- Subtitles: outline width setting; the styled overlay keeps subtitle colours, positions, formatting and top placement.
- Player: fall back from the next episode's automatically selected source when it fails; on Android, bottom controls ignore the gesture navigation inset.
- TorBox: match alternative-hash cache answers.
- iOS: keep downloads out of iCloud and computer backups.
- Languages: add Simplified Chinese and Ukrainian (Android and iOS); complete Brazilian Portuguese; update Hebrew, Portuguese, French and Urdu; fix mixed right-to-left and left-to-right text in Search and Watch Party.
- New and updated translations use the Morivia name, except where they refer to Nuvio's own services.
- Nuvio's AltStore source (`store.json`) is not included: AltStore needs public downloads, and this repository is private.

- New: Morivia for iPhone and iPad. Every release now contains an unsigned iOS app (`Morivia-iOS-X.Y.Z.ipa`, iOS/iPadOS 16.1 or newer) next to the Android app for phones and tablets.
- The iOS app uses the Morivia name, icon and bundle ID `com.morivia.app`, with the same features as the full Android app.
- Android and iOS share one version: `scripts/bump-version.py` updates both.
- Releases are now named "Morivia X.Y.Z" and are built by one workflow that tests Android, builds both apps and publishes them together.
- Rewritten README with download and install steps for Android and iOS.
- Removed leftover workflows from the original project that closed new issues and pull requests with Nuvio messages, and one-time setup workflows.
- Issue template links now point to Morivia's releases and README instead of Nuvio's.
- `.gitignore` no longer hides AGENTS.md, the scripts folder or the docs folder.
- The debug log script uses Morivia's app ID by default.

## 0.3

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

## 0.2

- Restore Nuvio Cloud configuration for email/password and Sign in with link.
- Use the official public publishable client key when no backend override is configured.
- Reject incomplete backend overrides during the build instead of producing a broken login.

## 0.1

- First Morivia Android release.
- Morivia app name, launcher icon, transparent in-app logo and repository branding.
- Original Nuvio account sign-in identity retained.
- Android-only source tree and build configuration; Apple and desktop files removed.
- Independent Android versioning and a consistent development signing identity.
- Semantic version policy for every future app update.
