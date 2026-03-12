# Projektstatus

Opdateret: 2026-03-12

## Byggeprojekt

| Komponent | Status | Note |
|---|---|---|
| Gradle-struktur | ✅ | AGP 8.10.0 / Gradle 8.11.1, gradlew.bat tilføjet |
| AndroidManifest.xml | ✅ | targetSdk 35, runtime permissions |
| Java-kildekode | ✅ | 5 klasser, BUILD SUCCESSFUL |
| OpenStreetMap/Leaflet | ✅ | Lokal bundle, ingen CDN |
| Topografi + Satellit | ✅ | OpenTopoMap + ESRI |
| Google Maps | ✅ | Native MapView, API-nøgle i local.properties |
| GPS-cirkler | ✅ | Geografiske cirkler r=8m — røde målinger, blå gennemsnit |
| Kortvisning ved opstart | ✅ | Zoomer til telefonens GPS-position |
| Koordinatformater | ✅ | DM / DD / DMS |
| Live kort-opdatering | ✅ | evaluateJavascript, ingen reload under måling |
| Follow-toggle | ✅ | ●/○ knap på kortet |
| AdMob SDK | ✅ | Test-IDs i debug, rigtige IDs i release |
| API-nøgler i repo | ✅ | Ingen — local.properties er gitignored |
| Test på fysisk enhed | ✅ | Testet på Samsung Galaxy S23 Ultra |
| Signering/release | ❌ | Keystore ikke sat op endnu |

## Kendte mangler

- **Keystore mangler** — release APK/AAB kan ikke signeres til Google Play
- **`android.preference.PreferenceManager`** — deprecated men virker
- **GPS**: bruger `getLastKnownLocation` — kan give forældet position ved første opstart

## Risici

- Kort kræver internetforbindelse — tiles vises ikke offline
- Google Maps API-nøgle er ubegrænset (ingen SHA-1 begrænsning endnu) — bør tilføjes inden Go Live
