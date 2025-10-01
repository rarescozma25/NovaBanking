package model;

public class Factura {
    private final String furnizor;
    private final Bani suma;
    private final String descriere;

    public Factura(String furnizor, Bani suma, String descriere) {
        this.furnizor = furnizor;
        this.suma = suma;
        this.descriere = descriere;
    }

    public String getFurnizor() {
        return furnizor;
    }

    public Bani getSuma() {
        return suma;
    }

    public String getDescriere() {
        return descriere;
    }

    @Override
    public String toString() {
        return "Factura catre " + furnizor + " - " + suma.getValoare() + " " + suma.getMoneda() + " (" + descriere + ")";
    }
}

