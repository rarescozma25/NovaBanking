package model;

public enum TipContCurent  {

    Standard(0.01f, 0.015f), Premium(0.03f, 0.01f), Platinum(0.05f, 0.005f);
    private final float dobanda;
    private final float comision;
    TipContCurent(float dobanda,float comision) {
        this.dobanda = dobanda;
        this.comision = comision;
    }
    public float getDobanda() {
        return dobanda;
    }

    public float getComision() {
        return comision;
    }
}
