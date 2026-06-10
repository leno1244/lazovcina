package com.example.lazovcina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gomila karata na sredini stola.
 *
 * Čuva SVE karte koje su bačene od poslednjeg čišćenja,
 * a posebno pamti POSLEDNJI POT (karte koje je bacio poslednji igrač)
 * jer se one proveravaju kada neko viče "BULLSHIT".
 */
public class GomilaNaStolu {

    /** Sve karte na gomili (od prvog bacanja u rundi). */
    private final List<Card> sveKarte;

    /**
     * Poslednje bačene karte + prijavljeni rang.
     * Ovo je ono što se proverava pri pozivu BULLSHIT.
     */
    private List<Card> poslednjeKarte;
    private Rank prijavljeniRang;   // Ono što je igrač rekao da je bacio
    private int indeksPoslednjegIgraca; // Ko je bacio poslednji

    public GomilaNaStolu() {
        this.sveKarte = new ArrayList<>();
        this.poslednjeKarte = new ArrayList<>();
        this.prijavljeniRang = null;
        this.indeksPoslednjegIgraca = -1;
    }

    // ─────────────────────────────────────────────────────────────
    // Bacanje karata
    // ─────────────────────────────────────────────────────────────

    /**
     * Igrač baca karte na gomilu.
     *
     * @param karte         stvarne karte koje je igrač bacio
     * @param prijavljeni   rang koji je igrač OBJAVIO (može lagati)
     * @param indeksIgraca  ko baca
     */
    public void dodajBacanje(List<Card> karte, Rank prijavljeni, int indeksIgraca) {
        sveKarte.addAll(karte);
        poslednjeKarte = new ArrayList<>(karte);
        prijavljeniRang = prijavljeni;
        indeksPoslednjegIgraca = indeksIgraca;
    }

    // ─────────────────────────────────────────────────────────────
    // Provjera blefa (BULLSHIT!)
    // ─────────────────────────────────────────────────────────────

    /**
     * Proverava da li je poslednji igrač lagao.
     *
     * Igrač je lagao ako IJEDNA od poslednjih karata nije odgovarajućeg ranga.
     *
     * @return true  = lagao (ko je vikao BULLSHIT uzima gomilu)
     *         false = nije lagao (ko je vikao BULLSHIT uzima gomilu)
     */
    public boolean proveraBlefa() {
        if (prijavljeniRang == null || poslednjeKarte.isEmpty()) {
            return false; // nema šta da se proverava
        }
        for (Card karta : poslednjeKarte) {
            if (karta.getRank() != prijavljeniRang) {
                return true; // lagao!
            }
        }
        return false; // sve karte su ispravnog ranga – nije lagao
    }

    // ─────────────────────────────────────────────────────────────
    // Uzimanje gomile (kazna)
    // ─────────────────────────────────────────────────────────────

    /**
     * Vraća sve karte sa stola i čisti gomilu.
     * Poziva se kada se određuje ko kažnjava.
     */
    public List<Card> uzmiSveKarte() {
        List<Card> kopija = new ArrayList<>(sveKarte);
        ocisti();
        return kopija;
    }

    /** Čisti gomilu (nova runda). */
    public void ocisti() {
        sveKarte.clear();
        poslednjeKarte.clear();
        prijavljeniRang = null;
        indeksPoslednjegIgraca = -1;
    }

    // ─────────────────────────────────────────────────────────────
    // Getteri
    // ─────────────────────────────────────────────────────────────

    public boolean isPrazna() {
        return sveKarte.isEmpty();
    }

    public int getBrojKarata() {
        return sveKarte.size();
    }

    public List<Card> getPoslednjeKarte() {
        return Collections.unmodifiableList(poslednjeKarte);
    }

    public Rank getPrijavljeniRang() {
        return prijavljeniRang;
    }

    public int getIndeksPoslednjegIgraca() {
        return indeksPoslednjegIgraca;
    }

    public List<Card> getSveKarte() {
        return Collections.unmodifiableList(sveKarte);
    }
}
