package model;

import java.util.Objects;

public final class Transfer extends Tranzactie {
    private final Cont cont_sursa;
    private final Cont cont_destinatar;


    public Transfer(Bani suma, String descriere, Cont cont_sursa, Cont cont_destinatar) {
        super(suma, descriere);

        this.cont_sursa = cont_sursa;
        this.cont_destinatar = cont_destinatar;
    }

    public Cont getCont_sursa() {
        return cont_sursa;
    }
    public Cont getCont_destinatar() {
        return cont_destinatar;
    }

    @Override
    public void executaTranzactie() {
        if (cont_sursa.getSold().getValoare() < super.getSuma().getValoare()) {
            throw new IllegalArgumentException("Fonduri insuficiente pentru transfer.");
        }

        Bani soldSursa = cont_sursa.getSold();
        cont_sursa.setSold(new Bani(soldSursa.getValoare() - getSuma().getValoare(), soldSursa.getMoneda()));

        Bani soldDestinatar = cont_destinatar.getSold();
        cont_destinatar.setSold(new Bani(soldDestinatar.getValoare() + getSuma().getValoare(), soldDestinatar.getMoneda()));
        cont_destinatar.gettranzactii().add(this);
    }

    @Override
    public String toString() {
        return "[ID " + this.getIdTranzactie() + "] " +
                "Transferul de " + this.getSuma() +
                " este în starea: [" + this.getStatus() + "]";
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(cont_sursa, transfer.cont_sursa) && Objects.equals(cont_destinatar, transfer.cont_destinatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cont_sursa, cont_destinatar);
    }
}
