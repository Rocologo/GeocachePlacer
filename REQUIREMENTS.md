# Kravspecifikation — GeocachePlacer

## Formål
Android-app til geocachere. Måler GPS-position gentagne gange, beregner gennemsnit og afvigelse, og viser positionen på et kort.

## Målgruppe
Geocachere der ønsker at placere en præcis cache-position.

## Platformkrav
- Android 6.0+ (API 23+)
- Kræver GPS og internetforbindelse (til kortvisning)

---

## Kortvisning

### Korttyper
- **Google Maps** (standard) — Normal, Satellit, Terræn, Hybrid
- **OpenStreetMap** — Standard, Topografi (OpenTopoMap), Satellit (ESRI)
- Brugeren vælger korttype i indstillinger
- Standard ved første opstart: Google Maps Normal

### Kortadfærd
- Kortet viser aktuel GPS-position ved opstart
- Under måling: nye målepunkter vises som røde markører på kortet
- Gennemsnitsposition vises som blå pin
- Follow-toggle (●/○): kortet følger/låser positionen
- Zoom-knapper altid synlige i kortets højre hjørne

---

## GPS-måling

- Brugeren starter måling med "Run"-knappen
- App'en måler GPS-position N gange (konfigurerbart: 1, 5, 10, 25, 50, 100, 200, eller indtil delta ≈ 0)
- Forsinkelse mellem målinger er konfigurerbar (500ms, 1s, 5s, 10s)
- Progressbar viser fremskridt
- "Pause" stopper målingen midlertidigt
- "Reset" nulstiller til aktuel GPS-position — gennemsnit og afvigelse blankes

---

## Koordinatvisning

Viser fire felter:
1. **Aktuel position** — seneste GPS-aflæsning
2. **Gennemsnit** — løbende gennemsnit af alle målinger
3. **Afvigelse** — forskel fra forrige gennemsnit til nuværende
4. **Antal koordinater** og **Højde**

### Koordinatformater (vælges i indstillinger)
- **DM** — D°M.mmm (standard, bruges af geocaching.com)
- **DD** — DD.ddddd (decimal grader)
- **DMS** — D°M'S" (grader, minutter, sekunder)

---

## Deling
- "Del"-knappen sender gennemsnitskoordinater + OpenStreetMap-link via Android share-funktion

---

## Annoncer
- AdMob banner-annonce nederst på skærmen
- Debug-build: test-IDs (ingen rigtige annoncer)
- Release-build: produktions-IDs fra local.properties

---

## Indstillinger
- Antal målinger per gennemsnit
- Forsinkelse mellem målinger
- Koordinatformat
- Korttype
- Hold skærm tændt (til/fra)
- E-mailadresse (til fejlrapporter)

---

## Sikkerhed og nøgler
- Google Maps API-nøgle må ALDRIG ligge i repository
- AdMob produktions-IDs må ALDRIG ligge i repository
- Begge skal ligge i `local.properties` (gitignored)
- Google Maps API-nøgle bør begrænses med SHA-1 fingerprint inden Go Live

---

## Google Play-krav
- `versionCode` skal altid øges ved nye releases
- `targetSdk` skal følge Google Plays krav (aktuelt: 35)
- Runtime-tilladelse for GPS skal håndteres korrekt
- Ingen brug af deprecated/forbudte APIs
