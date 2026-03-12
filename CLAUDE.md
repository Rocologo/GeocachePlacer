# GeocachePlacer — Instruktioner til Claude

## Projektbeskrivelse
Android-app til GPS-positionsmåling for geocachere. Gradle-projekt i `app/`-mappen.

## Kravspecifikation
Læs altid `REQUIREMENTS.md` inden kodeændringer. Alle ændringer skal overholde kravene deri.

## Kritiske regler

### Nøgler og hemmeligheder
- Google Maps API-nøgle og AdMob produktions-IDs MÅ ALDRIG skrives ind i kode eller commites
- De ligger i `local.properties` (gitignored) og injiceres via Gradle `manifestPlaceholders` og `resValue`
- Debug-build bruger altid AdMob test-IDs

### Korttyper
- App'en understøtter BÅDE Google Maps (native MapView) og OpenStreetMap (Leaflet i WebView)
- Google Maps-typer har præfiks `gm_` i mapTypeValues
- OSM-typer: `standard`, `topo`, `osm_satellite`
- Standard korttype ved første opstart: `gm_normal`
- Begge korttyper skal altid fungere — fjern ikke den ene til fordel for den anden

### Versionering
- `versionCode` i `app/build.gradle` skal øges ved hver release-build
- Aktuelt: versionCode 130, versionName "3.0"

## Byggesystem
- Gradle wrapper: `C:\Users\clj\AppData\Local\gradle-8.11.1\bin\gradle.bat`
- Android SDK: `C:\Users\clj\AppData\Local\Android\Sdk`
- Build: `:app:assembleDebug` / `:app:assembleRelease`
- ADB: `C:\Users\clj\AppData\Local\Android\Sdk\platform-tools\adb.exe`

## Test
- Emulator: AVD "GeocacheTest" (Pixel 6, Android 35)
- Fysisk enhed: Samsung Galaxy S23 Ultra, forbindes via WiFi ADB
  - IP varierer (typisk 192.168.2.x), port vises under Indstillinger → Udviklerindstillinger → Trådløs fejlretning

## Dokumentation der skal vedligeholdes
Ved alle større ændringer — opdater disse filer:
- `REQUIREMENTS.md` — hvis krav ændres eller tilføjes
- `CHANGELOG.md` — beskriv hvad der er ændret og hvornår
- `STATUS.md` — opdater komponentstatus
- `TASKS.md` — afkryds afsluttede opgaver, tilføj nye

## Arkitektur
- `MainActivity` — hoved-Activity, GPS-måling, kort, AdMob
- `GPSTracker` — LocationManager wrapper (extends Service)
- `PrefsActivity` + `SettingsFragment` — indstillinger
- `AboutActivity` — om-skærm
- Leaflet CSS+JS bundlet i `app/src/main/assets/` (ingen CDN)
- Kortvisning: WebView (OSM) og MapView (Google Maps) — kun én vises ad gangen
