# Changelog

## [3.0.0] — 2026-03-11 — Fuldt moderniseret

### Kort
- Leaflet.js bundlet som lokale assets (leaflet.css + leaflet.js) — ingen CDN-afhængighed
- Live-opdatering af kort via `evaluateJavascript` — ingen fuld reload under måling
- Røde cirkler for hvert målepunkt, blå pin for gennemsnitsposition
- Follow-toggle knap (●/○) — kortet følger eller låser positionen
- Google Maps tilføjet som native `MapView` (play-services-maps:18.2.0)
- Korttyper: Standard OSM / Topografi (OpenTopoMap) / Satellit (ESRI) / Google Maps
- Aktuel GPS-position vises på kort ved opstart

### GPS og koordinater
- Koordinatformat: D°M.mmm (DM) / DD.ddddd (DD) / D°M'S" (DMS) — vælges i indstillinger
- Aktuel position vises i tekstfelter ved opstart uden at køre Run
- Reset nulstiller til aktuel GPS-position, gennemsnit og afvigelse blankes

### Indstillinger
- ListPreference viser valgt værdi i summary (alle lister)
- Korttype-skift opdaterer kort øjeblikkeligt

### AdMob
- Test-IDs i debug-build, produktions-IDs i release-build (via Gradle buildTypes)
- API-nøgler gemt i `local.properties` (ikke i repository)

### Layouts
- Android 15 edge-to-edge løst med `windowOptOutEdgeToEdgeEnforcement`
- Follow-toggle overlaid på kortet (nederst-venstre)
- ZoomControls virker med både Leaflet og Google Maps

### Repository
- `.gitignore` oprettet (local.properties, build/, .gradle/, keystores)
- Google Maps API-nøgle og AdMob produktions-IDs udeladt fra repository

---

## [2.0.0] — 2026-03-11 — Stor modernisering

### Projektstruktur
- Migreret fra Eclipse/Ant til moderne Android Gradle-projekt
- Ny mappestruktur: `app/src/main/` (Android Studio-kompatibel)
- AGP 8.10.0, Gradle 8.11.1, Java 11

### Android SDK
- minSdk: 11 → 23 (Android 6.0)
- targetSdk: 17 → 35 (Android 15)
- compileSdk: 36

### Tilladelser
- Tilføjet runtime permission-dialog for `ACCESS_FINE_LOCATION` (kræves fra API 23)
- `android:exported` tilføjet til alle aktiviteter (krav fra API 31+)

### Biblioteker
- Fjernet `android-support-v4.jar` (gammel binary)
- Tilføjet AndroidX AppCompat 1.7.1, Core 1.17.0, Preference 1.2.1
- AdMob: `com.google.ads.*` → `play-services-ads:25.0.0`

### Kode
- `MainActivity`: `Activity` → `AppCompatActivity`
- `AsyncTask` → `ExecutorService` + `Handler`
- `CompatiblePreferenceActivity` → `PreferenceFragmentCompat`
- Ny klasse: `SettingsFragment.java`
- `BannerAds.java`, `CompatiblePreferenceActivity.java`, `MyZoomControls.java` udgået

### Kort
- Google Static Maps → OpenStreetMap + Leaflet.js i WebView

---

## [1.0.0] — ca. 2013 — Første version (Eclipse/Ant)

- GPS-averaging med AsyncTask
- Google Static Maps via WebView
- Google AdMob (gammel SDK)
- Publiceret på Google Play (siden fjernet pga. manglende permission-opdatering)
