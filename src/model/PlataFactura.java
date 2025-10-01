package model;

public sealed class PlataFactura extends Tranzactie permits PlataFacturaRecurenta {
    private final Cont contSursa;
    private final Factura factura;

    public PlataFactura(Cont contSursa, Factura factura) {
        super(factura.getSuma(), factura.getDescriere());
        this.contSursa = contSursa;
        this.factura = factura;
    }

    public Cont getContSursa() {
        return contSursa;
    }

    public Factura getFactura() {
        return factura;
    }

    @Override
    public void executaTranzactie() {
        Bani soldCurent=contSursa.getSold();
        contSursa.setSold(new Bani(
                soldCurent.getValoare() - suma.getValoare(), soldCurent.getMoneda()));
    }

    @Override
    public String toString() {
        return "ID:" + idTranzactie + ": Plata facturii către " + factura.getFurnizor() +
                " în valoare de " + suma.getValoare() + " " + suma.getMoneda();
    }
}
