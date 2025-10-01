package model;

import database.TranzactieSQL;
import util.AuditService;
import service.MonedaService;
import service.StatusTranzactie;
import service.TipNotificator_Cont;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public abstract class Cont implements GestionareCont,Comparable<Cont>{
    private static int contor_iban=1;
    private final Client client;
    private final String iban;
    private Bani sold;
    private List<Tranzactie> tranzactii;
    private List <Card> carduri;

    public Cont(Client client, Bani soldInitial) {
        this.iban=genereazaIBAN();
        this.client = client;
        this.sold=soldInitial;
        this.tranzactii=new ArrayList<Tranzactie>();
        this.carduri=new ArrayList<>();
    }




    private static String genereazaIBAN(){
        String cod_Tara="RO";
        String cod_control="25";
        String cod_banca="BRC";
        String numar_cont=String.format("%016d",contor_iban++);
        return cod_Tara + cod_control + cod_banca + numar_cont;
    }



    public Client getClient() {
        return client;
    }

    public String getIban() {
        return iban;
    }

    public Bani getSold() {
        return sold;
    }

    public void setSold(Bani sold) {
        this.sold = sold;
    }

    public List<Tranzactie> gettranzactii() {
        return tranzactii;
    }

    public Cont getCont() {
        return this;
    }
    public void adaugaCard(Card card){
        carduri.add(card);
    }

    public List<Card> getCarduri(){
        return carduri;
    }

    public boolean existaCardValid(){
        return carduri.stream().anyMatch(card -> !card.verificareBlocat() && !card.esteExpirat());
    }

    public void depune(Bani suma, String descriere) {
        float valoare = suma.getValoare();
        Moneda monedaSursa = suma.getMoneda();
        Moneda monedaCont = this.getSold().getMoneda();

        if (!monedaSursa.equals(monedaCont)) {
            double curs = MonedaService.curs(monedaSursa, monedaCont);
            if (curs<0) throw new RuntimeException("Conversie valută eșuată.");
            valoare *= curs;
        }

        Tranzactie depunere = new Depunere(new Bani(valoare, monedaCont), descriere, this);
        tranzactii.add(depunere);
        depunere.executaTranzactie();
        AuditService.log("Depunere in contul cu iban-ul "+getIban()," Sold actualizat: "+getSold());
        TipNotificator_Cont.obtineTipNotificator(this).notificaDepunere(this, valoare);
    }

    public void retrage(Bani suma, String descriere) {
        if(!areFonduriSuficiente(sold,suma)){
            throw new FonduriInsuficienteExceptie ("Fonduri nesuficiente");
        }
        boolean cardValid=this.existaCardValid();
        if(!cardValid){
            throw new RuntimeException("Nu exista niciun card valid pentru a efectua operatia de retragere!");
        }
        Tranzactie retragere = new Retragere(suma, descriere, this);
        tranzactii.add(retragere);
        retragere.executaTranzactie();
        AuditService.log("Retragere din contul cu iban-ul "+getIban()," Sold actualizat: "+getSold());
        TipNotificator_Cont.obtineTipNotificator(this).notificaRetragere(this,suma.getValoare());
    }


    public void transfera(Bani suma, String descriere, Cont destinatar) {

        float valoareInput = suma.getValoare();
        Moneda monedaInput = suma.getMoneda();
        double cursul_in_RON = MonedaService.curs(monedaInput, Moneda.RON);
        float valoare_RON=(float)(valoareInput*cursul_in_RON);
        if (valoare_RON>5000f){
            Tranzactie t = new Transfer(suma, descriere, this, destinatar);
            t.setStatus(StatusTranzactie.ASTEPTARE);
            this.gettranzactii().add(t);
            AuditService.log("Tranzacție PENDING", "Echivalent RON: " + valoare_RON + ", " + descriere);
            System.out.println("Tranzacția a fost marcată ca PENDING (echivalent " + valoare_RON + " RON).");
            return;
        }

        Moneda monedaContSursa = this.getSold().getMoneda();
        Moneda monedaContDest = destinatar.getSold().getMoneda();


        float valoareInMonedaSursa = valoareInput;
        if (!monedaInput.equals(monedaContSursa)) {
            double curs = MonedaService.curs(monedaInput, monedaContSursa);
            valoareInMonedaSursa *= curs;
        }

        Bani sumaPtVerificare = new Bani(valoareInMonedaSursa, monedaContSursa);
        if (!areFonduriSuficiente(sold, sumaPtVerificare)) {
            throw new FonduriInsuficienteExceptie("Fonduri insuficiente pentru transfer.");
        }

        float soldNou = sold.getValoare() - valoareInMonedaSursa;
        this.setSold(new Bani(soldNou, monedaContSursa));

        float valoarePentruDest = valoareInMonedaSursa;
        if (!monedaContSursa.equals(monedaContDest)) {
            double cursFinal = MonedaService.curs(monedaContSursa, monedaContDest);
            valoarePentruDest *= cursFinal;
        }


        float soldDestNou = destinatar.getSold().getValoare() + valoarePentruDest;
        destinatar.setSold(new Bani(soldDestNou, monedaContDest));
        TipNotificator_Cont.obtineTipNotificator(this).notificaTransfer(this,destinatar,suma.getValoare());

    }


    public StatusTranzactie transferaDB(Bani suma, String descriere, Cont destinatar,
                                        TranzactieSQL tranzactieDao, int sursaId, int destId, int sumaId) {
        float valoareInput = suma.getValoare();
        Moneda monedaInput = suma.getMoneda();
        double cursul_in_RON = MonedaService.curs(monedaInput, Moneda.RON);
        float valoare_RON = (float)(valoareInput * cursul_in_RON);

        if (valoare_RON>5000f) {
            try {
                Transfer t = new Transfer(suma, descriere, this, destinatar);
                t.setStatus(StatusTranzactie.ASTEPTARE);
                tranzactieDao.addTranzactie(t, sursaId, destId, sumaId, null);
                AuditService.log("Tranzacție PENDING", "Echivalent RON: " + valoare_RON + ", " + descriere);
                System.out.println("Tranzacția a fost marcată ca PENDING (echivalent " + valoare_RON + " RON).");
                return StatusTranzactie.ASTEPTARE;
            } catch (Exception e) {
                System.err.println("Eroare la salvarea tranzacției PENDING: " + e.getMessage());
                return StatusTranzactie.RESPINSA;
            }
        }

        Moneda monedaContSursa = this.getSold().getMoneda();
        Moneda monedaContDest = destinatar.getSold().getMoneda();


        float valoareInMonedaSursa = valoareInput;
        if (!monedaInput.equals(monedaContSursa)) {
            double curs = MonedaService.curs(monedaInput, monedaContSursa);
            valoareInMonedaSursa *= curs;
        }

        Bani sumaPtVerificare = new Bani(valoareInMonedaSursa, monedaContSursa);
        if (!areFonduriSuficiente(sold, sumaPtVerificare)) {
            throw new FonduriInsuficienteExceptie("Fonduri insuficiente pentru transfer.");
        }

        float soldNou = sold.getValoare() - valoareInMonedaSursa;
        this.setSold(new Bani(soldNou, monedaContSursa));

        float valoarePentruDest = valoareInMonedaSursa;
        if (!monedaContSursa.equals(monedaContDest)) {
            double cursFinal = MonedaService.curs(monedaContSursa, monedaContDest);
            valoarePentruDest *= cursFinal;
        }


        float soldDestNou = destinatar.getSold().getValoare() + valoarePentruDest;
        destinatar.setSold(new Bani(soldDestNou, monedaContDest));
        TipNotificator_Cont.obtineTipNotificator(this).notificaTransfer(this,destinatar,suma.getValoare());

        return StatusTranzactie.APROBATA;
    }





    public void platesteFactura(Factura factura){
        if(!areFonduriSuficiente(sold,factura.getSuma())){
            throw new FonduriInsuficienteExceptie("Fonduri insuficiente pentru plata facturii! ");
        }

        Tranzactie plata= new PlataFactura(this,factura);
        tranzactii.add(plata);
        plata.executaTranzactie();
        TipNotificator_Cont.obtineTipNotificator(this).notificaPlataFactura(this, factura);
    }


    public abstract void calculeazaDobanda(LocalDate dataCurenta);
    public abstract void calculeazaDobandaBD(LocalDate dataCurenta);

    @Override
    public int compareTo(Cont o) {
        float valoareThis = this.getSold().getValoare();
        float valoareOther = o.getSold().getValoare();

        if (!this.getSold().getMoneda().equals(Moneda.RON)) {
            valoareThis*= (float) MonedaService.curs(this.getSold().getMoneda(), Moneda.RON);
        }

        if (!o.getSold().getMoneda().equals(Moneda.RON)) {
            valoareOther*=(float) MonedaService.curs(o.getSold().getMoneda(), Moneda.RON);
        }

        int cmp= Float.compare(valoareOther, valoareThis);
        if (cmp!=0) return cmp;
        return this.getIban().compareTo(o.getIban());
    }

    public List<Tranzactie> extrasdecont(LocalDateTime start, LocalDateTime end) {
        return tranzactii.stream().filter(t->!t.getDataTranzactie().isBefore(start) && !t.getDataTranzactie().isAfter(end))
                .collect(Collectors.toList());
    }
    public void afiseazaExtras(LocalDateTime start,LocalDateTime end){
        List <Tranzactie>tranzactii_efectuate=extrasdecont(start,end);
        if (tranzactii_efectuate.isEmpty()){
            System.out.println("Nu exista tranzactii efectuate in perioada data");
        }
        else{
            tranzactii_efectuate.forEach(System.out::println);
        }
    }

    @Override
    public String toString() {
        return "Contul detinut de "+this.client.getNume()+" "+this.client.getPrenume()+ " avand soldul de "+ this.sold;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cont cont = (Cont) o;
        return Objects.equals(client, cont.client) && Objects.equals(iban, cont.iban) && Objects.equals(sold, cont.sold) && Objects.equals(tranzactii, cont.tranzactii);
    }

    @Override
    public int hashCode() {
        return Objects.hash(client, iban, sold, tranzactii);
    }
}

