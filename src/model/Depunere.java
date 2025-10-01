package model;

import java.util.Objects;

public final class Depunere extends Tranzactie {
    private final Cont cont_destinatar;
    public Depunere(Bani suma, String descriere, Cont cont_destinatar) {
        super(suma, descriere);
        this.cont_destinatar = cont_destinatar;
    }

    @Override
    public void executaTranzactie() {
        Bani soldCurent=cont_destinatar.getSold();
        cont_destinatar.setSold(new Bani(soldCurent.getValoare() + super.getSuma().getValoare(), soldCurent.getMoneda()));
    }
    @Override
    public String toString() {
        return "Depunerea cu id-ul "+ this.idTranzactie+" a fost realizata cu succes";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Depunere depunere = (Depunere) o;
        return Objects.equals(cont_destinatar, depunere.cont_destinatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cont_destinatar);
    }
}
