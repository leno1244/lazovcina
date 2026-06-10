package com.example.lazovcina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Predstavlja jednog igrača u igri Bullshit.
 *
 * Igrač ima:
 *  - ime i redni broj (indeks) u igri
 *  - špil karata u ruci
 *  - broj života (podrazumevano 3)
 *  - da li je aktivan (nije ispao iz igre)
 */
public class Igrac {

    private final String ime;
    private final int indeks;          // 0-based indeks igrača
    private final List<Card> ruka;     // Karte u ruci
    private int brZivota;              // Broj preostalih života
    private boolean aktivan;
    private int neuspesniBullshit = 0;
    public static final int MAX_BULLSHIT = 3;

    public static final int POCETNI_ZIVOTI = 3;

    public Igrac(String ime, int indeks) {
        this.ime = ime;
        this.indeks = indeks;
        this.ruka = new ArrayList<>();
        this.brZivota = POCETNI_ZIVOTI;
        this.aktivan = true;
    }

    // ─────────────────────────────────────────────────────────────
    // Manipulacija kartama u ruci
    // ─────────────────────────────────────────────────────────────

    /** Dodaje jednu kartu u ruku. */
    public void dodajKartu(Card karta) {
        ruka.add(karta);
    }

    /** Dodaje listu karata u ruku (kazna - uzima sa stola). */
    public void dodajKarte(List<Card> karte) {
        ruka.addAll(karte);
    }

    /**
     * Igrač baca karte sa određenim indeksima iz ruke na sto.
     * Vraća listu bacenih karata i uklanja ih iz ruke.
     *
     * @param indeksiKarata indeksi karata u listi ruke (0-based)
     * @return stvarno bacene karte
     */
    public List<Card> baciKarte(List<Integer> indeksiKarata) {
        // Sortiramo u opadajućem redosledu da ne poremetimo indekse pri brisanju
        List<Integer> sortirani = new ArrayList<>(indeksiKarata);
        sortirani.sort(Collections.reverseOrder());

        List<Card> bacene = new ArrayList<>();
        for (int idx : sortirani) {
            if (idx >= 0 && idx < ruka.size()) {
                bacene.add(0, ruka.remove(idx)); // dodaj na početak da sačuvamo red
            }
        }
        return bacene;
    }

    /** Vraća nepromjenjivu kopiju ruke (za prikaz). */
    public List<Card> getRuka() {
        return Collections.unmodifiableList(ruka);
    }

    /** Broj karata u ruci. */
    public int getBrojKarata() {
        return ruka.size();
    }

    /** Da li je igrač isprao ruku (pobedio)? */
    public boolean imaKarte() {
        return !ruka.isEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // Životi
    // ─────────────────────────────────────────────────────────────

    /** Oduzima jedan život. Ako padne na 0, igrač postaje neaktivan. */
    public boolean izgubioZivot() {
        if (brZivota > 0) {
            brZivota--;
        }
        if (brZivota == 0) {
            aktivan = false;
        }
        return aktivan; // true = još je u igri
    }

    public int getBrZivota() {
        return brZivota;
    }

    public boolean isAktivan() {
        return aktivan;
    }

    // ─────────────────────────────────────────────────────────────
    // Getteri
    // ─────────────────────────────────────────────────────────────

    public String getIme() {
        return ime;
    }

    public int getIndeks() {
        return indeks;
    }

    @Override
    public String toString() {
        return ime + " [ruka=" + ruka.size() + ", zivoti=" + brZivota + "]";
    }
    public boolean dodajNeuspesniBullshit() {
        neuspesniBullshit++;
        return neuspesniBullshit >= MAX_BULLSHIT;
    }

    public int getNeuspesniBullshit() {
        return neuspesniBullshit;
    }


}
