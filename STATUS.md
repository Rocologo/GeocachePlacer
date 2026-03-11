# Projektstatus

Opdateret: 2026-03-11

## Byggeprojekt

| Komponent | Status | Note |
|---|---|---|
| Gradle-struktur | ✅ Oprettet | AGP 8.10.0 / Gradle 8.11.1 |
| AndroidManifest.xml | ✅ Opdateret | targetSdk 35, runtime permissions |
| Java-kildekode | ✅ Migreret | 5 klasser, ingen compile-fejl forventet |
| Layouts | ✅ Opdateret | AdMob + OSM klar |
| OpenStreetMap/Leaflet | ✅ Implementeret | Kræver internetforbindelse |
| AdMob SDK | ⚠️ Test-IDs | Rigtige IDs skal indsættes |
| Google Maps | ❌ Ikke implementeret | Kan tilføjes med API-nøgle |
| Signering/release | ❌ Ikke sat op | Skal gøres inden Google Play |
| Faktisk kompilering | ✅ Bekræftet | `BUILD SUCCESSFUL` — APK på 8.5MB |

## Kendte mangler

- **AdMob IDs mangler** — test-IDs bruges, ingen reklameindtægt
- **Ikke kompileret** — projektet er ikke åbnet i Android Studio endnu
- **Ikke testet på enhed** — GPS-funktionalitet ikke verificeret
- **Ingen keystore** — release APK/AAB kan ikke signeres
- **`android.preference.PreferenceManager`** — bruges i MainActivity, deprecated men virker

## Risici

- Leaflet CDN kræver internet — kort vises ikke offline (samme begrænsning som Google Static Maps havde)
- `getLastKnownLocation` kan returnere null på første brug — håndteres i GPSTracker (returnerer 0,0)
