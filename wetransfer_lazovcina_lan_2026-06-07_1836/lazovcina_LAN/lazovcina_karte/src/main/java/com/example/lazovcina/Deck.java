package com.example.lazovcina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private List<Card> karte;

    public Deck() {
        karte = new ArrayList<>();
        for (Suit boja : Suit.values()) {
            for (Rank vrednost : Rank.values()) {
                karte.add(new Card(boja, vrednost));
            }
        }
    }

    public void izmesaj() {
        Collections.shuffle(karte);
    }

    public List<Card> podeli(int n) {
        List<Card> podeljene = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!karte.isEmpty()) {
                podeljene.add(karte.remove(0));
            }
        }
        return podeljene;
    }

    public void dodajKarte(List<Card> noveKarte) {
        karte.addAll(noveKarte);
    }

    public boolean jePrazan() {
        return karte.isEmpty();
    }

    public int getBrojKarata() {
        return karte.size();
    }
}