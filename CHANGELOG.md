# Changelog

## [2.0.0] — 2026-03-11 — Stor modernisering

### Projektstruktur
- Migreret fra Eclipse/Ant til moderne Android Gradle-projekt
- Ny mappestruktur: `app/src/main/` (Android Studio-kompatibel)
- AGP 8.10.0, Gradle 8.11.1, Java 11

### Android SDK
- minSdk: 11 → 21 (Android 5.0)
- targetSdk: 17 → 35 (Android 15) — krav fra Google Play
- compileSdk: 35

### Tilladelser
- Tilføjet runtime permission-dialog for `ACCESS_FINE_LOCATION` (kræves fra API 23)
- Tilføjet `ACCESS_COARSE_LOCATION`
- `android:exported` tilføjet til alle aktiviteter (krav fra API 31+)

### Biblioteker
- Fjernet `android-support-v4.jar` (gammel binary)
- Tilføjet AndroidX AppCompat 1.7.1
- Tilføjet AndroidX Core 1.17.0
- Tilføjet AndroidX Preference 1.2.1
- AdMob: `com.google.ads.*` (dead SDK) → `play-services-ads:25.0.0`

### Kode
- `MainActivity`: `Activity` → `AppCompatActivity`
- `AsyncTask` (deprecated API 30) → `ExecutorService` + `Handler`
- `CompatiblePreferenceActivity` (Eclipse-hack) → `PreferenceFragmentCompat`
- Ny klasse: `SettingsFragment.java`
- `BannerAds.java`, `CompatiblePreferenceActivity.java`, `MyZoomControls.java` udgået
- `AboutActivity`: Fikset `FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET` → `FLAG_ACTIVITY_NEW_TASK`

### Kort
- Google Static Maps (kræver API-nøgle, old URL-format) → **OpenStreetMap + Leaflet.js**
- Leaflet loades fra CDN (unpkg.com), vises i WebView
- Delte koordinater linker nu til OpenStreetMap i stedet for Google Maps

### Layouts
- `com.google.ads.AdView` → `com.google.android.gms.ads.AdView`
- Namespace `xmlns:ads` opdateret til `http://schemas.android.com/apk/res-auto`
- `layout-land/main.xml` omskrevet til LinearLayout + RelativeLayout (bedre landscape-layout)
- `about.xml` omskrevet til LinearLayout (simplere)

---

## [1.0.0] — ca. 2013 — Første version (Eclipse/Ant)

- GPS-averaging med AsyncTask
- Google Static Maps via WebView
- Google AdMob (gammel SDK)
- Publiceret på Google Play (siden fjernet pga. manglende permission-opdatering)
