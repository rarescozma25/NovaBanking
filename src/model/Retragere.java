package model;

import java.util.Objects;

public final class Retragere extends Tranzactie {
        private final Cont cont_sursa;

        public Retragere(Bani bani,String descriere, Cont cont_sursa){
            super(bani,descriere);
            this.cont_sursa=cont_sursa;
        }

        @Override
        public void executaTranzactie() {

            if(cont_sursa.getSold().getValoare()<super.getSuma().getValoare()){
                throw new FonduriInsuficienteExceptie("Fonduri insuficiente pentru retragere.");
            }
            else{
                Bani soldCurent=cont_sursa.getSold();
                cont_sursa.setSold(new Bani(soldCurent.getValoare()-super.getSuma().getValoare(), soldCurent.getMoneda()));

            }
        }
    @Override
    public String toString() {
        return "ID:"+this.idTranzactie+": Retragerea de "+ this.suma.getValoare()+ " a fost realizata cu succes!";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Retragere retragere = (Retragere) o;
        return Objects.equals(cont_sursa, retragere.cont_sursa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cont_sursa);
    }
}
