# Morivia Android 1.0.2 verification

Date: 2026-11-16. Application ID: `com.morivia.app`, version code: `3`.

## Verified locally

- Full release and debug APK builds passed with the full Android distribution.
- Release APK package name, version, ARM64 architecture and signature were checked.
- Android host tests: 2,108 total, 2,107 passed, no failures, one Windows-only skip.
- Release lint: zero errors, 13 warnings. The previous trust-all TLS warnings were removed.
- Regression tests cover first-guest profile creation and persistence, preservation of existing guest profiles, update repository access, compatible APK selection, certificate rejection and Authorization handling across redirects.
- Nuvio Cloud health and authentication settings returned HTTP 200 using the app's public client configuration; email sign-in is enabled. No personal account was used.

The skipped host test exercises Android FileProvider, whose canonical path comparisons require POSIX paths. It remains enabled on Linux CI. Its real Android URI and read behavior was also verified by `LocalSubtitleSharingTest`.

## Android runtime checks

On a separate Android API 37 emulator installation:

- `StartupSmokeTest`: passed. A fresh install continued without an account, created and selected its local profile, and opened Home. The app installed the public Cinemeta addon, searched for Matrix, and retrieved The Matrix metadata through its own repositories. The test removed the addon afterward.
- `LocalSubtitleSharingTest`: passed. The production FileProvider returned a readable content URI and clearing the subtitle cache preserved the original file.
- `SubtitlePlaybackTest`: both ExoPlayer and MPV caption styling/clearing tests passed.
- `MpvSurfaceTest`: passed. Video returned after seeking and recreating the MPV surface.

Startup and playback tests ran in separate instrumentation processes to isolate their application-wide state. Playback checks passed with native x86_64 libraries and the emulator's host NVIDIA GPU renderer.

## Limits and remaining device checks

- The emulator's SwiftShader software renderer intermittently aborted in MPV/libplacebo with an OpenGL buffer-size assertion. The same playback tests passed with the host GPU. This does not establish compatibility with every phone GPU.
- No physical phone was connected. Verify the ARM64 release on the intended phone, including picture, sound, subtitles, background/resume behavior and installation over the previous signed version.
- Personal Nuvio sign-in, cross-device cloud sync, watch parties and the user's own streaming/debrid sources were not exercised. TMDB, Trakt, SIMKL and other optional integrations need their own configuration and account-level checks.
- MoriviaMobile is private. Anonymous update API requests cannot read its releases; the app offers the GitHub releases page for authenticated manual downloads instead of claiming the installed version is current.

The release workflow now runs Android host tests and release lint before publishing an APK. Pull requests to main run host tests and debug lint as well.

CI uses Java 21: Robolectric's Android 16/API 36 tests require it ([compatibility table](https://robolectric.org/compatibility_table/)). The first CI attempt used Java 17 and could not initialize the API 36 download lifecycle test class; publishing was blocked until the runtime configuration was corrected.
