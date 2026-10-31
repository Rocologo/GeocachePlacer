Byg debug-APK og installer på Samsung Galaxy S23 Ultra via ADB.

## Kontekst

- Projekt: GeocachePlacer (Android)
- Enhed: Samsung Galaxy S23 Ultra, serienummer R5CW10QH46N
- WiFi-IP: `192.168.2.204` (fast via DHCP-reservation). Porten skifter hver gang trådløs fejlretning slås til
- Maskinen er allerede parret med telefonen (parring gemt i `~/.android/`)
- ADB-sti: `~/Android/Sdk/platform-tools/adb`
- Gradlew: `./gradlew` i projektrodmappen

## Din opgave

Udfør disse trin i rækkefølge:

### Trin 1 — Find og forbind telefonen

Sæt `ADB=~/Android/Sdk/platform-tools/adb`. Kør:
```
$ADB devices -l
```

Hvis en enhed med `model:SM_S918B` vises som `device`, brug dens serial (første kolonne) som `<SERIAL>` og gå til trin 2.

Ellers find connect-porten via mDNS:
```
$ADB mdns services
```
Find linjen `adb-R5CW10QH46N-…  _adb-tls-connect._tcp  192.168.2.204:<port>` og kør:
```
$ADB connect 192.168.2.204:<port>
```
`<SERIAL>` er så `192.168.2.204:<port>`.

Hvis mDNS ikke viser telefonen:
- Bed brugeren slå **Trådløs fejlretning** til (Udviklerindstillinger) og slå **Twingate fra** — med Twingate aktiv viser telefonen sin tunnel-IP (10.254.x), som ikke kan nås
- Spørg evt. om porten fra skærmen "Trådløs fejlretning" og forbind til `192.168.2.204:<port>`
- Hvis connect fejler med "failed to connect" selvom porten er åben, er parringen tabt: bed brugeren trykke "Par enhed med parringskode" og sende koden. Pairing-porten findes i `$ADB mdns services` (`_adb-tls-pairing._tcp`); kør `$ADB pair 192.168.2.204:<pairport> <kode>`

Fortsæt kun hvis enheden er tilgængelig.

### Trin 2 — Byg debug-APK

Kør fra projektrodmappen:
```
./gradlew :app:assembleDebug
```

Vent på "BUILD SUCCESSFUL". Hvis build fejler: vis fejlbesked og stop — ret ikke koden automatisk, men rapporter fejlen til brugeren.

### Trin 3 — Installer på telefon

Kør:
```
~/Android/Sdk/platform-tools/adb -s <SERIAL> install -r app/build/outputs/apk/debug/app-debug.apk
```

Vent på "Success".

### Trin 4 — Rapporter resultat

Vis en kort opsummering:
- Build: ✅ / ❌
- Installation: ✅ / ❌
- Eventuelle advarsler fra compileren (deprecation osv.)

Hvis alt lykkedes: "Appen er klar på telefonen."
