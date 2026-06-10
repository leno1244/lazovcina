package com.example.lazovcina;

public final class Protocol {

    private Protocol() {}

    public static final String SEP = ":";

    public static final String HELLO           = "HELLO";
    public static final String LOBBY_UPDATE    = "LOBBY_UPDATE";
    public static final String SOBA_PUNA       = "SOBA_PUNA";
    public static final String IGRA_KRENI      = "IGRA_KRENI";
    public static final String TVOJ_POTEZ      = "TVOJ_POTEZ";
    public static final String BACI_KARTU      = "BACI_KARTU";
    public static final String KARTE_BACENE    = "KARTE_BACENE";
    public static final String PROGLASI_BULLSHIT = "PROGLASI_BULLSHIT";
    public static final String SHOWDOWN_REZULTAT = "SHOWDOWN_REZULTAT";
    public static final String AZURIRAJ_ZIVOTE = "AZURIRAJ_ZIVOTE";
    public static final String TAJMER_START    = "TAJMER_START";
    public static final String TAJMER_ISTEKAO  = "TAJMER_ISTEKAO";
    public static final String KRAJ_IGRE       = "KRAJ_IGRE";
    public static final String IGRAC_NAPUSTIO  = "IGRAC_NAPUSTIO";
    public static final String SERVER_BROADCAST = "BULLSHIT_SERVER";
    public static final String TVOJ_ID         = "TVOJ_ID";
    public static final String TVOJE_KARTE     = "TVOJE_KARTE";
    public static final String POSTAVI_IMENA   = "POSTAVI_IMENA";
    public static final String AZURIRAJ_KARTE  = "AZURIRAJ_KARTE";

    public static final int UDP_DISCOVERY_PORT = 45678;
    public static final int GAME_PORT          = 45679;

    public static String build(String... parts) {
        return String.join(SEP, parts) + "\n";
    }

    public static String[] parse(String line) {
        return line.trim().split(SEP);
    }
}
