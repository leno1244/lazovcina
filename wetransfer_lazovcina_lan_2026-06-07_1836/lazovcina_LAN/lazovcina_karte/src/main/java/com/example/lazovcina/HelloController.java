package com.example.lazovcina;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class HelloController {

    // === SLOJEVI (LAYERS) ===
    @FXML
    private AnchorPane layerHost;
    @FXML
    private AnchorPane layerGuest;
    @FXML
    private AnchorPane layerMainMenu;
    @FXML
    private AnchorPane layerRules;
    @FXML
    private AnchorPane layerServerList;
    @FXML
    private AnchorPane layerWaiting;
    @FXML
    private AnchorPane layerGameTable;
    @FXML
    private AnchorPane layerBullshitAction;
    @FXML
    private AnchorPane layerMyTurn;
    @FXML
    private AnchorPane layerShowdown;
    @FXML
    private AnchorPane layerFullRoomError;
    @FXML
    private AnchorPane layerEndScreen;

    // === ELEMENTI UNUTAR SLOJEVA ===
    @FXML
    private ImageView backgroundImage;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField serverNameField;
    @FXML
    private TextField ipAddressField;

    @FXML
    private HBox p2Lives;
    @FXML
    private HBox p3Lives;
    @FXML
    private HBox p4Lives;
    @FXML
    private HBox myLives;
    @FXML
    private VBox player2Hand;
    @FXML
    private HBox player3Hand;
    @FXML
    private VBox player4Hand;
    @FXML
    private HBox myHand;
    @FXML
    private StackPane mainCardPile;
    @FXML
    private AnchorPane layerLiarBubble;
    @FXML
    private VBox liarBubbleP2;
    @FXML
    private VBox liarBubbleP3;
    @FXML
    private VBox liarBubbleP4;
    @FXML
    private VBox liarBubbleMe;
    @FXML
    private Label liarCenterLabel;
    @FXML
    private Label liarTailP2;
    @FXML
    private Label liarTailP3;
    @FXML
    private Label liarTailP4;
    @FXML
    private Label liarTailMe;
    @FXML
    private Label labelPlayer2;
    @FXML
    private Label labelPlayer3;
    @FXML
    private Label labelPlayer4;
    @FXML
    private Label labelMyName;
    @FXML
    private Arc timerArc;
    @FXML
    private Canvas timerCanvas;
    @FXML
    private StackPane timerCirclePane;
    @FXML
    private Label timerLabel;
    private Timeline timerTimeline;
    @FXML
    private HBox bullshitCardsContainer;
    @FXML
    private Label bullshitCardCountLabel;
    @FXML
    private Label bullshitStatusLabel;
    @FXML
    private Label showdownVerdictLabel;
    @FXML
    private HBox showdownCardsContainer;
    @FXML
    private Label winnerLabel;
    @FXML
    private Label labelWaitingStatus;
    @FXML
    private VBox waitingPlayersList;
    @FXML
    private VBox roomListContainer;
    @FXML
    private Button backToMenuButton;
    @FXML private Label labelBrojKarataP2;
    @FXML private Label labelBrojKarataP3;
    @FXML private Label labelBrojKarataP4;
    @FXML private Label labelBrojKarataMe;
    @FXML private Label labelTrazeniRang;
    // === NOVA POLJA ZA MREŽNI SLOJ ===
    private NetworkManager networkManager;
    private CardManager cardManager;
    private String trenutnoTrazeniRank = "";
    private int poslednjiBacioID = 0;
    private List<String> igracUCekaonici = new ArrayList<>();
    private GlavnaLogika logika = new GlavnaLogika();
    private boolean bullshitAktivan = false;

    @FXML
    public void initialize() {
        networkManager = new NetworkManager(this);
        sakrijSveSlojeve();
        layerMainMenu.setVisible(true);
    }

    private void sakrijSveSlojeve() {
        layerMainMenu.setVisible(false);
        layerHost.setVisible(false);
        layerGuest.setVisible(false);
        layerRules.setVisible(false);
        layerServerList.setVisible(false);
        layerWaiting.setVisible(false);
        layerGameTable.setVisible(false);
        layerBullshitAction.setVisible(false);
        layerLiarBubble.setVisible(false);
        layerMyTurn.setVisible(false);
        layerShowdown.setVisible(false);
        layerFullRoomError.setVisible(false);
        layerEndScreen.setVisible(false);
    }

    // === 1. AKCIJE SA GLAVNOG MENIJA ===

    private String osigurajUsername(String prefiks) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            int randomBroj = 100000 + new Random().nextInt(900000);
            username = prefiks + randomBroj;
            usernameField.setText(username);
        }
        return username;
    }

    @FXML
    void handleHostGame(ActionEvent event) {
        labelMyName.setText(osigurajUsername("Host_Igrac"));
        sakrijSveSlojeve();
        layerHost.setVisible(true);
    }

    @FXML
    void handleJoinGame(ActionEvent event) {
        labelMyName.setText(osigurajUsername("Gost_Igrac"));
        sakrijSveSlojeve();
        layerGuest.setVisible(true);
    }

    @FXML
    void handleConfirmHost(ActionEvent event) {
        if (networkManager == null) {
            networkManager = new NetworkManager(this);
        }
        String serverName = serverNameField.getText().trim();
        if (serverName.isEmpty()) {
            serverNameField.setPromptText("⚠ Unesi ime servera!");
            serverNameField.setStyle("-fx-background-radius: 5; -fx-padding: 8; -fx-border-color: red; -fx-border-width: 2;");
            return;
        }
        String username = osigurajUsername("Host_Igrac");
        sakrijSveSlojeve();
        layerWaiting.setVisible(true);
        networkManager.startAsHost(serverName, username);
    }

    @FXML
    void handleConfirmGuest(ActionEvent event) {
        String ip = ipAddressField.getText().trim();
        if (ip.isEmpty()) {
            ipAddressField.setPromptText("⚠ Unesi IP adresu!");
            ipAddressField.setStyle("-fx-background-radius: 5; -fx-padding: 8; -fx-border-color: red; -fx-border-width: 2;");
            return;
        }
        boolean jeIspravna = ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") ||
                ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d+") ||
                ip.matches("[a-zA-Z0-9._-]+:\\d+");
        if (!jeIspravna) {
            ipAddressField.setText("");
            ipAddressField.setPromptText("⚠ Neispravan format! (npr. 192.168.0.106)");
            ipAddressField.setStyle("-fx-background-radius: 5; -fx-padding: 8; -fx-border-color: red; -fx-border-width: 2;");
            return;
        }
        String username = osigurajUsername("Gost_Igrac");
        sakrijSveSlojeve();
        layerWaiting.setVisible(true);
        networkManager.startAsGuest(username, ip);
    }

    @FXML
    void handleShowRules(ActionEvent event) {
        sakrijSveSlojeve();
        layerRules.setVisible(true);
    }

    @FXML
    void handleHideRules(ActionEvent event) {
        sakrijSveSlojeve();
        layerMainMenu.setVisible(true);
    }

    // === 2. AKCIJE SA SERVER LISTE ===

    @FXML
    void handleSelectRoom(ActionEvent event) {
        System.out.println("Uspešno odabrana soba! Povezivanje na LAN sesiju...");
        sakrijSveSlojeve();
        layerWaiting.setVisible(true);
    }

    @FXML
    void handleBackToMenu(ActionEvent event) {
        networkManager.shutdown();
        networkManager = new NetworkManager(this);
        sakrijSveSlojeve();
        layerMainMenu.setVisible(true);
    }

    public void dodajSobuUListu(String nazivSobe, int trenutno, int max) {
        String serverIp = "127.0.0.1";
        if (nazivSobe.contains("(") && nazivSobe.contains(")")) {
            serverIp = nazivSobe.substring(
                    nazivSobe.lastIndexOf('(') + 1,
                    nazivSobe.lastIndexOf(')')
            );
        }

        HBox row = new HBox(20.0);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 5; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 0 1 0;");

        Label lblNaziv = new Label(nazivSobe);
        lblNaziv.setPrefWidth(250.0);
        lblNaziv.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px;");

        Label lblBroj = new Label(trenutno + " / " + max);
        lblBroj.setPrefWidth(80.0);
        lblBroj.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px;");

        Button btnJoin = new Button("JOIN");
        String boja = (trenutno < max) ? "#00aa00" : "#555555";
        btnJoin.setStyle("-fx-background-color: " + boja + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnJoin.setDisable(trenutno >= max);

        final String finalIp = serverIp;
        btnJoin.setOnAction(e -> {
            String username = osigurajUsername("Gost_Igrac");
            sakrijSveSlojeve();
            layerWaiting.setVisible(true);
            networkManager.startAsGuest(username, finalIp);
        });

        row.getChildren().addAll(lblNaziv, lblBroj, btnJoin);
        roomListContainer.getChildren().add(row);
    }

    public void ocistiListuSoba() {
        roomListContainer.getChildren().clear();
    }

    // === 3. AKCIJE IZ ČEKAONICE ===

    @FXML
    void handleLeaveWaiting(ActionEvent event) {
        System.out.println("Korisnik je napustio čekaonicu pre početka.");
        if (networkManager.isHost()) {
            networkManager.posaljiPoruku(Protocol.build(Protocol.IGRAC_NAPUSTIO, "1"));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        networkManager.shutdown();
        networkManager = new NetworkManager(this);
        sakrijSveSlojeve();
        layerMainMenu.setVisible(true);
    }

    public void azurirajCekaonicu(String igrac, int trenutniBroj, int maxBroj) {
        labelWaitingStatus.setText("Igrači u sobi: " + trenutniBroj + "/" + maxBroj);
        if (!igracUCekaonici.contains(igrac)) {
            igracUCekaonici.add(igrac);
        }
        waitingPlayersList.getChildren().clear();
        for (String ime : igracUCekaonici) {
            Label playerLabel = new Label("▶  " + ime);
            playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 4 0 4 0;");
            waitingPlayersList.getChildren().add(playerLabel);
        }
        if (trenutniBroj == maxBroj) {


        }
    }
    // === 4. AKCIJE TOKOM TVOG POTEZA ===



    // === 5. AKCIJE ZA BULLSHIT PANEL ===

    @FXML
    void handlePlayCards(ActionEvent event) {
        if (cardManager == null) return;
        List<Card> odabrane = cardManager.getOznaceneKarte();
        if (odabrane.isEmpty()) return;

        int broj = odabrane.size();
        if (broj > 4) {
            // Deselektuj sve iznad 4 - ili samo blokiraj
            // Najjednostavnije: upozori i ne dozvoli
            bullshitStatusLabel.setText(" Možeš baciti najviše 4 karte!");
            layerBullshitAction.setVisible(true); // privremeno prikaži poruku
            // Sakrij nakon 2 sekunde
            new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (layerBullshitAction.isVisible() && cardManager.getOznaceneKarte().size() > 4) {
                    layerBullshitAction.setVisible(false);
                }
            })).play();
            return;
        }
        layerMyTurn.setVisible(false);
        cardManager.ukloniOznaceneIzRuke();
        cardManager.popuniMojuRuku(myHand);

        int preostaloBrojKarata = cardManager.getMojaRuka().size();
        azurirajBrojKarata(networkManager.getMyPlayerID(), preostaloBrojKarata);

        networkManager.posaljiBaciKartu(networkManager.getMyPlayerID(), broj, trenutnoTrazeniRank);
        // Pošalji broadcast o novom broju karata
        networkManager.posaljiAzuriranjeBrojaKarata(networkManager.getMyPlayerID(), preostaloBrojKarata);
    }

    public void aktivirajShowdown(String poruka, List<String> karteSlike) {
        showdownVerdictLabel.setText(poruka);

        showdownCardsContainer.getChildren().clear();
        for (String slikaPath : karteSlike) {
            ImageView cardView = new ImageView();
            try {
                cardView.setImage(new Image(getClass().getResourceAsStream(slikaPath)));
            } catch (Exception e) {
                cardView.setImage(new Image(getClass().getResourceAsStream("card_back.png")));
            }
            cardView.setFitWidth(90);
            cardView.setFitHeight(130);
            cardView.setPreserveRatio(true);
            showdownCardsContainer.getChildren().add(cardView);
        }

        layerGameTable.setVisible(true);
        layerShowdown.setVisible(true);
    }

    public void updatePlayerLives(int playerID, int livesLeft) {
        HBox targetHBox;
        switch (playerID) {
            case 1:
                targetHBox = myLives;
                break;
            case 2:
                targetHBox = p2Lives;
                break;
            case 3:
                targetHBox = p3Lives;
                break;
            case 4:
                targetHBox = p4Lives;
                break;
            default:
                return;
        }

        for (int i = 0; i < 3; i++) {
            Label xLabel = (Label) targetHBox.getChildren().get(i);
            if (i >= livesLeft) {
                xLabel.setStyle("-fx-text-fill: rgba(255,0,0,0.25); -fx-font-size: 20px; -fx-font-weight: bold;");
            } else {
                xLabel.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold;");
            }
        }
    }

    public void prikaziBaceneKarteNaStolu(int count) {
        mainCardPile.getChildren().clear();

        HBox cardsLayout = new HBox();
        cardsLayout.setAlignment(javafx.geometry.Pos.CENTER);
        cardsLayout.setSpacing(-40.0);

        for (int i = 0; i < count; i++) {
            ImageView cardView = new ImageView(new Image(getClass().getResourceAsStream("card_back.png")));
            cardView.setFitWidth(90);
            cardView.setFitHeight(130);
            cardsLayout.getChildren().add(cardView);
        }
        mainCardPile.getChildren().add(cardsLayout);
    }

    // === 6. AKCIJA SA SHOWDOWN OVERLAY-A ===

    @FXML
    void handleContinueFromShowdown(ActionEvent event) {
        layerShowdown.setVisible(false);
        layerGameTable.setVisible(true);
        System.out.println("Showdown završen. Igra se nastavlja na stolu.");
    }

    // === 7. AKCIJA SA PROZORA ZA GREŠKU ===

    @FXML
    void handleCloseError(ActionEvent event) {
        sakrijSveSlojeve();
        layerMainMenu.setVisible(true);
    }

    // === 8. AKCIJA SA ZAVRŠNOG EKRANA ===

    @FXML
    void handleBackToMenuFromEnd(ActionEvent event) {
        System.out.println("Resetovanje parametara i povratak u glavni meni.");
        networkManager.shutdown();
        networkManager = new NetworkManager(this);
        igracUCekaonici.clear();
        waitingPlayersList.getChildren().clear();
        sakrijSveSlojeve();
        layerMainMenu.setVisible(true);
    }
