package model;

import database.ContEconomiiSQL;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ContEconomii extends Cont{
    private int id;
    private TipContEconomii tip;
    private LocalDate ultimaDobanda_Aplicata;

    public ContEconomii(Client client, Bani soldInitial, TipContEconomii tip) {
        super(client, soldInitial);
        this.tip = tip;
        this.ultimaDobanda_Aplicata=LocalDate.now();
    }

    public TipContEconomii getTip() {
        return tip;
    }


    @Override
    public void calculeazaDobanda(LocalDate dataCurenta) {
        long luniTrecute = ChronoUnit.MONTHS.between(ultimaDobanda_Aplicata, dataCurenta);

        if (luniTrecute >= tip.getDurata()) {
            float valoareNoua = super.getSold().getValoare() * (1 + tip.getDobanda());
            super.setSold(new Bani(valoareNoua, super.getSold().getMoneda()));
            ultimaDobanda_Aplicata = dataCurenta;
            System.out.println("Dobanda de " + tip.getDobanda() + " a fost aplicata.");
        } else {
            System.out.println("Nu au trecut suficiente luni pentru a aplica dobanda! ");
        }
    }

    public void calculeazaDobandaBD(LocalDate dataCurenta) {
        long luniTrecute = ChronoUnit.MONTHS.between(ultimaDobanda_Aplicata, dataCurenta);
        if (luniTrecute < tip.getDurata()) {
            System.out.println("Nu au trecut suficiente luni pentru a aplica dobânda! " +
                    "(au trecut doar " + luniTrecute + " luni, cerute: " + tip.getDurata() + ")");
            return;
        }

        float valoareVeche = super.getSold().getValoare();
        float dobanda = tip.getDobanda();
        float valoareNoua = valoareVeche*(1 + dobanda);
        super.setSold(new Bani(valoareNoua, super.getSold().getMoneda()));

        float delta = valoareNoua - valoareVeche;

        try {
            ContEconomiiSQL dao = new ContEconomiiSQL();

            boolean okSold = dao.updateSold(this.id, delta);
            if (!okSold) {
                throw new SQLException("Eșec la updateSold(contId=" + this.id + ", delta=" + delta + ")");
            }

            boolean okData = dao.updateUltimaDobanda(this.id, dataCurenta);
            if (!okData) {
                throw new SQLException("Eșec la updateUltimaDobanda(contId=" + this.id + ")");
            }

            this.ultimaDobanda_Aplicata = dataCurenta;
            System.out.println("Dobânda de " + dobanda + " aplicată și în BD la data " + dataCurenta);
        }
        catch (SQLException ex) {
            System.err.println("Eroare la BD (ContEconomii): " + ex.getMessage());
        }
    }

    public int getId() {
        return id;
    }

    public LocalDate getUltimaDobanda_Aplicata() {
        return ultimaDobanda_Aplicata;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setUltimaDobandaAplicata(LocalDate ultimaDobanda_Aplicata) {
        this.ultimaDobanda_Aplicata = ultimaDobanda_Aplicata;
    }
}
