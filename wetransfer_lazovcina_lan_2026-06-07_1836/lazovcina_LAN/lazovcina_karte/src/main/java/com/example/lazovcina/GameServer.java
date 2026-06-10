
    package com.example.lazovcina;

import javafx.application.Platform;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

    public class GameServer {

        private static final int MAX_PLAYERS = 4;

        private final String serverName;
        private final String hostUsername;
        private final NetworkManager networkManager;

        private ServerSocket serverSocket;
        private final Map<Integer, PrintWriter> clients = new ConcurrentHashMap<>();
        private final Map<Integer, String> playerNames = new ConcurrentHashMap<>();
        private volatile boolean running = false;

        public GameServer(String serverName, String hostUsername, NetworkManager networkManager) {
            this.serverName    = serverName;
            this.hostUsername  = hostUsername;
            this.networkManager = networkManager;
            playerNames.put(1, hostUsername);
        }

        public void start() {
            running = true;

            Thread acceptThread = new Thread(this::acceptLoop, "Server-Accept-Thread");
            acceptThread.setDaemon(true);
            acceptThread.start();

            Thread udpThread = new Thread(this::udpBroadcastLoop, "Server-UDP-Broadcast");
            udpThread.setDaemon(true);
            udpThread.start();

            System.out.println("[SERVER] Pokrenut: " + serverName + " na portu " + Protocol.GAME_PORT);
        }

        private void acceptLoop() {
            try {
                serverSocket = new ServerSocket(Protocol.GAME_PORT, 50, InetAddress.getByName("0.0.0.0"));
                System.out.println("[SERVER] Slusam na svim interfejsima, port " + Protocol.GAME_PORT);

                while (running && clients.size() < MAX_PLAYERS - 1) {
                    Socket clientSocket = serverSocket.accept();

                    if (clients.size() >= MAX_PLAYERS - 1) {
                        PrintWriter tempWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                        tempWriter.print(Protocol.build(Protocol.SOBA_PUNA));
                        tempWriter.flush();
                        clientSocket.close();
                        continue;
                    }

                    int playerID = clients.size() + 2;
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    clients.put(playerID, writer);

                    Thread readerThread = new Thread(
                            () -> clientReadLoop(clientSocket, playerID),
                            "Server-Reader-P" + playerID
                    );
                    readerThread.setDaemon(true);
                    readerThread.start();

                    System.out.println("[SERVER] Klijent P" + playerID + " se spojio sa " + clientSocket.getInetAddress());
                }

            } catch (IOException e) {
                if (running) System.err.println("[SERVER] Greška u accept petlji: " + e.getMessage());
            }
        }

        private void clientReadLoop(Socket socket, int playerID) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String poruka = line;
                    System.out.println("[SERVER] Od P" + playerID + ": " + poruka);
                    String[] parts = Protocol.parse(poruka);
                    String tip = parts[0];

                    if (Protocol.HELLO.equals(tip) && parts.length >= 2) {
                        String username = parts[1];
                        playerNames.put(playerID, username);

                        // Pošalji novom igraču sve koji su već u sobi
                        PrintWriter newWriter = clients.get(playerID);
                        playerNames.entrySet().stream()
                                .filter(e -> e.getKey() != playerID)
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(e -> {
                                    String prevUpdate = Protocol.build(
                                            Protocol.LOBBY_UPDATE,
                                            e.getValue(),
                                            String.valueOf(e.getKey()),
                                            String.valueOf(MAX_PLAYERS)
                                    );
                                    newWriter.print(prevUpdate);
                                    newWriter.flush();
                                });

                        // Pošalji igraču njegov playerID
                        posaljiIgracu(playerID, Protocol.build(Protocol.TVOJ_ID, String.valueOf(playerID)));

                        // Obavesti sve o novom igraču
                        String updateMsg = Protocol.build(
                                Protocol.LOBBY_UPDATE,
                                username,
                                String.valueOf(playerNames.size()),
                                String.valueOf(MAX_PLAYERS)
                        );
                        broadcastSvima(updateMsg);

                        if (playerNames.size() == MAX_PLAYERS) {
                            for (int pid = 1; pid <= MAX_PLAYERS; pid++) {
                                StringBuilder sb = new StringBuilder();
                                for (int offset = 0; offset < MAX_PLAYERS; offset++) {
                                    int id = ((pid - 1 + offset) % MAX_PLAYERS) + 1;
                                    if (sb.length() > 0) sb.append(",");
                                    sb.append(playerNames.getOrDefault(id, "Igrac" + id));
                                }
                                posaljiIgracu(pid, Protocol.build(Protocol.POSTAVI_IMENA, sb.toString()));
                            }
                            broadcastSvima(Protocol.build(Protocol.IGRA_KRENI));
                        }
                    } else {
                        networkManager.onPrimljenaPoruka(poruka, playerID);
                    }
                }
            } catch (IOException e) {
                if (running) System.err.println("[SERVER] Klijent P" + playerID + " prekinuo vezu.");
            } finally {
                clients.remove(playerID);
                playerNames.remove(playerID);
                broadcastSvima(Protocol.build(Protocol.IGRAC_NAPUSTIO, String.valueOf(playerID)));
            }
        }

        private void udpBroadcastLoop() {
            try (DatagramSocket udpSocket = new DatagramSocket()) {
                udpSocket.setBroadcast(true);
                InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");

                while (running && playerNames.size() < MAX_PLAYERS) {
                    String msg = Protocol.build(
                            Protocol.SERVER_BROADCAST,
                            serverName,
                            String.valueOf(playerNames.size()),
                            String.valueOf(MAX_PLAYERS)
                    );
                    byte[] data = msg.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddr, Protocol.UDP_DISCOVERY_PORT);
                    udpSocket.send(packet);
                    Thread.sleep(2000);
                }
            } catch (IOException | InterruptedException e) {
                if (running) System.err.println("[SERVER-UDP] Greška: " + e.getMessage());
            }
        }

        public void broadcastSvima(String poruka) {
            for (PrintWriter writer : clients.values()) {
                writer.print(poruka);
                writer.flush();
            }
            Platform.runLater(() -> networkManager.onPrimljenaPoruka(poruka.trim(), 0));
        }

        public void posaljiIgracu(int playerID, String poruka) {
            if (playerID == 1) {
                Platform.runLater(() -> networkManager.onPrimljenaPoruka(poruka.trim(), 0));
                return;
            }
            PrintWriter writer = clients.get(playerID);
            if (writer != null) {
                writer.print(poruka);
                writer.flush();
            }
        }

        public void stop() {
            running = false;
            try {
                if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            } catch (IOException e) {
                System.err.println("[SERVER] Greška pri gašenju: " + e.getMessage());
            }
        }
        public Map<Integer, PrintWriter> getClients() {
            return clients;
        }
        public Map<Integer, String> getPlayerNames() { return Collections.unmodifiableMap(playerNames); }
        public boolean isRunning() { return running; }
    }

