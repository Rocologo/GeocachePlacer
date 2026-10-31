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
- Aktuelt: versionCode 131, versionName "3.0.1"

## Byggesystem
- Udviklingsmaskine: omarchy-cosmos (Arch Linux), projektet i `~/Projects/GeocachePlacer`
- Java: OpenJDK 21
- Gradle wrapper: `./gradlew` (Gradle 8.11.1); `gradlew.bat` beholdes til Windows
- Android SDK: `~/Android/Sdk` (sat via `sdk.dir` i `local.properties`)
- Build: `./gradlew :app:assembleDebug` / `./gradlew :app:assembleRelease`
- ADB: `~/Android/Sdk/platform-tools/adb`
- Release-keystore forventes i `~/.android/geocacheplacer-upload.jks` (ikke flyttet fra Windows endnu)

## Test
- Emulator: AVD "GeocacheTest" (Pixel 6, Android 35)
- Fysisk enhed: Samsung Galaxy S23 Ultra, forbindes via WiFi ADB
  - IP varierer (typisk 192.168.2.x), port vises under Indstillinger → Udviklerindstillinger → Trådløs fejlretning

## Main developer og forholdet til de andre projekter

Denne session er **main developer** for GeocachePlacer — den eneste der skriver kode i dette repo.

omarchy-cosmos rummer kun Christians private projekter, hver med sin egen main developer:
`admin-ai` (Private Admin AI — administrator), `GeocachePlacer`, `RocTrading`, `minecraft-plugins`.

- Skriv ikke kode i andre projekter, og lad ikke andre skrive kode her. Hører en opgave til et andet projekt, så sig det til Christian, eller skriv til det projekts session.
- Sessioner må gerne tale sammen (ListAgents / SendMessage), når det giver mening — fx for at dele viden eller koordinere.
- Admin AI (sessionen i ~/Projects/admin-ai) er administrator for maskinen og må overdrage opgaver og give kontekst, men skriver heller ikke kode her.

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
