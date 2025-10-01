package service;
import model.Adresa;
import model.CNP_invalidExceptie;
import model.Client;
import util.AuditService;
import util.ParolaUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AutentificareService {
    private final Map<String, Client> clienti;
    private final SessionManager sessionManager;

    public AutentificareService() {
        this.clienti = new HashMap<>();
        this.sessionManager = SessionManager.getInstance();
    }

    public void creareCont(String nume, String prenume, String cnp, String parola, Adresa adresa) {
        if (clienti.containsKey(cnp)) {
            System.out.println("Client cu acest CNP există deja!");
            return;
        }
        try {
            Client nou = new Client(nume, prenume, cnp, adresa, parola);
            clienti.put(cnp, nou);
            AuditService.log("Creare cont","CNP: "+cnp+", Nume: "+nume+" "+prenume);
        }catch (CNP_invalidExceptie e) {
            System.out.println(e.getMessage());
        }


    }

    public boolean login(String cnp, String parola) {
        Client client = clienti.get(cnp);
        if (client != null && client.verificaParola(parola)) {
            String otpGenerat = OtpService.genereazaOtp();
            System.out.println("Parolă corectă. Codul OTP a fost trimis (verifică otp_log.txt)");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Introdu codul OTP: ");
            String codIntrodus = scanner.nextLine();

            if (OtpService.valideaza(codIntrodus)) {
                sessionManager.login(client);
                AuditService.log("Validare OTP finalizata","CNP: "+cnp);
                System.out.println("Autentificat complet ca " + client.getNume() + " " + client.getPrenume());
                OtpService.stergeCod();
                return true;
            } else {
                AuditService.log("Eroare OTP", "CNP: "+cnp +", OTP introdus greșit");
                System.out.println("Cod OTP invalid.");
            }
        } else {
            AuditService.log("Login esuat","Metodele de validare nu au fost indeplinite!");
            System.out.println("CNP sau parolă incorectă.");
        }
        return false;
    }

    public  void logout() {
        Client client = sessionManager.getClientAutentificat();
        if (client != null) {
            sessionManager.logout();
            System.out.println("Logout efectuat pentru " + client.getNume());
            AuditService.log("Logout efectuat","CNP: "+client.getNume());
        } else {
            System.out.println("Niciun utilizator autentificat.");
            AuditService.log("Eroare la logout","Niciun utilizator autentificat.");
        }
    }

    public boolean reseteazaParola(String cnp, String parolaNoua) {
        Client client = clienti.get(cnp);
        if (client != null) {
            client.setParola(ParolaUtils.hashParola(parolaNoua));
            AuditService.log("Logout", "CNP: "+client.getCnp());
            return true;
        }
        return false;
    }


    public Client getClient(String cnp){
        return clienti.get(cnp);
    }

    public boolean esteAutentificat() {
        return sessionManager.esteAutentificat();
    }

    public Client getClientAutentificat() {
        return sessionManager.getClientAutentificat();
    }
}
