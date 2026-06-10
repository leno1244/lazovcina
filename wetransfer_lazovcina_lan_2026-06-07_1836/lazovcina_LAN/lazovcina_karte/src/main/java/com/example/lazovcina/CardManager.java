package com.example.lazovcina;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class CardManager {

    private Deck spil;
    private List<Card> mojaRuka;
    private List<Card> rukaIgraca2;
    private List<Card> rukaIgraca3;
    private List<Card> rukaIgraca4;
    private List<Card> oznaceneKarte;

    public CardManager() {
        spil = new Deck();
        spil.izmesaj();

        mojaRuka = spil.podeli(13);
        rukaIgraca2 = spil.podeli(13);
        rukaIgraca3 = spil.podeli(13);
        rukaIgraca4 = spil.podeli(13);

        oznaceneKarte = new ArrayList<>();
    }

    public void popuniMojuRuku(HBox myHand) {
        myHand.getChildren().clear();
        int brojKarata = mojaRuka.size();
        double spacing = brojKarata > 10 ? -40 : -20;
        myHand.setSpacing(spacing);
        for (Card karta : mojaRuka) {
            postaviKlikNaKartu(karta, myHand);
            myHand.getChildren().add(karta);
        }
    }

    public void popuniRukuIgraca2(VBox player2Hand) {
        player2Hand.getChildren().clear();
        for (Card karta : rukaIgraca2) {
            ImageView poledjina = napraviPoledjinu();
            poledjina.setRotate(90);
            player2Hand.getChildren().add(poledjina);
        }
    }

    public void popuniRukuIgraca3(HBox player3Hand) {
        player3Hand.getChildren().clear();
        player3Hand.setSpacing(-45.0);
        player3Hand.setAlignment(javafx.geometry.Pos.CENTER);
        for (Card karta : rukaIgraca3) {
            ImageView poledjina = new ImageView(
                    new Image(getClass().getResourceAsStream("card_back.png"))
            );
            poledjina.setFitWidth(85);
            poledjina.setFitHeight(120);
            poledjina.setPreserveRatio(true);
            poledjina.setRotate(180);
            player3Hand.getChildren().add(poledjina);
        }
    }


    public void popuniRukuIgraca4(VBox player4Hand) {
        player4Hand.getChildren().clear();
        for (Card karta : rukaIgraca4) {
            ImageView poledjina = napraviPoledjinu();
            poledjina.setRotate(270);
            player4Hand.getChildren().add(poledjina);
        }
    }

    private ImageView napraviPoledjinu() {
        ImageView poledjina = new ImageView(
                new Image(getClass().getResourceAsStream("card_back.png"))
        );
        poledjina.setFitWidth(70);
        poledjina.setFitHeight(100);
        poledjina.setPreserveRatio(true);
        return poledjina;
    }

    private void postaviKlikNaKartu(Card karta, HBox kontejner) {
        karta.setOnMouseClicked(e -> {
            if (!karta.isOznacena()) {
                karta.setTranslateY(-20);
                karta.setOznacena(true);
                oznaceneKarte.add(karta);
            } else {
                karta.setTranslateY(0);
                karta.setOznacena(false);
                oznaceneKarte.remove(karta);
            }
        });
    }

    public List<Card> getOznaceneKarte() {
        return oznaceneKarte;
    }

    public void ukloniOznaceneIzRuke() {
        mojaRuka.removeAll(oznaceneKarte);
        oznaceneKarte.clear();
    }

    public List<Card> getMojaRuka() {
        return mojaRuka;
    }

    public List<Card> getRukaIgraca2() {
        return rukaIgraca2;
    }

    public List<Card> getRukaIgraca3() {
        return rukaIgraca3;
    }

    public List<Card> getRukaIgraca4() {
        return rukaIgraca4;
    }
}