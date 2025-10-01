package model;

import database.ContCurentSQL;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ContCurent extends Cont{
    private int id;
    private TipContCurent tip;
    private LocalDate ultimaDobanda_Aplicata;
    public ContCurent(Client client, Bani soldInitial, TipContCurent tip) {
        super(client, soldInitial); // IBAN-ul este generat în super
        this.tip = tip;
        this.ultimaDobanda_Aplicata = LocalDate.now();
    }



    public TipContCurent getTip() {
        return tip;
    }


    public void calculeazaDobanda(LocalDate dataCurenta) {
        long luniTrecute = ChronoUnit.MONTHS.between(ultimaDobanda_Aplicata, dataCurenta);
        if (luniTrecute>=6) {
            super.setSold(new Bani(super.getSold().getValoare() * (1 + tip.getDobanda()), super.getSold().getMoneda()));
            ultimaDobanda_Aplicata = dataCurenta;
            System.out.println("Dobanda de tipul " + tip.getDobanda() + " a fost aplicata!");
        }
        else{
            System.out.println("Nu au trecut suficiente luni pentru a aplica dobanda! ");
        }
    }

    public void calculeazaDobandaBD(LocalDate dataCurenta) {
        long luniTrecute = ChronoUnit.MONTHS.between(ultimaDobanda_Aplicata, dataCurenta);
        if (luniTrecute < 6) {
            System.out.println("Nu au trecut suficiente luni pentru a aplica dobânda! " + "(au trecut doar " + luniTrecute + " luni)");
            return;
        }

        float valoareVeche = super.getSold().getValoare();
        float dobanda = tip.getDobanda();
        float valoareNoua = valoareVeche*(1 + dobanda);
        super.setSold(new Bani(valoareNoua, super.getSold().getMoneda()));
        float delta = valoareNoua-valoareVeche;

        try {
            ContCurentSQL dao = new ContCurentSQL();

            boolean okSold = dao.updateSold(this.id, delta);
            if (!okSold) {
                throw new SQLException("Eșec la updateSold(contId=" + this.id + ", delta=" + delta + ")");
            }

            boolean okData = dao.updateUltimaDobanda(this.id, dataCurenta);
            if (!okData) {
                throw new SQLException("Eșec la updateUltimaDobanda(contId=" + this.id + ")");
            }

            this.ultimaDobanda_Aplicata = dataCurenta;
            System.out.println("Dobânda de " + dobanda + " aplicată cu succes în BD la data " + dataCurenta);
        }
        catch (SQLException ex) {
            System.err.println("Eroare la BD (ContCurent): " + ex.getMessage());
        }
    }

    public LocalDate getUltimaDobanda_Aplicata() {
        return ultimaDobanda_Aplicata;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setUltimaDobanda_Aplicata(LocalDate ultimaDobanda_Aplicata) {
        this.ultimaDobanda_Aplicata = ultimaDobanda_Aplicata;
    }
}
