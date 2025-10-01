package model;

import java.time.LocalDate;

public enum TipContEconomii {
    Trei_Luni(0.015f,3),Sase_Luni(0.035f,6),Un_An(0.05f,12);
    private final float dobanda;
    private final float durata;
    TipContEconomii(float dobanda,float durata) {
        this.dobanda = dobanda;
        this.durata = durata;
    }
    public float getDobanda() {
        return dobanda;
    }
    public float getDurata() {
        return durata;
    }


}
