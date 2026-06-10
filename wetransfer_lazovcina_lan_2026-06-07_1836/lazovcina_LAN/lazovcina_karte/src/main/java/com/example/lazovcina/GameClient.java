
    package com.example.lazovcina;

import javafx.application.Platform;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

    public class GameClient {

        private final String username;
        private final NetworkManager networkManager;

        private Socket socket;
        private PrintWriter writer;
        private volatile boolean running = false;
        private int myPlayerID = -1;

        public GameClient(String username, NetworkManager networkManager) {
            this.username       = username;
            this.networkManager = networkManager;
        }

        public void skeniranjeSoba(String targetIp, int timeoutMs, Consumer<String[]> callback) {
            Thread skenThread = new Thread(() -> {
                Set<String> pronadjeni = new HashSet<>();
                try (DatagramSocket udpSocket = new DatagramSocket()) {
                    udpSocket.setSoTimeout(timeoutMs);
                    udpSocket.setBroadcast(true);

                    byte[] buf = new byte[512];
                    long start = System.currentTimeMillis();

                    while (System.currentTimeMillis() - start < timeoutMs) {
                        try {
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            udpSocket.receive(packet);

                            String poruka = new String(packet.getData(), 0, packet.getLength()).trim();
                            String[] parts = Protocol.parse(poruka);

                            if (parts.length >= 4 && Protocol.SERVER_BROADCAST.equals(parts[0])) {
                                String serverName = parts[1];
                                String trenutno   = parts[2];
                                String max        = parts[3];
                                String serverIp   = packet.getAddress().getHostAddress();

                                String kljuc = serverIp + ":" + serverName;
                                if (!pronadjeni.contains(kljuc)) {
                                    pronadjeni.add(kljuc);
                                    final String[] info = {serverName, trenutno, max, serverIp};
                                    Platform.runLater(() -> callback.accept(info));
                                }
                            }
                        } catch (SocketTimeoutException ignored) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("[CLIENT-UDP] Greška pri skeniranju: " + e.getMessage());
                }
            }, "Client-UDP-Scan");

            skenThread.setDaemon(true);
            skenThread.start();
        }

        public void connect(String host, int port, Runnable onSuccess, Consumer<String> onFailure) {
            Thread connectThread = new Thread(() -> {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host, port), 5000);

                    writer = new PrintWriter(socket.getOutputStream(), true);
                    running = true;

                    Thread readerThread = new Thread(this::readLoop, "Client-Reader");
                    readerThread.setDaemon(true);
                    readerThread.start();

                    posalji(Protocol.build(Protocol.HELLO, username));
                    Platform.runLater(onSuccess);

                } catch (IOException e) {
                    System.err.println("[CLIENT] Konekcija neuspešna: " + e.getMessage());
                    Platform.runLater(() -> onFailure.accept(e.getMessage()));
                }
            }, "Client-Connect");

            connectThread.setDaemon(true);
            connectThread.start();
        }

        private void readLoop() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String poruka = line;
                    System.out.println("[CLIENT] Primljeno: " + poruka);

                    if (poruka.startsWith(Protocol.SOBA_PUNA)) {
                        Platform.runLater(() -> networkManager.onSobaPuna());
                        disconnect();
                        return;
                    }

                    Platform.runLater(() -> networkManager.onPrimljenaPoruka(poruka, myPlayerID));
                }
            } catch (IOException e) {
                if (running) System.err.println("[CLIENT] Veza prekinuta: " + e.getMessage());
            }
        }

        public void posalji(String poruka) {
            if (writer != null) {
                writer.print(poruka);
                writer.flush();
            }
        }

        public void disconnect() {
            running = false;
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.err.println("[CLIENT] Greška pri zatvaranju: " + e.getMessage());
            }
        }

        public void setMyPlayerID(int id) { this.myPlayerID = id; }
        public int getMyPlayerID() { return myPlayerID; }
        public boolean isConnected() { return running && socket != null && socket.isConnected(); }
    }

