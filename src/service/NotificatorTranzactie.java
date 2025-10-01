package service;

import model.Cont;
import model.Factura;

public interface NotificatorTranzactie {
    void notificaDepunere(Cont cont, float suma);
    void notificaRetragere(Cont cont,float suma);
    void notificaTransfer(Cont sursa,Cont destinatie,float suma);
    void notificaPlataFactura(Cont cont, Factura factura);

}
