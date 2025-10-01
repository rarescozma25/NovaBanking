package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface GestionareCont {

    void calculeazaDobanda(LocalDate dataCurenta);
    void depune(Bani suma,String descriere);
    void retrage(Bani suma, String descriere);
    void transfera(Bani suma,String descriere,Cont destinatar);

    default boolean areFonduriSuficiente(Bani sold, Bani suma) {
        return sold.getValoare()>=suma.getValoare();
    }

    Cont getCont();
}
