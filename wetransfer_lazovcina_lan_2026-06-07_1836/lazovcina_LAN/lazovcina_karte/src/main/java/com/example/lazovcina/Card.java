package com.example.lazovcina;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Card extends ImageView {

    private final Suit suit;
    private final Rank rank;
    private boolean oznacena = false;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;

        String putanja = napraviPutanju(rank, suit);
        Image slika = new Image(getClass().getResourceAsStream(putanja));
        this.setImage(slika);

        this.setFitWidth(90);
        this.setFitHeight(130);
        this.setPreserveRatio(true);
    }

    private String napraviPutanju(Rank rank, Suit suit) {
        String rankDeo;
        switch (rank) {
            case AS:       rankDeo = "ace"; break;
            case DVOJKA:   rankDeo = "2"; break;
            case TROJKA:   rankDeo = "3"; break;
            case CETVORKA: rankDeo = "4"; break;
            case PETICA:   rankDeo = "5"; break;
            case SESTICA:  rankDeo = "6"; break;
            case SEDMICA:  rankDeo = "7"; break;
            case OSMICA:   rankDeo = "8"; break;
            case DEVETKA:  rankDeo = "9"; break;
            case DESETKA:  rankDeo = "10"; break;
            case DECKO:    rankDeo = "jack"; break;
            case DAMA:     rankDeo = "queen"; break;
            case KRALJ:    rankDeo = "king"; break;
            default:       rankDeo = ""; break;
        }

        String suitDeo;
        switch (suit) {
            case PIK:  suitDeo = "spades"; break;
            case KARO: suitDeo = "diamonds"; break;
            case HERC: suitDeo = "hearts"; break;
            case TREF: suitDeo = "clubs"; break;
            default:   suitDeo = ""; break;
        }

        return rankDeo + "_of_" + suitDeo + ".png";
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isOznacena() {
        return oznacena;
    }

    public void setOznacena(boolean oznacena) {
        this.oznacena = oznacena;
    }

    @Override
    public String toString() {
        return rank.name() + "_" + suit.name();
    }
}