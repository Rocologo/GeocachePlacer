# GeocachePlacer

Android-app til Geocachere der vil placere en ny Geocache med præcise GPS-koordinater.

## Hvad gør appen?

Appen måler GPS-positionen gentagne gange og beregner et løbende gennemsnit samt afvigelsen (delta) fra forrige måling. Når delta nærmer sig 0, er positionen præcis nok. Positionen vises på et OpenStreetMap-kort i appen og kan deles via e-mail, SMS eller andre apps.

## Funktioner

- Måler GPS-position op til 200 gange og beregner gennemsnit
- Viser afvigelse (delta) så brugeren ved hvornår positionen er præcis
- Viser position på interaktivt OpenStreetMap-kort med zoom
- Del koordinater via alle delte apps (e-mail, SMS, WhatsApp m.fl.)
- Justerbar forsinkelse mellem målinger (500ms–10sek)
- Justerbart antal målinger (1–200 eller indtil delta = 0)

## Teknisk

- **Sprog:** Java
- **Min Android:** 5.0 (API 21)
- **Target Android:** 15 (API 35)
- **Build:** Gradle 8.11.1 / AGP 8.10.0
- **Kort:** OpenStreetMap via Leaflet.js i WebView
- **Reklamer:** Google AdMob SDK 25.0.0

## Kom i gang (udvikling)

1. Installer [Android Studio](https://developer.android.com/studio)
2. Åbn mappen `GeocachePlacer` i Android Studio
3. Lad Android Studio downloade Gradle og dependencies automatisk
4. Tilslut en Android-enhed eller start en emulator
5. Tryk Run

## Inden publicering på Google Play

- [ ] Opret/find app i [AdMob-konsollen](https://admob.google.com) og indsæt rigtige IDs
  - `AndroidManifest.xml` → `APPLICATION_ID`
  - `res/layout/main.xml` og `res/layout/about.xml` → `adUnitId`
- [ ] Opdater `versionCode` og `versionName` i `app/build.gradle`
- [ ] Opret signerings-keystore og konfigurer release build
- [ ] Test på fysisk enhed (GPS virker ikke i emulator)

## Pakkenavn

`dk.rocologo.geocacheplacer`

## Udvikler

Rocologo@hotmail.com
