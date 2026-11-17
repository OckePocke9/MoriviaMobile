#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
touch local.properties
./gradlew :androidApp:installFullDebug -Pnuvio.android.distribution=full
