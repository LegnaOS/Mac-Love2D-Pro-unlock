#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
root="$PWD"
out="$PWD/artifacts/lovelive2d-mac-build"
app="$PWD/output/LoveLive2D-Mac/LoveLive2D Mac.app"
mkdir -p "$out/classes" "$out/rlm" "$out/rlm1603" "$app/Contents/MacOS" "$app/Contents/Resources"
JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}
"$JAVA_HOME/bin/javac" --release 11 -d "$out/classes" "$root/src/MacTool.java"
"$JAVA_HOME/bin/jar" --create --file "$app/Contents/Resources/MacTool.jar" -C "$out/classes" .
"$JAVA_HOME/bin/javac" --release 11 -d "$out/rlm" "$root"/upstream/rlm1501/*.java
"$JAVA_HOME/bin/jar" --create --file "$app/Contents/Resources/rlm1501.jar" -C "$out/rlm" .
"$JAVA_HOME/bin/javac" --release 11 -cp "$out/rlm" -d "$out/rlm1603" "$root/compat/rlm1603/RlmLicense.java"
"$JAVA_HOME/bin/jar" --create --file "$app/Contents/Resources/rlm1603-payload.jar" -C "$out/rlm1603" .
for arch in arm64 x86_64;do
 xcrun swiftc -swift-version 5 -O -target "$arch-apple-macosx12.0" -framework Cocoa "$root/src/App.swift" -o "$out/launcher-$arch"
done
lipo -create "$out/launcher-arm64" "$out/launcher-x86_64" -output "$app/Contents/MacOS/LoveLive2D"
cp "$root/LICENSE" "$root/UPSTREAM.md" "$root/README.md" "$app/Contents/Resources/"
cat > "$app/Contents/Info.plist" <<'PLIST'
<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd"><plist version="1.0"><dict>
<key>CFBundleExecutable</key><string>LoveLive2D</string>
<key>CFBundleIdentifier</key><string>dev.legna.lovelive2d.macport</string>
<key>CFBundleName</key><string>LoveLive2D Mac</string>
<key>CFBundlePackageType</key><string>APPL</string>
<key>CFBundleShortVersionString</key><string>0.2.0</string>
<key>CFBundleVersion</key><string>2</string>
<key>LSMinimumSystemVersion</key><string>12.0</string>
<key>NSHighResolutionCapable</key><true/>
</dict></plist>
PLIST
codesign --force --sign - "$app"
echo "$app"
