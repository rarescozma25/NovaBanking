package model;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Objects;

public class Card implements Comparable<Card> {
    private static final SecureRandom random=new SecureRandom();
    private int id;
    private final String numarCard;
    private final String numeTitular;
    private final String cvv;
    private final LocalDate dataExpirare;
    private final Cont contAsociat;
    private final TipCard tipCard;
    private boolean esteBlocat;


    public Card(Cont cont,TipCard tipCard) {
        this.numarCard = genereazaNumarCard();
        this.numeTitular = cont.getClient().getNume() + " " + cont.getClient().getPrenume();
        this.cvv = genereazaCVV();
        this.dataExpirare=LocalDate.now().plusYears(5);
        this.contAsociat = cont;
        this.esteBlocat=true;
        this.tipCard=tipCard;
    }

    public Card(int id,String numarCard, String numeTitular, String cvv, LocalDate dataExpirare, Cont contAsociat, TipCard tipCard, boolean esteBlocat) {
        this.id = id;
        this.numarCard = numarCard;
        this.numeTitular = numeTitular;
        this.cvv = cvv;
        this.dataExpirare = dataExpirare;
        this.contAsociat = contAsociat;
        this.tipCard = tipCard;
        this.esteBlocat = esteBlocat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String genereazaNumarCard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String genereazaCVV() {
        int cvvValue = 100 + random.nextInt(900);
        return String.valueOf(cvvValue);
    }

    public String getNumarCard() {
        return numarCard;
    }

    public String getNumeTitular() {
        return numeTitular;
    }

    public String getCvv() {
        return cvv;
    }

    public LocalDate getDataExpirare() {
        return dataExpirare;
    }

    public TipCard getTipCard() {
        return tipCard;
    }

    public boolean verificareBlocat(){
        return esteBlocat;
    }
    public boolean esteExpirat(){
        return dataExpirare.isBefore(LocalDate.now());
    }

    public void blocheazaCard(){
        esteBlocat=true;
    }
    public void deblocheazaCard(){
        esteBlocat=false;
    }

    @Override
    public String toString() {
        return "Card " + " [" + numarCard + "] " + numeTitular + ", expiră: " + dataExpirare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return Objects.equals(numarCard, card.numarCard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numarCard);
    }

    @Override
    public int compareTo(Card c) {
        return Float.compare(
                this.contAsociat.getSold().getValoare(),
                c.contAsociat.getSold().getValoare()
        );
    }
}
