package service;

import model.Cont;
import model.ContCurent;

public class TipNotificator_Cont {
    public static NotificatorTranzactie obtineTipNotificator(Cont cont){
        if (cont instanceof ContCurent){
            return new NotificatorEmail();
        }
        else{
            return new NotificatorSMS();
        }
    }
}
