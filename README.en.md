# Mac Love2D Pro unlock

Native macOS adaptation of [LoveLive2D](https://github.com/ailidev/LoveLive2D).
[中文说明](README.md) · [Downloads](https://github.com/LegnaOS/Mac-Love2D-Pro-unlock/releases)

## Status: 0.2.0 candidate

The rlm1603 profile matches only the two exact original Cubism 5.3.04 archive hashes listed in the Chinese README. It replaces one RLM class and the corresponding hash reference, rather than replacing the whole 1603 archive with an older version.

The observed editor state was an extended trial, **not PRO activation**. An isolated Java 17 entry point opened and saved a model, generated a texture atlas and exported moc3. The copied native Cubism launcher was blocked by macOS on the test machine; Intel hardware, all editor features and long-term stability remain unverified. File patch success is separate from editor runtime success.

## Build and test

Requires macOS 12+, Xcode Command Line Tools, JDK 17 and Python 3.

```sh
bash build.sh
python3 test.py
# Optional: original local commercial files are read only; tests use temporary copies.
CUBISM_1603_RES="/path/to/original/res" python3 test.py
python3 package.py
```

Output: `output/LoveLive2D-Mac/LoveLive2D Mac.app`; release archives: `dist/`.
The launcher is universal arm64/x86_64 and ad-hoc signed, not notarized. Some bundled Cubism runtimes lack a Java executable; a system JDK is then required.

## Workflow

Save and quit Cubism first. Copy the complete installation including sibling resources, then select the copy in the Mac tool. Inspect before applying. The original archives and SHA256 transaction record are retained in `.lovelive2d-mac-backup`; existing backups are never overwritten. Restore verifies the backup and current files before writing. External modifications stop restoration. No model files or global security settings are modified by the tool.

## Provenance

GPL-3.0; see [LICENSE](LICENSE) and [UPSTREAM.md](UPSTREAM.md). This standalone repository was extracted from tool-module commit `706c674` without importing unrelated project history. No commercial editor, SDK, user model, license file or credential is distributed.
