package model;

public enum Moneda {
    RON("Lei"),EUR("€"),USD("$");
    private String simbol;
    Moneda(String simbol) {
        this.simbol = simbol;
    }

    @Override
    public String toString() {
        return name() + " (" + simbol + ")";
    }

}
