package com.example.lazovcina;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║                     GLAVNA LOGIKA IGRE                          ║
 * ║                      "Bullshit / Lažov"                         ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * Ova klasa je JEDINA ISTINA o stanju igre.
 * Ona ne zna ništa o JavaFX, ImageView, HBox ili animacijama.
 * Komunicira sa ostatkom koda kroz callback interfejs {@link GameLogicListener}.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  REDOSLED POZIVANJA (tipični tok jednog poteza)                  │
 * │                                                                  │
 * │  1. GlavnaLogika.baciKarte(indeksiKarata, prijavljeniRang)       │
 * │       → proverava da li je potez validan                         │
 * │       → beleži stvarne karte                                     │
 * │       → okida onPotezOdigran()                                   │
 * │       → okida onSledecaRunda() i pomiče trenutniRang             │
 * │                                                                  │
 * │  2. (opciono) GlavnaLogika.vikniBS()                             │
 * │       → proverava blef                                           │
 * │       → određuje ko uzima gomilu                                 │
 * │       → okida onBullshitRezultat(...)                            │
 * │       → ako je neko izgubio život → onIgracIzgubioZivot(...)     │
 * │       → ako je neko ispao → onIgracIspao(...)                    │
 * │       → ako je igra gotova → onKrajIgre(...)                     │
 * └──────────────────────────────────────────────────────────────────┘
 */
public class GlavnaLogika {

    // ─────────────────────────────────────────────────────────────
    // Callback interfejs prema UI / CardManager / Animator
    // ─────────────────────────────────────────────────────────────

    /**
     * Implementira klasa koja želi da dobija obaveštenja o događajima u igri
     * (tipično: HelloController ili CardManager).
     */
    public interface GameLogicListener {

        /**
         * Poziva se kada igrač uspešno odigra potez (baci karte).
         *
         * @param igrac        ko je bacio
         * @param broseneKarte stvarne karte (UI može da ih sakrije)
         * @param prijavljeni  rang koji je objavio
         * @param sledeci      sledeći igrač na redu
         */
        void onPotezOdigran(Igrac igrac, List<Card> broseneKarte,
                            Rank prijavljeni, Igrac sledeci);

        /**
         * Poziva se kada igrač ne sme da preskoci red (skip nije dozvoljen
         * u osnovnoj igri, ali može se proširiti).
         */
        void onNedozvoljenPotez(String razlog);

        /**
         * Poziva se nakon provjere "BULLSHIT".
         *
         * @param lagovac   igrač koji je lagao (ili null ako nije lagao)
         * @param vikac     igrač koji je vikao BULLSHIT
         * @param jeliLagao true = lagovac uzima gomilu; false = vikač uzima
         * @param kaznjeni  igrač koji uzima sve karte sa stola
         * @param karte     karte koje kaznjeni uzima
         */
        void onBullshitRezultat(Igrac lagovac, Igrac vikac,
                                boolean jeliLagao, Igrac kaznjeni,
                                List<Card> karte);

        /**
         * Poziva se kada igrač izgubi jedan život.
         *
         * @param igrac      ko je izgubio život
         * @param preostalih koliko života mu je ostalo
         */
        void onIgracIzgubioZivot(Igrac igrac, int preostalih);

        /**
         * Poziva se kada igrač izgubi sve živote i ispada.
         */
        void onIgracIspao(Igrac igrac);

        /**
         * Poziva se kada igrač isprazni ruku (pobedi).
         *
         * @param pobednik ko je pobedio
         */
        void onKrajIgre(Igrac pobednik);

        /**
         * Poziva se da UI zna koji rang se trenutno traži.
         */
        void onNovRang(Rank rang);
    }

    // ─────────────────────────────────────────────────────────────
    // Stanje igre
    // ─────────────────────────────────────────────────────────────

    private final List<Igrac> igraci;
    private final GomilaNaStolu gomila;
    private int trenutniIndeks;        // Ko je na redu
    private Rank trenutniRang;         // Koji rang se trenutno traži
    private GameLogicListener listener;
    private boolean igracTokuje;       // Sprečava duple pozive

