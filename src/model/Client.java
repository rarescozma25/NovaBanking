package model;

import util.ParolaUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class Client {
    private final String nume;
    private final String prenume;
    private String parola;
    private final String cnp;
    private final Adresa adresa;
    private Set<Cont> conturi;
    private  int id;
    public Client(String nume, String prenume, String cnp, Adresa adresa,String parola)   {
        this.nume = nume;
        this.prenume = prenume;
        if(!esteCnpValid(cnp)) throw new CNP_invalidExceptie("CNP invalid");
        this.cnp = cnp;
        this.adresa = adresa;
        this.parola = ParolaUtils.hashParola(parola);
        this.conturi = new TreeSet<>();
    }

    public Client(int id,String nume, String prenume, String cnp, Adresa adresa,String parola)   {
        this.id = id;
        this.nume = nume;
        this.prenume = prenume;
        if(!esteCnpValid(cnp)) throw new CNP_invalidExceptie("CNP invalid");
        this.cnp = cnp;
        this.adresa = adresa;
        this.parola = ParolaUtils.hashParola(parola);
        this.conturi = new TreeSet<>();
    }

    public String getNume() {
        return nume;
    }
    public String getPrenume() {
        return prenume;
    }
    public String getCnp() {
        return cnp;
    }

    public String getParola(){
        return parola;
    }

    public String getParolaHash() {
        return ParolaUtils.hashParola(this.parola);
    }
    private boolean esteCnpValid(String cnp) {
        if (cnp == null || cnp.length() != 13 || !cnp.matches("\\d{13}"))
            return false;
        return true;
        }
    public boolean verificaParola(String parola) {
        return this.parola.equals(ParolaUtils.hashParola(parola)); //nu mai verificam parolele propriu-zise, ci hash-ul dintre ele

    }

    public void adaugaCont(Cont cont) {
        conturi.add(cont);}

    public void setParola(String parola) {
        this.parola = parola;
    }

    public Set<Cont> getConturi() {
        return conturi;
    }

    @Override
    public String toString() {
        if(esteCnpValid(cnp))
            return "Clientul "+ nume + " " + prenume + " avand varsta de " + this.getVarsta();
        else
            return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(nume, client.nume) && Objects.equals(prenume, client.prenume) && Objects.equals(cnp, client.cnp) && Objects.equals(adresa, client.adresa) && Objects.equals(parola, client.parola);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nume, prenume, cnp, adresa);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVarsta(){
        if (esteCnpValid(cnp)) {
            char s=cnp.charAt(0);
            int an=Integer.parseInt(cnp.substring(1,3));
            int luna=Integer.parseInt(cnp.substring(3,5));
            int zi=Integer.parseInt(cnp.substring(5,7));
            if (s=='1' || s=='2'){
                an+=1900;
            }
            else if (s=='5' || s=='6'){
                an+=2000;
            }
            else {System.out.println("Invalid CNP");
                  return 0;}
            LocalDate data_nasterii = LocalDate.of(an,luna,zi);
            return Period.between(data_nasterii, LocalDate.now()).getYears();
        }
        else {
            System.out.println("Invalid CNP");
            return 0;
        }

    }
}
