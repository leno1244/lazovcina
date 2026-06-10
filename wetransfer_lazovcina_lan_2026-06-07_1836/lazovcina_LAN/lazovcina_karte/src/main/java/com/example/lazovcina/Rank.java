package com.example.lazovcina;

public enum Rank {
    AS(1), DVOJKA(2), TROJKA(3), CETVORKA(4), PETICA(5),
    SESTICA(6), SEDMICA(7), OSMICA(8), DEVETKA(9), DESETKA(10),
    DECKO(11), DAMA(12), KRALJ(13);

    private final int vrednost;

    Rank(int vrednost) {
        this.vrednost = vrednost;
    }

    public int getVrednost() {
        return vrednost;
    }
}