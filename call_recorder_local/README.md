# Local Call Recorder (Flutter + Kotlin)

Prototype Android project for a **local-only** call recorder workflow.

## What it does

- Flutter UI for first-time setup and enable/disable.
- Native Kotlin foreground service for microphone recording.
- Watches screen state: screen ON arms the service; screen OFF stops it.
- Watches phone call state and attempts to start/stop recording automatically.
- Saves M4A/AAC files locally under the app's external Music directory in `CallRecorder/`.
- No backend, account, cloud upload, or network transfer is included.
- GitHub Actions workflow builds a release APK.

## Important Android limitation

Android does not guarantee that third-party applications can capture both sides of a normal cellular call. `VOICE_COMMUNICATION` audio routing is device/ROM dependent, and many modern devices restrict call audio capture. This project does **not** use Accessibility Service to bypass those restrictions.

The project is therefore a working architecture/prototype for the local recorder lifecycle, but the actual two-way call-audio result must be tested on the target phone.

## First setup

1. Install the APK.
2. Open the app once.
3. Grant microphone, phone-state and notification permissions.
4. Tap **Setup sekali**.
5. On OEM devices, also exempt the app from battery optimization / enable auto-start if the device requires it.
6. Close the app.

Android may still require user-visible foreground-service behavior. Do not disable or hide required Android privacy indicators.

## Build with GitHub

Push this repository to GitHub. The included workflow `.github/workflows/build-apk.yml` installs Flutter/Gradle on the runner and builds the release APK on pushes to `main` and on manual workflow runs.

The artifact is named `call-recorder-release-apk`.

## Local build

Requires Flutter stable and Android SDK.

```bash
flutter pub get
flutter build apk --release
```

Output:

```text
build/app/outputs/flutter-apk/app-release.apk
```

## Next improvements

- Better device/OEM compatibility handling.
- In-app recording list and playback.
- Storage cleanup controls.
- Call direction metadata.
- Runtime diagnostics page showing which audio source is actually available.
- Optional app-private encryption for saved recordings.
Updated on 2026-09-27.
