# Házi feladat specifikáció

Információk [itt](https://viauac00.github.io/laborok/hf)

## Mobil- és webes szoftverek
### 2023.10.11.
### SchPincér
### Kovács Bertalan - (RHDBLM)
### berci9ke101@gmail.com 
### Laborvezető: Kövesdán Gábor

## Bemutatás

Az alkalmazás motivációja, hogy egyszerűen értesülhessünk arról, ha a kedvenc kollégiumi "kajás"-körünk nyitást szervez, mivel sokan lemaradnak a rendelésről. Az alkalmazásban a rendelésre is lehetőség van. 
Az alkalmazás célközönsége a Schönherz kollégium lakói.

## Főbb funkciók

Az alkalmazásban lehetőség van az [SchPincér](https://schpincer.bme.hu) weboldalon való rendelésre. Az alkalmazás értesítéseket küld a felhasználónak, ha valamelyik kör nyitást szervez. Az alkalmazás eltárolja a múltbéli és a jelenlegi nyitásokat, így azokat megtekinthetjük, esetlegesen manuálisan is frissíthetjük az [SchPincerApi](https://github.com/berci9ke101/schpincerapi) segítségével. A felhasználó, minimális szinten, testreszabhatja az alkalmazás működését.


## Választott technológiák:

- (UI)
- (Fragmentek)
- (RecyclerView)
- (Hálózati kommunikáció)
- (Service)
- (Perzisztens adattárolás)
- (Notification)

# Házi feladat dokumentáció

## Az alkalmazás funkcionalitása

1. ábra

![](/src/fig1.jpg)

Az első ábrán az alkalmazás kezdőképernyője fogad minket, itt több összetevővel képes a felhasználó interakcióba lépni:
 - Az 1-es számmal jelzett részen találhatóak az aktuális nyitások, neveik, nyitási napjuk és dátumuk. Az egyes nyitásokra kattintva a második ábrán látható nézetben találja magát a felhasználó. Itt az adott nyitásokat szervező körök oldalán találhatjuk magunkat, így is elősegítve a rendelés folyamatát.

- A 2-es számmal jelzett `hamburgerbar`-ra kattintva a felhasználó a 3-as számmal jelzett menüt tudja kinyitni.

- A 3-as számmal jelzett menü, ahol 3 gomb szerepel:
  - az `Order` gomb megnyomásával a felhasználót a második ábrán látható nézetre irányítja az alkalmazás, ahol az [SchPincér](https://schpincer.sch.bme.hu) főoldalát láthatja.
  - a `Refresh` gomb megnyomására manuálisan tudja a felhasználó frissíteni a jelenlegi nyitások listáját
  - a `Settings` gomb megnyomására a harmadik ábrán illusztrált nézetben találja magát a felhasználó 

2. ábra

![](/src/fig2.jpg)

A második ábrán a felhasználó képes az [SchPincér](https://schpincer.sch.bme.hu) oldalon mozogni. Az 1-es számmal jelzett gombbal, vagy a visszagommbbal adott telefonon a felhasználó az 1-es nézetre kerülhet.

3. ábra

![](/src/fig3.jpg)

A harmadik ábrán a felhasználó az ott látható beállításokat állíthatja neki megfelelően. Ezek rendre a következőek:

  - Értesítések engedélyezése: az alkalmazás értesítést küld, ha van új nyitás 
    - Ennek megfelelően az értesítések intervallumának beállítása
  - API kommunikáció engedélyezése: az alkalmazás automatikusan frissíti a főoldalon a nyitásokat, ha van új nyitás
    - Ennek megfelelően az API kérések intervallumának beállítása
  - Az adatbázis törlése: itt a főképernyőn található nyitásokat tudja a felhasználó törölni

## Érdekesebb megoldások

A `SettingsFragment` kódjában található az alábbi kódrészlet:

```kotlin
//...
companion object {
    //...
    var developer = false
    private var cnt = 1
}

//...

private fun createListeners() {
    //...
    findPreference<Preference>(developerSecret)?.run {
        setOnPreferenceClickListener {
            triggerDeveloper()
            true
        }
    }
}

private fun becomeDeveloper() {
    Toast.makeText(this.context, "DEVELOPER MODE ON", Toast.LENGTH_SHORT).show()
    developer = true
}

private fun triggerDeveloper() {
    when {
        cnt == 8 -> {
            cnt++
            becomeDeveloper()
        }

        cnt < 8 -> cnt++
        else -> return
    }
}

//...
```

Ez a kódrészlet azért felelős, hogy, ha a beállításokban nyolcszor rányomunk a Copyright szövegre, akkor `DEVELOPER` módba lép az alkalmazás és könnyen lehet tesztelni az API-val kapcsolatos funkciókat.

Egy másik érdekesebb megoldás az `OpeningAdapter` osztályban található:

```kotlin
override fun onBindViewHolder(holder: OpeningViewHolder, position: Int) {

//...

holder.binding.llOpening.setOnClickListener {
    val bundle = Bundle()
    bundle.putString("URL", generateUrl(openingItem))
    fragment.findNavController().navigate(R.id.action_welcomeFragment_to_webFragment, bundle)
}

private fun generateUrl(openingItem: OpeningItem): String {
    return "https://schpincer.sch.bme.hu/provider/" +
            when (openingItem.circleName) {
                "Pizzásch" -> "pizzasch"
                "Americano" -> "americano"
                "ReggeliSCH" -> "reggelisch"
                "Vödör" -> "vodor"
                "Lángosch" -> "langosch"
                "Dzsájrosz" -> "dzsajrosz"
                else -> "pizzasch"
            }
}
```

Itt egy _batyu_-ban adjuk át a `WebFragment`-nek, hogy milyen `URL`-re ugorjon a navigálás során. Alatta pedig az a függvény látható, amely a lehetséges `URL` értékeket generálja.

A harmadik érdekesebb megoldás a beállítások eltárolása a `SharedPreferences`-ben, mely az Android [Settings](https://developer.android.com/develop/ui/views/components/settings) útmutatója alapján készült. Ennek a kódja és a leírása a `SettingsFragment`-ben, illetve a `preferences.xml`-ben található.
