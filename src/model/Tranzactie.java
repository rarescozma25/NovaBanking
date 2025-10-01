package model;
import service.StatusTranzactie;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract sealed class Tranzactie permits Transfer, Retragere, Depunere,PlataFactura {
    protected static int contorId = 0;
    protected final int idTranzactie;
    protected final Bani suma;
    protected final String descriere;
    protected final LocalDateTime dataTranzactie;
    private StatusTranzactie status;

    public Tranzactie(Bani suma, String descriere) {
        this.idTranzactie = ++contorId;
        this.suma = suma;
        this.descriere = descriere;
        this.dataTranzactie = LocalDateTime.now();
        this.status = StatusTranzactie.APROBATA;
    }

    public int getIdTranzactie() {
        return idTranzactie;
    }

    public Bani getSuma() {
        return suma;
    }

    public abstract void executaTranzactie();

    public String getDescriere() {
        return descriere;
    }

    public LocalDateTime getDataTranzactie() {
        return dataTranzactie;
    }
    public StatusTranzactie getStatus() { return status; }
    public void setStatus(StatusTranzactie status) { this.status = status; }
    @Override
    public String toString() {
        return "[" + idTranzactie + "] " + suma + " - " + descriere + " (" + dataTranzactie + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tranzactie that = (Tranzactie) o;
        return idTranzactie == that.idTranzactie && Objects.equals(suma, that.suma) && Objects.equals(descriere, that.descriere) && Objects.equals(dataTranzactie, that.dataTranzactie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTranzactie, suma, descriere, dataTranzactie);
    }
}