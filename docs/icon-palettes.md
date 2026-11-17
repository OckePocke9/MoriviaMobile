# Morivia icon palettes

`scripts/generate-theme-icons.cjs` derives every icon from the immutable existing ribbon at `assets/branding/morivia-icon-source.png`. It centres the visible artwork, keeps a black launcher background and reads preset gradients directly from `ThemeColors.kt`. Transparent symbols are used in the footer. Existing Android components are retained so an update keeps its launcher entry.

The script uses Node.js and sharp. Install sharp in a temporary build-tool directory and run with NODE_PATH pointing at its node_modules. Generated Android, Compose and iOS assets are committed; normal app builds need no Node installation.

Android and iOS launchers require precompiled icons. All preset theme gradients have exact matching assets. For arbitrary custom colours, `iconForTheme` selects the closest compiled accent; it does not claim that arbitrary launcher colours can be generated after installation.

The Android updater retains the same package ID and development signing keystore. It uses the normal APK installer and unknown-sources permission flow. GitHub does not permit anonymous downloads from a private repository: the fallback opens `/releases/latest` for an authorised GitHub session. No GitHub token is bundled in the app.