@FXML
    void handleBullshit(ActionEvent event) {
        if (!bullshitAktivan) return;
        bullshitAktivan = false;
        layerBullshitAction.setVisible(false);
        layerBullshitAction.setDisable(true);
        networkManager.posaljiBullshit(networkManager.getMyPlayerID(), poslednjiBacioID);
    }
    public void pokaziTekstIPileNaStolu(String poruka, int brojKarata, int bacioID) {
        this.poslednjiBacioID = bacioID;
        bullshitStatusLabel.setText(poruka);
        bullshitCardCountLabel.setText(String.valueOf(brojKarata));
        bullshitCardCountLabel.setVisible(true);
        layerBullshitAction.setVisible(false);
        this.bullshitAktivan = false;
    }

    public void upalisamoDugmeBullshit(int bacioID) {
        if (bacioID != networkManager.getMyPlayerID()) {
            this.bullshitAktivan = true;
            layerBullshitAction.setDisable(false);
            layerBullshitAction.setVisible(true);
        }
    }
    public void pokaziStoSaBullshitom(String poruka, int brojKarata, int bacioID) {
        this.poslednjiBacioID = bacioID;
        bullshitStatusLabel.setText(poruka);
        bullshitCardCountLabel.setText(String.valueOf(brojKarata));
        bullshitCardCountLabel.setVisible(true);
        // Bullshit dugme prikazuj SAMO igracima koji NISU bacili karte
        if (bacioID != networkManager.getMyPlayerID()) {
            this.bullshitAktivan = true;
            layerBullshitAction.setVisible(true);
        } else {
            this.bullshitAktivan = false;
            layerBullshitAction.setVisible(false);
        }
    }

    public void postaviImenaIgraca(String imeP2, String imeP3, String imeP4) {
        labelPlayer2.setText(imeP2);
        labelPlayer3.setText(imeP3);
        labelPlayer4.setText(imeP4);
    }

    public void aktivirajKrajIgre(String imePobednika) {
        sakrijSveSlojeve();
        winnerLabel.setText(imePobednika + " je uspešno izbacio sve karte i pobedio u ovoj partiji!");
        layerEndScreen.setVisible(true);
    }

    public void pokreniTajmer(int playerID, double layoutX, double layoutY) {
        if (timerTimeline != null) timerTimeline.stop();

        timerCirclePane.setLayoutX(layoutX);
        timerCirclePane.setLayoutY(layoutY - 55);
        timerCirclePane.setVisible(true);

        final int[] sekunde = {15};
        crtajTajmer(15);

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            sekunde[0]--;
            timerLabel.setText(String.valueOf(sekunde[0]));
            crtajTajmer(sekunde[0]);

            if (sekunde[0] <= 0) {
                timerTimeline.stop();
                timerCirclePane.setVisible(false);
            }
        }));
        timerTimeline.setCycleCount(15);
        timerTimeline.play();
    }

    private void crtajTajmer(int sekunde) {
        GraphicsContext gc = timerCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 50, 50);

        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(4);
        gc.strokeOval(5, 5, 40, 40);

        gc.setStroke(Color.web("#00ff00"));
        gc.setLineWidth(4);
        double ugao = (sekunde / 15.0) * 360.0;
        gc.strokeArc(5, 5, 40, 40, 90, ugao, javafx.scene.shape.ArcType.OPEN);
    }

    public void pokreniTajmerZaIgraca(int playerID) {
        double x, y;
        int vizPoz = vizuelnaPozicija(playerID);
        switch (vizPoz) {
            case 0: x = 340;  y = 515; break; // ja - dole
            case 1: x = 50;   y = 130; break; // levo
            case 2: x = 540;  y = 5;   break; // gore
            case 3: x = 1110; y = 130; break; // desno
            default: return;
        }
        pokreniTajmer(playerID, x, y);
    }

    public void prikaziBullshitOblacic(int tuzilacID, int optuzeniID) {
        liarBubbleP2.setVisible(false);
        liarBubbleP3.setVisible(false);
        liarBubbleP4.setVisible(false);
        liarBubbleMe.setVisible(false);
        liarTailP2.setVisible(false);
        liarTailP3.setVisible(false);
        liarTailP4.setVisible(false);
        liarTailMe.setVisible(false);

        VBox targetBubble;
        Label targetTail;
        int vizPoz = vizuelnaPozicija(tuzilacID);
        switch (vizPoz) {
            case 0: targetBubble = liarBubbleMe; targetTail = liarTailMe; break;
            case 1: targetBubble = liarBubbleP2; targetTail = liarTailP2; break;
            case 2: targetBubble = liarBubbleP3; targetTail = liarTailP3; break;
            case 3: targetBubble = liarBubbleP4; targetTail = liarTailP4; break;
            default: return;
        }


        // Dinamički tekst posred ekrana
        liarCenterLabel.setText(getImeIgraca(tuzilacID) + " optužuje " + getImeIgraca(optuzeniID) + " za laž!");
        liarCenterLabel.setVisible(true);

        layerLiarBubble.setVisible(true);
        targetBubble.setVisible(true);
        targetTail.setVisible(true);

        Timeline hideTimeline = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            targetBubble.setVisible(false);
            targetTail.setVisible(false);
            layerLiarBubble.setVisible(false);
            liarCenterLabel.setVisible(false);
        }));
        hideTimeline.setCycleCount(1);
        hideTimeline.play();
    }

    // === NOVE METODE ZA MREŽNI SLOJ ===

    // NOVO (ispravno):
    public void pokreniIgru() {
        List<String> imenaIgraca = new ArrayList<>(igracUCekaonici); // sačuvaj pre clear!

        sakrijSveSlojeve();
        layerGameTable.setVisible(true);
        waitingPlayersList.getChildren().clear();

        // Imena postavljamo SAMO za hosta ovde (gosti dobijaju POSTAVI_IMENA pre IGRA_KRENI)
// Za hosta: myPlayerID=1, pa je on uvek get(0)
        if (networkManager.isHost()) {
            int mojID = networkManager.getMyPlayerID(); // uvek 1 za hosta
            for (int offset = 0; offset < imenaIgraca.size(); offset++) {
                int idx = ((mojID - 1 + offset) % imenaIgraca.size());
                String ime = imenaIgraca.get(idx);
                switch (offset) {
                    case 0: labelMyName.setText(ime); break;
                    case 1: labelPlayer2.setText(ime); break;
                    case 2: labelPlayer3.setText(ime); break;
                    case 3: labelPlayer4.setText(ime); break;
                }
            }
            // Inicijalizuj labele za broj karata
            azurirajBrojKarata(1, 13);
            azurirajBrojKarata(2, 13);
            azurirajBrojKarata(3, 13);
            azurirajBrojKarata(4, 13);
        }
// Gosti NE postavljaju imena ovde — dobijaju ih kroz POSTAVI_IMENA

        cardManager = new CardManager();
        cardManager.popuniMojuRuku(myHand);
        cardManager.popuniRukuIgraca2(player2Hand);
        cardManager.popuniRukuIgraca3(player3Hand);
        cardManager.popuniRukuIgraca4(player4Hand);

        logika = new GlavnaLogika();
        for (int i = 0; i < imenaIgraca.size(); i++) {
            logika.dodajIgraca(new Igrac(imenaIgraca.get(i), i));
        }
        Deck deck = new Deck();
        deck.izmesaj();
        logika.startIgre(deck);
        logika.setListener(gameListener);

        igracUCekaonici.clear(); // briši tek na kraju!

        // Host šalje TVOJ_POTEZ prvom igraču
        if (networkManager.isHost()) {
            networkManager.resetujBrojKarata();
            networkManager.posaljiTvojPotez(1, logika.getTrenutniRang().name(), 1, 4);
        }
    }

    public void postaviMojeKarte(String karteStr) {
        myHand.getChildren().clear();
        String[] kartice = karteStr.split(",");
        for (String kartica : kartice) {
            // kartica je npr. "KRALJ_PIK"
            String[] delovi = kartica.split("_", 2);
            if (delovi.length < 2) continue;
            try {
                Rank rank = Rank.valueOf(delovi[0]);
                Suit suit = Suit.valueOf(delovi[1]);
                Card card = new Card(suit, rank);
                postaviKlikNaKartu(card);
                myHand.getChildren().add(card);
            } catch (Exception e) {
                System.out.println("Greška pri parsiranju karte: " + kartica);
            }
        }
    }

    private void postaviKlikNaKartu(Card karta) {
        karta.setOnMouseClicked(e -> {
            if (!karta.isOznacena()) {
                karta.setTranslateY(-20);
                karta.setOznacena(true);
            } else {
                karta.setTranslateY(0);
                karta.setOznacena(false);
            }
        });
    }

    public void postaviPoledjineOstalih() {
        // Ovo je isti kod kao pre, samo za igrače 2, 3, 4
        // (poleđine jer ne znamo njihove karte)
        if (cardManager == null) cardManager = new CardManager();
        cardManager.popuniRukuIgraca2(player2Hand);
        cardManager.popuniRukuIgraca3(player3Hand);
        cardManager.popuniRukuIgraca4(player4Hand);
    }

    public void prikaziPocetniDeck() {
        cardManager = new CardManager();
        igracUCekaonici.clear();
        waitingPlayersList.getChildren().clear();
        sakrijSveSlojeve();
        layerGameTable.setVisible(true);

        // Ruke igraca su prazne - animator/logicar ce ih popuniti
        myHand.getChildren().clear();
        player2Hand.getChildren().clear();
        player3Hand.getChildren().clear();
        player4Hand.getChildren().clear();

        // Pun spil na sredini stola
        // Pun spil na sredini stola - sve karte naslagane jedna na drugu
        mainCardPile.getChildren().clear();
        StackPane deckStack = new StackPane();

        for (int i = 0; i < 52; i++) {
            javafx.scene.image.ImageView cardView = new javafx.scene.image.ImageView(
                    new javafx.scene.image.Image(getClass().getResourceAsStream("card_back.png"))
            );
            cardView.setFitWidth(90);
            cardView.setFitHeight(130);
            deckStack.getChildren().add(cardView);
        }
        mainCardPile.getChildren().add(deckStack);
    }

    public void obavestitiOPotezu(int playerID, String rank, int min, int max) {
        this.trenutnoTrazeniRank = rank;
        boolean jaIgram = (playerID == networkManager.getMyPlayerID());
        layerMyTurn.setVisible(jaIgram);
        if (jaIgram && labelTrazeniRang != null) {
            labelTrazeniRang.setText("Baci: " + formatujRank(rank));
        }
    }

    public void prikazujeSobaPunaGresku() {
        sakrijSveSlojeve();
        layerFullRoomError.setVisible(true);
    }

    public void prikazujeGreskuKonekcije() {
        sakrijSveSlojeve();
        layerFullRoomError.setVisible(true);
    }

    public String getImeIgraca(int playerID) {
        int vizPoz = networkManager != null ? vizuelnaPozicija(playerID) : (playerID - 1);
        switch (vizPoz) {
            case 0: return labelMyName.getText();
            case 1: return labelPlayer2.getText();
            case 2: return labelPlayer3.getText();
            case 3: return labelPlayer4.getText();
            default: return "Igrač " + playerID;
        }
    }

    public String formatujRank(String rank) {
        switch (rank.toUpperCase()) {
            case "AS":
                return "Aseva";
            case "DVOJKA":
                return "Dvojki";
            case "TROJKA":
                return "Trojki";
            case "CETVORKA":
                return "Četvorki";
            case "PETICA":
                return "Petica";
            case "SESTICA":
                return "Šestica";
            case "SEDMICA":
                return "Sedmica";
            case "OSMICA":
                return "Osmica";
            case "DEVETKA":
                return "Devetki";
            case "DESETKA":
                return "Desetki";
            case "DECKO":
                return "Dečkova";
            case "DAMA":
                return "Dama";
            case "KRALJ":
                return "Kraljeva";
            default:
                return rank;
        }
    }

    public void igracNapustioLobby() {
        networkManager.shutdown();
        networkManager = new NetworkManager(this);
        igracUCekaonici.clear();
        waitingPlayersList.getChildren().clear();
        sakrijSveSlojeve();
        layerMainMenu.setVisible(true);
    }

    public void ukloniIgracaIzLobbyja(int playerID) {
        if (playerID <= igracUCekaonici.size()) {
            igracUCekaonici.remove(playerID - 1);
        }
        waitingPlayersList.getChildren().clear();
        for (String ime : igracUCekaonici) {
            Label playerLabel = new Label("▶  " + ime);
            playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 4 0 4 0;");
            waitingPlayersList.getChildren().add(playerLabel);
        }
        labelWaitingStatus.setText("Igrači u sobi: " + igracUCekaonici.size() + "/4");
    }

    private final GlavnaLogika.GameLogicListener gameListener = new GlavnaLogika.GameLogicListener() {

        @Override
        public void onPotezOdigran(Igrac igrac, List<Card> karte, Rank rang, Igrac sledeci) {
            networkManager.posaljiBaciKartu(igrac.getIndeks() + 1, karte.size(), rang.name());
        }

        @Override
        public void onNedozvoljenPotez(String razlog) {
            System.out.println("Nedozvoljen potez: " + razlog);
        }

        @Override
        public void onBullshitRezultat(Igrac lagovac, Igrac vikac, boolean jeLagao, Igrac kaznjeni, List<Card> karte) {
            String ishod = jeLagao ? "LAZOV" : "ISKREN";
            // Napravi putanje slika od stvarnih karata
            StringBuilder sb = new StringBuilder();
            for (Card c : karte) {
                if (sb.length() > 0) sb.append(",");
                sb.append(c.toString()); // npr. "KRALJ_PIK" → metoda napraviPutanju postoji u Card
            }
            networkManager.posaljiShowdownRezultat(ishod, kaznjeni.getIndeks() + 1, sb.toString());
        }

        @Override
        public void onIgracIzgubioZivot(Igrac igrac, int preostalih) {
            networkManager.posaljiAzuriranjeZivota(igrac.getIndeks() + 1, preostalih);
            updatePlayerLives(igrac.getIndeks() + 1, preostalih);
        }

        @Override
        public void onIgracIspao(Igrac igrac) {
            System.out.println(igrac.getIme() + " ispao.");
        }

        @Override
        public void onKrajIgre(Igrac pobednik) {
            networkManager.posaljiKrajIgre(pobednik.getIme());
            aktivirajKrajIgre(pobednik.getIme());
        }

        @Override
        public void onNovRang(Rank rang) {
            trenutnoTrazeniRank = rang.name();
        }
    };
    public void posaljiSledeciPotez(int sledeciPlayerID) {
        // logika već ima ažuriran rang nakon bacanja
        String rank = networkManager.getTrenutniRangNaMrezi();
        networkManager.posaljiTvojPotez(sledeciPlayerID, rank, 1, 4);
    }
    public String getTrenutniRang() {
        return trenutnoTrazeniRank;
    }

    public void postaviImenaIzPerspektive(String moje, String p2, String p3, String p4) {
        if (!moje.isEmpty()) labelMyName.setText(moje);
        if (!p2.isEmpty()) labelPlayer2.setText(p2);
        if (!p3.isEmpty()) labelPlayer3.setText(p3);
        if (!p4.isEmpty()) labelPlayer4.setText(p4);
    }
    public void sakrijBullshitPanel() {
        bullshitAktivan = false;
        layerBullshitAction.setVisible(false);
        bullshitCardCountLabel.setVisible(false);
        bullshitStatusLabel.setText("");
    }
    public void azurirajBrojKarata(int playerID, int brojKarata) {
        String tekst = "🂠 " + brojKarata + " karata";
        int vizPoz = networkManager != null ? vizuelnaPozicija(playerID) : (playerID - 1);
        switch (vizPoz) {
            case 0: if (labelBrojKarataMe != null) labelBrojKarataMe.setText(tekst); break;
            case 1: if (labelBrojKarataP2 != null) labelBrojKarataP2.setText(tekst); break;
            case 2: if (labelBrojKarataP3 != null) labelBrojKarataP3.setText(tekst); break;
            case 3: if (labelBrojKarataP4 != null) labelBrojKarataP4.setText(tekst); break;
        }
        // Ažuriraj i vizuelne poledjine (ne za sebe)
        if (networkManager != null && playerID != networkManager.getMyPlayerID()) {
            azurirajPoledjineIgraca(playerID, brojKarata);
        }
    }
    // Vraća vizuelnu poziciju igrača iz perspektive lokalnog igrača:
