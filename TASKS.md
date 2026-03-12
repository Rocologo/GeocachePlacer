# Opgaver

## I gang

### Google Play-publicering
- [ ] Opsætte keystore og signere release APK/AAB
- [x] Teste på fysisk Android-enhed — installeret og verificeret på Samsung Galaxy S23 Ultra
- [ ] Publicere til Google Play

## Kommende

### Forbedringer (nice-to-have)
- [ ] Tilføje offline-besked i WebView hvis ingen internetforbindelse
- [ ] Forbedre GPS-nøjagtighed: brug `onLocationChanged` aktivt i stedet for `getLastKnownLocation`
- [ ] Tilføje mulighed for at kopiere koordinater til clipboard
- [ ] Mørkt tema support
- [ ] Begræns Google Maps API-nøgle i Cloud Console med SHA-1 fingerprint

## Afsluttet

- [x] Kildekode hentet fra GitHub (Rocologo/GeocachePlacer)
- [x] Fuld kodeanalyse og migreringsplan
- [x] Migreret Eclipse/Ant → Gradle (AGP 8.10.0 / Gradle 8.11.1)
- [x] Opdateret targetSdk til 35, minSdk til 23
- [x] Tilføjet runtime GPS-tilladelse
- [x] Migreret til AndroidX (AppCompat, Core, Preference)
- [x] AdMob migreret til play-services-ads:25.0.0
- [x] OpenStreetMap + Leaflet.js implementeret (lokal bundle, ingen CDN)
- [x] Topografisk kort (OpenTopoMap) og satellit (ESRI) tilføjet
- [x] Google Maps implementeret som native MapView
- [x] Korttype-skift i indstillinger (OSM/Topo/Satellit/Google Maps)
- [x] GPS-cirkler på kort (live-opdatering via evaluateJavascript)
- [x] Follow-toggle knap (kort følger/låser position)
- [x] Koordinatformat: DM / DD / DMS
- [x] Aktuel position vises ved opstart og efter Reset
- [x] AdMob: test-IDs i debug, produktions-IDs i release (via local.properties)
- [x] Google Maps API-nøgle via local.properties (ikke i repo)
- [x] .gitignore oprettet, ingen private nøgler i repository
- [x] Pushet til GitHub (Rocologo/GeocachePlacer)
- [x] GPS-cirkler: skiftet fra pins til geografiske cirkler (røde målinger, blå gennemsnit)
- [x] Kortvisning ved opstart zoomer til telefonens GPS-position
- [x] OSM-korttyper gendannet (Standard, Topografi, Satellit)
- [x] gradlew.bat + gradle-wrapper.jar tilføjet — projektet bygger fra kommandolinje
- [x] Git multi-konto løst: credential.useHttpPath=true (Rocologo vs zenturaclj)
