# Opgaver

## I gang

### Modernisering og Google Play-publicering
- [x] Migrere Eclipse/Ant → Gradle
- [x] Opdatere targetSdk til 35
- [x] Tilføje runtime GPS-tilladelse
- [x] Migrere AdMob til ny SDK (play-services-ads:25.0.0)
- [x] Implementere OpenStreetMap/Leaflet som kortalternativ
- [x] Migrere til AndroidX (AppCompat, Preference)
- [x] Verificere projektet kompilerer (BUILD SUCCESSFUL via Gradle CLI)
- [ ] Teste på fysisk Android-enhed (GPS, kort, AdMob)
- [ ] Indsætte rigtige AdMob IDs (Application ID + Ad Unit IDs)
- [ ] Opsætte keystore og signere release APK/AAB
- [ ] Publicere til Google Play

## Kommende

### Google Maps support
- [ ] Finde/oprette Google Maps API-nøgle (Google Cloud Console)
- [ ] Implementere Google Maps som alternativ til OpenStreetMap
- [ ] Tilbyde brugeren valg mellem OSM og Google Maps i indstillinger

### Forbedringer (nice-to-have)
- [ ] Migrere `android.preference.PreferenceManager` til AndroidX
- [ ] Tilføje offline-besked i WebView hvis ingen internetforbindelse
- [ ] Forbedre GPS-nøjagtighed: brug `onLocationChanged` aktivt i stedet for `getLastKnownLocation`
- [ ] Tilføje mulighed for at kopiere koordinater til clipboard
- [ ] Mørkt tema support

## Afsluttet

- [x] Kildekode hentet fra GitHub (Rocologo/GeocachePlacer)
- [x] Fuld kodeanalyse
- [x] Migreringsplan udarbejdet
- [x] Alle filer oprettet (22 filer)