// 0 = ja (dole), 1 = levo, 2 = gore, 3 = desno
    private int vizuelnaPozicija(int apsolutniID) {
        int myID = networkManager.getMyPlayerID(); // 1-based
        return ((apsolutniID - myID + 4) % 4);
    }
    public void azurirajPoledjineIgraca(int playerID, int noviBroj) {
        int vizPoz = vizuelnaPozicija(playerID);
        switch (vizPoz) {
            case 1: // levo — VBox player2Hand
                player2Hand.getChildren().clear();
                for (int i = 0; i < noviBroj; i++) {
                    ImageView p = new ImageView(new Image(getClass().getResourceAsStream("card_back.png")));
                    p.setFitWidth(70); p.setFitHeight(100); p.setPreserveRatio(true);
                    p.setRotate(90);
                    player2Hand.getChildren().add(p);
                }
                break;
            case 2: // gore — HBox player3Hand
                player3Hand.getChildren().clear();
                player3Hand.setSpacing(-45.0);
                player3Hand.setAlignment(javafx.geometry.Pos.CENTER);
                for (int i = 0; i < noviBroj; i++) {
                    ImageView p = new ImageView(new Image(getClass().getResourceAsStream("card_back.png")));
                    p.setFitWidth(85); p.setFitHeight(120); p.setPreserveRatio(true);
                    p.setRotate(180);
                    player3Hand.getChildren().add(p);
                }
                break;
            case 3: // desno — VBox player4Hand
                player4Hand.getChildren().clear();
                for (int i = 0; i < noviBroj; i++) {
                    ImageView p = new ImageView(new Image(getClass().getResourceAsStream("card_back.png")));
                    p.setFitWidth(70); p.setFitHeight(100); p.setPreserveRatio(true);
                    p.setRotate(270);
                    player4Hand.getChildren().add(p);
                }
                break;
            case 0: // ja — ne diraj, myHand se ažurira kroz cardManager
                break;
        }
    }
    public void pokrniShowdownSaOdlaganjeemPoteza(int indeksVikaca) {
        // Zaustavi tajmer tokom showdowna
        if (timerTimeline != null) timerTimeline.stop();
        timerCirclePane.setVisible(false);

        // Pokreni bullshit logiku — ona okida onBullshitRezultat → posaljiShowdownRezultat → aktivirajShowdown
        logika.vikniBS(indeksVikaca);

        // Nakon 6 sekundi: sakrij showdown i posalji sledeci potez
        Timeline showdownTimer = new Timeline(new KeyFrame(Duration.seconds(6), e -> {
            layerShowdown.setVisible(false);
            // Azuriraj broj karata kaznjenog igraca na hostu
            // (logika je vec dodala karte kaznjenom, ali NM ne zna novi broj)
            // Posalji sledeciPotez — host zna ko je sledeci iz logike
            int sledeciIndeks = logika.getTrenutniIndeks(); // 0-based
            int sledeciID = sledeciIndeks + 1; // 1-based
            // Azuriraj brojKarataIgraca[] za kaznjenog
            for (int i = 0; i < 4; i++) {
                Igrac ig = logika.getIgrac(i);
                networkManager.getBrojKarataIgraca()[i] = ig.getBrojKarata();
                // Broadcast azuriranja karata za sve
                networkManager.posaljiAzuriranjeBrojaKarata(i + 1, ig.getBrojKarata());
                azurirajBrojKarata(i + 1, ig.getBrojKarata());
            }


// NOVO:
            String noviRang = networkManager.getTrenutniRangNaMrezi();
            networkManager.posaljiTvojPotez(sledeciID, noviRang, 1, 4);
        }));
        showdownTimer.setCycleCount(1);
        showdownTimer.play();
    }
    public GlavnaLogika getLogika() {
        return logika;
    }

    public void azurirajTrenutniRangNaLogici(String rankStr) {
        try {
            Rank rank = Rank.valueOf(rankStr);
// trenutniRang u logici se azurira kroz prebaciNaSledeceg
// ali za gomilu nam treba samo da znamo koji rang je prijavljen
            trenutnoTrazeniRank = rankStr;
        } catch (Exception e) {
            System.out.println("Greška pri parsiranju ranka: " + rankStr);
        }
    }
}