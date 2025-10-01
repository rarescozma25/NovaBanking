package service;

import model.Cont;
import model.Factura;
import util.LoggerService;

public class NotificatorSMS implements NotificatorTranzactie {
    @Override
    public void notificaDepunere(Cont cont, float suma) {
        String mesaj= "SMS: Ai depus suma de "+suma+ " "+cont.getSold().getMoneda()+" in contul cu iban-ul "+cont.getIban();
        LoggerService.scrieMesaj(mesaj);
    }

    @Override
    public void notificaRetragere(Cont cont, float suma) {
        String mesaj= "SMS :Ai retras suma de "+suma+ " "+cont.getSold().getMoneda()+" in contul cu iban-ul "+cont.getIban();
        LoggerService.scrieMesaj(mesaj);
    }

    @Override
    public void notificaTransfer(Cont sursa, Cont destinatie, float suma) {
        String mesaj= "SMS: Ai transferat suma de "+suma+ " "+sursa.getSold().getMoneda()+" in contul titularului "+destinatie.getClient().getNume()+" cu iban-ul "+destinatie.getIban();
        LoggerService.scrieMesaj(mesaj);

    }

    @Override
    public void notificaPlataFactura(Cont cont, Factura factura){
        String mesaj = "SMS: Ai plătit factura către " + factura.getFurnizor() + " în valoare de " +
                factura.getSuma().getValoare() + " " + cont.getSold().getMoneda() +
                ", din contul cu IBAN " + cont.getIban();
        LoggerService.scrieMesaj(mesaj);
    }
}
