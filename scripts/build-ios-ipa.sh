#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "$0")/.." && pwd -P)"
version_file="${repository_root}/gradle.properties"
version="$(sed -nE 's/^morivia\.versionName=([0-9]+\.[0-9]+)[[:space:]]*$/\1/p' "${version_file}")"
build_number="$(sed -nE 's/^morivia\.versionCode=([0-9]+)[[:space:]]*$/\1/p' "${version_file}")"
if [[ ! "${version}" =~ ^[0-9]+\.[0-9]+$ || ! "${build_number}" =~ ^[1-9][0-9]*$ ]]; then
    echo "Invalid Morivia version in ${version_file}." >&2
    exit 1
fi
if [[ -n "${1:-}" && "${1}" != "${version}" ]]; then
    echo "Requested version ${1} does not match ${version}." >&2
    exit 1
fi
configuration="${IOS_CONFIGURATION:-Release}"
case "${configuration}" in
    Debug)
        configuration_slug="debug"
        ;;
    Release)
        configuration_slug="release"
        ;;
    *)
        echo "Unsupported iOS configuration: ${configuration}" >&2
        exit 1
        ;;
esac
derived_data="${IOS_DERIVED_DATA_PATH:-${repository_root}/build/ios-derived-full-${configuration_slug}}"
output_directory="${IOS_IPA_OUTPUT_DIR:-${repository_root}/build/ios-ipa}"
clang_module_cache="${CLANG_MODULE_CACHE_PATH:-${derived_data}/ModuleCache.noindex}"
swiftpm_module_cache="${SWIFTPM_MODULECACHE_OVERRIDE:-${derived_data}/SwiftPMModuleCache.noindex}"

cd "${repository_root}"
build_environment=(
    env
    MORIVIA_IOS_DISTRIBUTION=full
    CLANG_MODULE_CACHE_PATH="${clang_module_cache}"
    SWIFTPM_MODULECACHE_OVERRIDE="${swiftpm_module_cache}"
)
if [[ -n "${MORIVIA_GRADLE_JVMARGS:-}" ]]; then
    build_environment+=("ORG_GRADLE_PROJECT_org.gradle.jvmargs=${MORIVIA_GRADLE_JVMARGS}")
fi
if [[ -n "${MORIVIA_KOTLIN_NATIVE_JVMARGS:-}" ]]; then
    build_environment+=("ORG_GRADLE_PROJECT_kotlin.native.jvmArgs=${MORIVIA_KOTLIN_NATIVE_JVMARGS}")
fi
# Code for the iOS 27.1 SDK (foldable iPhone hinge, vertical bar) is built only when Xcode has it.
sdk_version="$(xcrun --sdk iphoneos --show-sdk-version)"
swift_conditions='$(inherited)'
if [[ "$(printf '%s\n' 27.1 "${sdk_version}" | sort -V | head -n 1)" == "27.1" ]]; then
    swift_conditions+=" MORIVIA_IOS_27_1_SDK"
fi
echo "iOS SDK ${sdk_version}; Swift conditions: ${swift_conditions}"

"${build_environment[@]}" \
    xcodebuild \
    -project iosApp/iosApp.xcodeproj \
    -scheme iosApp \
    -configuration "${configuration}" \
    -sdk iphoneos \
    -destination 'generic/platform=iOS' \
    -derivedDataPath "${derived_data}" \
    CODE_SIGNING_ALLOWED=NO \
    CODE_SIGNING_REQUIRED=NO \
    CODE_SIGN_IDENTITY= \
    MARKETING_VERSION="${version}" \
    CURRENT_PROJECT_VERSION="${build_number}" \
    SWIFT_ACTIVE_COMPILATION_CONDITIONS="${swift_conditions}" \
    build

app_path="${derived_data}/Build/Products/${configuration}-iphoneos/Morivia.app"
if [[ ! -d "${app_path}" ]]; then
    echo "iOS build did not produce ${app_path}." >&2
    exit 1
fi

built_version="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleShortVersionString' "${app_path}/Info.plist")"
if [[ "${built_version}" != "${version}" ]]; then
    echo "Built iOS version ${built_version} does not match ${version}." >&2
    exit 1
fi

built_number="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleVersion' "${app_path}/Info.plist")"
if [[ "${built_number}" != "${build_number}" ]]; then
    echo "Built iOS build number ${built_number} does not match ${build_number}." >&2
    exit 1
fi

device_families="$(/usr/libexec/PlistBuddy -c 'Print :UIDeviceFamily' "${app_path}/Info.plist")"
if ! grep -Eq '^[[:space:]]*1[[:space:]]*$' <<<"${device_families}" ||
   ! grep -Eq '^[[:space:]]*2[[:space:]]*$' <<<"${device_families}"; then
    echo "Built iOS app must support both iPhone and iPad." >&2
    exit 1
fi

executable="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleExecutable' "${app_path}/Info.plist")"
architectures="$(xcrun lipo -archs "${app_path}/${executable}")"
if [[ " ${architectures} " != *" arm64 "* ]]; then
    echo "Built iOS application does not contain arm64." >&2
    exit 1
fi
if ! launch_screen_plist="$(plutil -extract UILaunchScreen xml1 -o - "${app_path}/Info.plist" 2>/dev/null)"; then
    echo "Built iOS application does not contain UILaunchScreen." >&2
    exit 1
fi
if [[ "${launch_screen_plist}" == *"<key>UILaunchScreen</key>"* ]]; then
    echo "Built iOS application contains a nested UILaunchScreen." >&2
    exit 1
fi
if [[ -d "${app_path}/_CodeSignature" || -f "${app_path}/embedded.mobileprovision" ]]; then
    echo "Built iOS application is unexpectedly signed." >&2
    exit 1
fi

widget_path="${app_path}/PlugIns/DownloadsWidgetExtension.appex"
if [[ ! -d "${widget_path}" ]]; then
    echo "Built iOS application does not contain the downloads widget." >&2
    exit 1
fi
widget_executable="$(/usr/libexec/PlistBuddy -c 'Print :CFBundleExecutable' "${widget_path}/Info.plist")"
widget_architectures="$(xcrun lipo -archs "${widget_path}/${widget_executable}")"
if [[ " ${widget_architectures} " != *" arm64 "* ]]; then
    echo "Built downloads widget does not contain arm64." >&2
    exit 1
fi

mkdir -p "${output_directory}"
output_directory="$(cd "${output_directory}" && pwd -P)"
package_root="$(mktemp -d "${TMPDIR:-/tmp}/morivia-ios-ipa.XXXXXX")"
trap 'rm -rf "${package_root}"' EXIT
mkdir -p "${package_root}/Payload"
ditto "${app_path}" "${package_root}/Payload/Morivia.app"

ipa_path="${output_directory}/Morivia-iOS-${version}-${configuration_slug}.ipa"
temporary_ipa="${package_root}/Morivia-iOS-${version}-${configuration_slug}.ipa"
(
    cd "${package_root}"
    /usr/bin/zip -qry "${temporary_ipa}" Payload
)
unzip -tq "${temporary_ipa}"
mv "${temporary_ipa}" "${ipa_path}"

echo "Created ${ipa_path}"
