# Capo Finder (Android)

Pick the key your lead calls. Get the capo fret and the chord family to play, based on the chords you know.

## Run it
1. Open this folder in **Android Studio** (Ladybug or newer). Let Gradle sync; it downloads Gradle 8.9 automatically.
2. Pick a phone or emulator (Android 7.0 / API 24 or newer) and press Run.
3. Run the logic tests: right-click `app/src/test` and choose Run Tests, or use `./gradlew test`.

If Android Studio offers to update the Android Gradle Plugin or Compose versions, accepting is safe.

## How it works
`capo fret = (target root - shape root) mod 12`, with C = 0.
For the lead's key it builds the four main chords (I, IV, V, vi for major; i, iv, v, VI for minor),
checks each shape family (C, G, D, A, E for major; Am, Em, Dm for minor), keeps only families where
every chord is in "My chords", ignores anything above fret 7, and ranks by lowest capo.
If nothing fits it names the one chord that would unlock the best option.

## Structure
- `engine/CapoEngine.kt`: the capo rule, ranking and "learn this chord" hint (plain Kotlin, unit-tested)
- `data/`: ViewModel plus on-device storage (known chords, last key, recent keys, theme)
- `ui/`: Compose screens built from the approved design (pinned result card, fretboard, bottom picker, dark mode)

## Not included yet
Launcher icon, finger diagrams, entering a full chord progression, 7th chords, analytics.

## Get an installable APK
**Option A: Android Studio.** Build > Build Bundle(s) / APK(s) > Build APK(s). The file is in `app/build/outputs/apk/debug/app-debug.apk`.

**Option B: GitHub, no install needed.**
1. Create a free GitHub account and a new empty repository.
2. Upload the contents of this folder (including the hidden `.github` folder).
3. Open the **Actions** tab, then **Build APK**, then **Run workflow**.
4. After about 5 minutes, open the finished run and download **CapoFinder-debug-apk**. Unzip it to get `app-debug.apk`.

Install on the phone: copy the APK across, open it, and allow "Install unknown apps" for your file manager or browser when asked.