    // ─────────────────────────────────────────────────────────────
    // Konstruktor i inicijalizacija
    // ─────────────────────────────────────────────────────────────

    public GlavnaLogika() {
        this.igraci = new ArrayList<>();
        this.gomila = new GomilaNaStolu();
        this.trenutniIndeks = 0;
        this.trenutniRang = Rank.AS; // igra počinje od dvojki
    }

    public void setListener(GameLogicListener listener) {
        this.listener = listener;
    }

    /**
     * Dodaje igrača. Poziva se pri setup-u, pre startIgre().
     */
    public void dodajIgraca(Igrac igrac) {
        igraci.add(igrac);
    }

    /**
     * Pokretanje igre: deli karte i signalizira UI-u koji rang se traži.
     *
     * @param deck već izmješan špil karata (Deck.java ga pravi)
     */
    public void startIgre(Deck deck) {
        int ukupno = 52;
        int brIgraca = igraci.size();
        int poIgracu = ukupno / brIgraca;

        for (Igrac igrac : igraci) {
            igrac.dodajKarte(deck.podeli(poIgracu));
        }

        trenutniRang = Rank.AS;
        trenutniIndeks = 0;

        if (listener != null) {
            listener.onNovRang(trenutniRang);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Glavni potez: bacanje karata
    // ─────────────────────────────────────────────────────────────

    /**
     * Igrač na redu baca odabrane karte i OBJAVLJUJE rang.
     * <p>
     * Poziva UI kada je korisnik kliknuo "Baci" dugme.
     *
     * @param indeksiKarata   indeksi odabranih karata u listi ruke igrača (0-based)
     * @param prijavljeniRang rang koji igrač TVRDI da baca (može lagati)
     * @return true ako je potez prihvaćen, false ako nije
     */
    public boolean baciKarte(List<Integer> indeksiKarata, Rank prijavljeniRang) {
        if (indeksiKarata == null || indeksiKarata.isEmpty()) {
            fire(l -> l.onNedozvoljenPotez("Moraš odabrati barem jednu kartu."));
            return false;
        }

        Igrac trenutni = getTrenutniIgrac();

        // Proveravamo da su indeksi validni
        for (int idx : indeksiKarata) {
            if (idx < 0 || idx >= trenutni.getBrojKarata()) {
                fire(l -> l.onNedozvoljenPotez("Nevažeći indeks karte."));
                return false;
            }
        }

        // Uzimamo karte iz ruke
        List<Card> broseneKarte = trenutni.baciKarte(indeksiKarata);

        // Karte idu na gomilu (sa prijavljenim rangom)
        gomila.dodajBacanje(broseneKarte, prijavljeniRang, trenutniIndeks);

        // Da li je igrač isprazniо ruku → pobeda
        if (!trenutni.imaKarte()) {
            final Igrac pobednik = trenutni;
            fire(l -> l.onPotezOdigran(pobednik, broseneKarte, prijavljeniRang, null));
            fire(l -> l.onKrajIgre(pobednik));
            return true;
        }

        // Prelazak na sledećeg igrača i sledeći rang
        Igrac sledeci = prebaciNaSledeceg();
        final Igrac finalTrenutni = trenutni;
        final List<Card> finalKarte = broseneKarte;

        fire(l -> l.onPotezOdigran(finalTrenutni, finalKarte, prijavljeniRang, sledeci));

        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // BULLSHIT provjera
    // ─────────────────────────────────────────────────────────────

    /**
     * Igrač viče "BULLSHIT" na poslednji potez.
     * <p>
     * Poziva se kada korisnik klikne dugme "BULLSHIT".
     *
     * @param indeksVikaca indeks igrača koji viče (0-based)
     */
    public void vikniBS(int indeksVikaca) {
        if (gomila.isPrazna()) {
            fire(l -> l.onNedozvoljenPotez("Nema karata na stolu!"));
            return;
        }

        int indeksLagovca = gomila.getIndeksPoslednjegIgraca();

        if (indeksVikaca == indeksLagovca) {
            fire(l -> l.onNedozvoljenPotez("Ne možeš vikati BULLSHIT na sebe!"));
            return;
        }

        Igrac lagovac = igraci.get(indeksLagovca);
        Igrac vikac = igraci.get(indeksVikaca);

        boolean jeliLagao = gomila.proveraBlefa();
        Igrac kaznjeni = jeliLagao ? lagovac : vikac;

        // Kaznjeni uzima sve karte sa stola
        List<Card> karte = gomila.uzmiSveKarte();
        kaznjeni.dodajKarte(karte);

        final boolean finalJeLagao = jeliLagao;
        final List<Card> finalKarte = karte;

        fire(l -> l.onBullshitRezultat(lagovac, vikac, finalJeLagao, kaznjeni, finalKarte));

        // Kazna: gubitak života
        boolean maxDostignuto = false;
        if (!jeliLagao) {
            maxDostignuto = vikac.dodajNeuspesniBullshit();
            if (maxDostignuto) {
                boolean josUIgri2 = vikac.izgubioZivot();
                if (!josUIgri2) {
                    fire(l -> l.onIgracIspao(vikac));
                }
            }
        }
        boolean josUIgri = kaznjeni.izgubioZivot();

        final int preostalo = kaznjeni.getBrZivota();
        fire(l -> l.onIgracIzgubioZivot(kaznjeni, preostalo));

        if (!josUIgri) {
            // Igrač ispada
            fire(l -> l.onIgracIspao(kaznjeni));

            // Ako je ostao samo jedan aktivan igrač → kraj
            List<Igrac> aktivni = getAktivniIgraci();
            if (aktivni.size() == 1) {
                Igrac pobednik = aktivni.get(0);
                fire(l -> l.onKrajIgre(pobednik));
                return;
            }

            // Ako je kaznjeni bio na redu, pomeramo pointer
            if (indeksLagovca == trenutniIndeks || !kaznjeni.isAktivan()) {
                prebaciNaSledeceg();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Interna logika kretanja
    // ─────────────────────────────────────────────────────────────

    /**
     * Prelazi na sledećeg aktivnog igrača i inkrementuje rang.
     *
     * @return sledeći igrač
     */
    private Igrac prebaciNaSledeceg() {
        // Pomeramo rang
        Rank[] sviRangovi = Rank.values();
        int noviOrdinal = (trenutniRang.ordinal() + 1) % sviRangovi.length;
        trenutniRang = sviRangovi[noviOrdinal];
        fire(l -> l.onNovRang(trenutniRang));

        // Tražimo sledeceg aktivnog igrača (preskačemo ispale)
        do {
            trenutniIndeks = (trenutniIndeks + 1) % igraci.size();
        } while (!igraci.get(trenutniIndeks).isAktivan());

        return igraci.get(trenutniIndeks);
    }

    // ─────────────────────────────────────────────────────────────
    // Pomoćne metode
    // ─────────────────────────────────────────────────────────────

    private void fire(java.util.function.Consumer<GameLogicListener> action) {
        if (listener != null) {
            action.accept(listener);
        }
    }

    public Igrac getTrenutniIgrac() {
        return igraci.get(trenutniIndeks);
    }

    public Rank getTrenutniRang() {
        return trenutniRang;
    }

    public int getTrenutniIndeks() {
        return trenutniIndeks;
    }

    public List<Igrac> getIgraci() {
        return igraci;
    }

    public List<Igrac> getAktivniIgraci() {
        List<Igrac> aktivni = new ArrayList<>();
        for (Igrac igrac : igraci) {
            if (igrac.isAktivan()) aktivni.add(igrac);
        }
        return aktivni;
    }

    public GomilaNaStolu getGomila() {
        return gomila;
    }

    /**
     * Vraća igrača po indeksu (0-based).
     */
    public Igrac getIgrac(int indeks) {
        return igraci.get(indeks);
    }

    /**
     * Koliko je karata ukupno na stolu.
     */
    public int getBrojKarataStol() {
        return gomila.getBrojKarata();
    }
}
