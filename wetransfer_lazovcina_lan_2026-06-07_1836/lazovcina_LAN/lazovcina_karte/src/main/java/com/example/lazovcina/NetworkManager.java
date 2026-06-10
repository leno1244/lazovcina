
    package com.example.lazovcina;

import javafx.application.Platform;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
    public class NetworkManager {

        private final HelloController controller;

        private GameServer server;
        private GameClient client;

        private boolean isHost = false;
        private int myPlayerID = 1;
        private int[] brojKarataIgraca = {13, 13, 13, 13}; // P1, P2, P3, P4
        public NetworkManager(HelloController controller) {
            this.controller = controller;
        }
        private int trenutniRangIndeks = 0; // 0 = AS ... 12 = KRALJ
        // ── POKRETANJE ──────────────────────────────────────────────────────────────

        public void startAsHost(String serverName, String hostUsername) {
            isHost = true;
            myPlayerID = 1;
            server = new GameServer(serverName, hostUsername, this);
            server.start();
            Platform.runLater(() -> controller.azurirajCekaonicu(hostUsername, 1, 4));
        }

        public void startAsGuest(String username, String adresa) {
            isHost = false;
            client = new GameClient(username, this);

            String host = adresa;
            int finalPort;
            if (adresa.contains(":")) {
                host = adresa.split(":")[0];
                int parsedPort;
                try {
                    parsedPort = Integer.parseInt(adresa.split(":")[1]);
                } catch (NumberFormatException e) {
                    parsedPort = Protocol.GAME_PORT;
                }
                finalPort = parsedPort;
            } else {
                finalPort = Protocol.GAME_PORT;
            }

            final String finalHost = host;
            client.connect(
                    finalHost,
                    finalPort,
                    () -> System.out.println("[NM] Gost uspešno konektovan na " + finalHost + ":" + finalPort),
                    error -> controller.prikazujeGreskuKonekcije()
            );
        }

        public void skenirajSobe(String targetIp, int timeoutMs) {
            GameClient scanner = new GameClient("", this);
            scanner.skeniranjeSoba(targetIp, timeoutMs, sobaInfo -> {
                String serverName = sobaInfo[0];
                int trenutno      = Integer.parseInt(sobaInfo[1]);
                int max           = Integer.parseInt(sobaInfo[2]);
                String ip         = sobaInfo[3];
                controller.dodajSobuUListu(serverName + " (" + ip + ")", trenutno, max);
            });
        }
        public void posaljiKarteIgracu(int playerID, String karteStr) {
            String poruka = Protocol.build(Protocol.TVOJE_KARTE,
                    String.valueOf(playerID), karteStr);
            if (isHost) {
                server.posaljiIgracu(playerID, poruka);
            }
        }

        public void resetujBrojKarata() {
            brojKarataIgraca = new int[]{13, 13, 13, 13};
            trenutniRangIndeks = 0;
        }

        public int[] getBrojKarataIgraca() {
            return brojKarataIgraca;
        }
        private boolean bullshitUToku = false;
        // ── PRIMANJE PORUKA ─────────────────────────────────────────────────────────

        public void onPrimljenaPoruka(String rawLine, int senderID) {
            String[] parts = Protocol.parse(rawLine);
            if (parts.length == 0) return;
            String tip = parts[0];

            //menja se ceo switch u network manager-u
            switch (tip) {
                case Protocol.BACI_KARTU:
                    if (parts.length < 4) break;
                    if (isHost) {
                        int trenutniID = Integer.parseInt(parts[1]);
                        int bacenoBroj = Integer.parseInt(parts[2]);
                        String baceniRankStr = parts[3];
                        brojKarataIgraca[trenutniID - 1] -= bacenoBroj;

// Dodaj karte u GlavnaLogika gomilu
                        Rank prijavljeniRank = Rank.valueOf(baceniRankStr);
                        List<Card> lazneKarte = new ArrayList<>();
                        for (int i = 0; i < bacenoBroj; i++) {
                            lazneKarte.add(new Card(Suit.PIK, prijavljeniRank));
                        }
                        int indeksIgraca = trenutniID - 1;
                        controller.getLogika().getGomila().dodajBacanje(lazneKarte, prijavljeniRank, indeksIgraca);
                        controller.getLogika().getTrenutniIndeks();
// Azuriraj trenutni rang u logici
                        Platform.runLater(() -> controller.azurirajTrenutniRangNaLogici(baceniRankStr));

                        String kbMsg = Protocol.build(Protocol.KARTE_BACENE, parts[1], parts[2], parts[3]);
                        server.broadcastSvima(kbMsg);
                        posaljiPoruku(Protocol.build(Protocol.AZURIRAJ_KARTE,
                                String.valueOf(trenutniID),
                                String.valueOf(brojKarataIgraca[trenutniID - 1])));

                        if (brojKarataIgraca[trenutniID - 1] <= 0) {
                            final String imePobednika = controller.getImeIgraca(trenutniID);
                            posaljiKrajIgre(imePobednika);
                            Platform.runLater(() -> controller.aktivirajKrajIgre(imePobednika));
                            break;
                        }

                        Rank[] sviRangovi = Rank.values();
                        trenutniRangIndeks = (trenutniRangIndeks + 1) % sviRangovi.length;
                        String noviRang = sviRangovi[trenutniRangIndeks].name();
                        int sledeciID = (trenutniID % 4) + 1;
                        server.broadcastSvima(Protocol.build(Protocol.TVOJ_POTEZ,
                                String.valueOf(sledeciID), noviRang, "1", "4"));
                    }
                    break;

                case Protocol.TVOJE_KARTE:
                    if (parts.length < 3) break;
                    final int targetID = Integer.parseInt(parts[1]);
                    final String karteStr = parts[2];
                    if (targetID == myPlayerID) {
                        Platform.runLater(() -> {
                            controller.postaviMojeKarte(karteStr);
                            controller.postaviPoledjineOstalih();
                        });
                    }
                    break;

                case Protocol.TVOJ_ID:
                    if (parts.length < 2) break;
                    myPlayerID = Integer.parseInt(parts[1]);
                    System.out.println("[NM] Moj playerID je: " + myPlayerID);
                    break;

                case Protocol.LOBBY_UPDATE:
                    if (parts.length < 4) break;
                    final String lobbyIgrac = parts[1];
                    final int lobbyTrenutno = Integer.parseInt(parts[2]);
                    final int lobbyMax = Integer.parseInt(parts[3]);
                    Platform.runLater(() -> controller.azurirajCekaonicu(lobbyIgrac, lobbyTrenutno, lobbyMax));
                    break;

                case Protocol.SOBA_PUNA:
                    onSobaPuna();
                    break;

                case Protocol.IGRA_KRENI:
                    Platform.runLater(() -> controller.pokreniIgru());
                    break;

                case Protocol.TVOJ_POTEZ:
                    if (parts.length < 5) break;
                    final int potezID = Integer.parseInt(parts[1]);
                    final String potezRank = parts[2];
                    final int potezMin = Integer.parseInt(parts[3]);
                    final int potezMax = Integer.parseInt(parts[4]);
                    Platform.runLater(() -> {
                        controller.obavestitiOPotezu(potezID, potezRank, potezMin, potezMax);
                        controller.pokreniTajmerZaIgraca(potezID);
                    });
                    break;

                case Protocol.KARTE_BACENE:
                    if (parts.length < 4) break;
                    final int kbPlayerID = Integer.parseInt(parts[1]);
                    final int kbBrojKarata = Integer.parseInt(parts[2]);
                    final String kbRank = parts[3];
                    Platform.runLater(() -> {
                        String imeIgraca = controller.getImeIgraca(kbPlayerID);
                        String statusMsg = imeIgraca + " je bacio " + kbBrojKarata + "x " + controller.formatujRank(kbRank) + "!";
                        controller.sakrijBullshitPanel();
                        controller.pokaziStoSaBullshitom(statusMsg, kbBrojKarata, kbPlayerID);
                        controller.prikaziBaceneKarteNaStolu(kbBrojKarata);
                    });
                    break;

                case Protocol.SHOWDOWN_REZULTAT:
                    if (parts.length < 4) break;
                    final String ishod = parts[1];
                    final int krivcID = Integer.parseInt(parts[2]);
                    final String[] slike = parts[3].split(",");
                    final String pokaziPoruka = "LAZOV".equals(ishod)
                            ? "Tužilac je bio u pravu! Igrač " + krivcID + " je varao i dobija sve karte!"
                            : "Igrač " + krivcID + " je bio iskren! Tužilac dobija sve karte!";
                    Platform.runLater(() -> controller.aktivirajShowdown(pokaziPoruka, Arrays.asList(slike)));
                    break;

                case Protocol.AZURIRAJ_ZIVOTE:
                    if (parts.length < 3) break;
                    final int zivotiID = Integer.parseInt(parts[1]);
                    final int zivotiBreoj = Integer.parseInt(parts[2]);
                    Platform.runLater(() -> controller.updatePlayerLives(zivotiID, zivotiBreoj));
                    break;

                case Protocol.TAJMER_START:
                    if (parts.length < 2) break;
                    final int tajmerID = Integer.parseInt(parts[1]);
                    Platform.runLater(() -> controller.pokreniTajmerZaIgraca(tajmerID));
                    break;

                case Protocol.KRAJ_IGRE:
                    if (parts.length < 2) break;
                    final String pobednik = parts[1];
                    Platform.runLater(() -> controller.aktivirajKrajIgre(pobednik));
                    break;

                case Protocol.IGRAC_NAPUSTIO:
                    if (parts.length < 2) break;
                    int napustioID = Integer.parseInt(parts[1]);
                    System.out.println("[NM] Igrač P" + napustioID + " je napustio igru.");
                    Platform.runLater(() -> controller.igracNapustioLobby());
                    break;

                case Protocol.PROGLASI_BULLSHIT:
                    if (parts.length < 3) break;
                    final int tuzilacID = Integer.parseInt(parts[1]);
                    final int optuzeniID = Integer.parseInt(parts[2]);
                    final int indeksVikaca = tuzilacID - 1;
                    Platform.runLater(() -> {
                        controller.prikaziBullshitOblacic(tuzilacID, optuzeniID);
                        if (isHost) {
                            controller.pokrniShowdownSaOdlaganjeemPoteza(indeksVikaca);
                        }
                    });
                    break;

                case Protocol.POSTAVI_IMENA:
                    if (parts.length < 2) break;
                    final String[] imena = parts[1].split(",");
                    Platform.runLater(() -> controller.postaviImenaIzPerspektive(
                            imena.length > 0 ? imena[0] : "",
                            imena.length > 1 ? imena[1] : "",
                            imena.length > 2 ? imena[2] : "",
                            imena.length > 3 ? imena[3] : ""
                    ));
                    break;

                case Protocol.AZURIRAJ_KARTE:
                    if (parts.length < 3) break;
                    final int azID = Integer.parseInt(parts[1]);
                    final int azBroj = Integer.parseInt(parts[2]);
                    Platform.runLater(() -> controller.azurirajBrojKarata(azID, azBroj));
                    break;

                default:
                    System.out.println("[NM] Nepoznata poruka: " + rawLine);
            }

        }

        public void onSobaPuna() {
            Platform.runLater(controller::prikazujeSobaPunaGresku);
        }

        // ── SLANJE PORUKA ───────────────────────────────────────────────────────────

        public void posaljiPoruku(String poruka) {
            if (isHost && server != null) {
                server.broadcastSvima(poruka);
            } else if (!isHost && client != null) {
                client.posalji(poruka);
            }
        }
        public void posaljiAzuriranjeBrojaKarata(int playerID, int brojKarata) {
            posaljiPoruku(Protocol.build(Protocol.AZURIRAJ_KARTE,
                    String.valueOf(playerID), String.valueOf(brojKarata)));
        }
        public void posaljiBaciKartu(int playerID, int brojKarata, String rank) {
            posaljiPoruku(Protocol.build(Protocol.BACI_KARTU, String.valueOf(playerID), String.valueOf(brojKarata), rank));
        }

        public void posaljiBullshit(int tuzilacID, int optuzeniID) {
            posaljiPoruku(Protocol.build(Protocol.PROGLASI_BULLSHIT, String.valueOf(tuzilacID), String.valueOf(optuzeniID)));
        }
        public void posaljiShowdownRezultat(String ishod, int playerID, String karteSlike) {
            if (!isHost) return;
            posaljiPoruku(Protocol.build(Protocol.SHOWDOWN_REZULTAT, ishod, String.valueOf(playerID), karteSlike));
        }

        public void posaljiTvojPotez(int playerID, String rank, int min, int max) {
            if (!isHost) return;
            posaljiPoruku(Protocol.build(Protocol.TVOJ_POTEZ, String.valueOf(playerID), rank, String.valueOf(min), String.valueOf(max)));
        }

        public void posaljiAzuriranjeZivota(int playerID, int zivoti) {
            if (!isHost) return;
            posaljiPoruku(Protocol.build(Protocol.AZURIRAJ_ZIVOTE, String.valueOf(playerID), String.valueOf(zivoti)));
        }

        public void posaljiKrajIgre(String pobednik) {
            if (!isHost) return;
            posaljiPoruku(Protocol.build(Protocol.KRAJ_IGRE, pobednik));
        }

        public void shutdown() {
            if (server != null) server.stop();
            if (client != null) client.disconnect();
        }

        public boolean isHost() { return isHost; }
        public int getMyPlayerID() { return myPlayerID; }
        public void setMyPlayerID(int id) { this.myPlayerID = id; }
        public String getTrenutniRangNaMrezi() {
            return Rank.values()[trenutniRangIndeks].name();
        }
    }


