Byg debug-APK og installer på Samsung Galaxy S23 Ultra via ADB.

## Kontekst

- Projekt: GeocachePlacer (Android)
- Enhed: Samsung Galaxy S23 Ultra, serienummer R5CW10QH46N
- ADB-sti: `~/Android/Sdk/platform-tools/adb`
- Gradlew: `./gradlew` i projektrodmappen

## Din opgave

Udfør disse trin i rækkefølge:

### Trin 1 — Tjek at enheden er tilgængelig

Kør:
```
~/Android/Sdk/platform-tools/adb devices
```

Tjek om `R5CW10QH46N` vises som `device`. Hvis ikke:
- Hvis den vises som `offline` eller slet ikke: bed brugeren om at tjekke USB/WiFi-forbindelsen
- Hvis WiFi ADB: kør `adb connect <IP>:<port>` (spørg brugeren om IP/port hvis ukendt)

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
~/Android/Sdk/platform-tools/adb -s R5CW10QH46N install -r app/build/outputs/apk/debug/app-debug.apk
```

Vent på "Success".

### Trin 4 — Rapporter resultat

Vis en kort opsummering:
- Build: ✅ / ❌
- Installation: ✅ / ❌
- Eventuelle advarsler fra compileren (deprecation osv.)

Hvis alt lykkedes: "Appen er klar på telefonen."
