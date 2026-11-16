# Changelog

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
