package model;

import java.util.Objects;

public final class Bani {
    private  Float valoare;
    private final Moneda moneda;

    public Bani(Float suma, Moneda moneda) {
        this.valoare = suma;
        this.moneda = moneda;
    }
    public Float getValoare() {
        return valoare;
    }
    public Moneda getMoneda() {
        return moneda;
    }

    public void setValoare(Float valoare) {
        this.valoare = valoare;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Bani bani = (Bani) o;
        return Objects.equals(valoare, bani.valoare) && moneda == bani.moneda;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valoare, moneda);
    }

    @Override
    public String toString() {
        return valoare.toString() + " " + moneda.toString();
    }
}
