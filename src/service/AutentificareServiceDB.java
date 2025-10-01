package service;

import model.Adresa;
import model.CNP_invalidExceptie;
import model.Client;
import util.AuditService;
import util.ParolaUtils;
import database.AdresaSQL;
import database.ClientSQL;

import java.sql.SQLException;
import java.util.Scanner;

public class AutentificareServiceDB {
    private final ClientSQL clientDao;
    private final AdresaSQL adresaDao;
    private final SessionManager sessionManager;

    public AutentificareServiceDB() {
        this.clientDao = new ClientSQL();
        this.adresaDao = new AdresaSQL();
        this.sessionManager = SessionManager.getInstance();
    }


    public void creareCont(String nume, String prenume, String cnp, String parola, Adresa adresa) {
        try {
            if (clientDao.getClientByCnp(cnp) != null) {
                System.out.println("Client cu acest CNP există deja!");
                return;
            }

            int adresaId = adresaDao.addAdresa(adresa);
            Client nou = new Client(nume, prenume, cnp, adresa, parola);
            clientDao.addClient(nou, adresaId);
            AuditService.log("Creare cont", "CNP: " + cnp + ", Nume: " + nume + " " + prenume);
        } catch (CNP_invalidExceptie e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.err.println("Eroare acces baza de date la creare cont: " + e.getMessage());
        }
    }


    public boolean login(String cnp, String parola) {
        try {
            String hash = ParolaUtils.hashParola(parola);

            Client client=clientDao.getClientByCnp(cnp);
            if (client != null) {
                String otpGenerat = OtpService.genereazaOtp();
                System.out.println("Parola corectă. Codul OTP a fost trimis (verifica otp_log.txt)");
                System.out.print("Introdu codul OTP: ");
                String codIntrodus = new Scanner(System.in).nextLine().trim();
                if (OtpService.valideaza(codIntrodus)) {
                    sessionManager.login(client);
                    AuditService.log("Validare OTP finalizata", "CNP: " + cnp);
                    System.out.println("Autentificat complet ca " + client.getNume() + " " + client.getPrenume());
                    OtpService.stergeCod();
                    return true;
                } else {
                    AuditService.log("Eroare OTP", "CNP: " + cnp + ", OTP introdus gresit");
                    System.out.println("Cod OTP invalid.");
                }
            } else {
                AuditService.log("Login esuat", "CNP sau parola incorecte");
                System.out.println("CNP sau parolă incorecta.");
            }
        } catch (SQLException e) {
            System.err.println("Eroare acces baza de date la autentificare: " + e.getMessage());
        }
        return false;
    }


    public void logout() {
        Client client = sessionManager.getClientAutentificat();
        if (client != null) {
            sessionManager.logout();
            System.out.println("Logout efectuat pentru " + client.getNume());
            AuditService.log("Logout efectuat", "CNP: " + client.getCnp());
        } else {
            System.out.println("Niciun utilizator autentificat.");
            AuditService.log("Eroare la logout", "Niciun utilizator autentificat.");
        }
    }


    public boolean reseteazaParola(String cnp, String parolaNoua) {
        try {
            Client client = clientDao.getClientByCnp(cnp);
            if (client != null) {
                String hash = ParolaUtils.hashParola(parolaNoua);
                clientDao.updateParola(cnp, hash);
                AuditService.log("Resetare parolă", "CNP: " + cnp);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Eroare acces baza de date la resetare parolă: " + e.getMessage());
        }
        return false;
    }





    public Client getClientAutentificat() {
        return sessionManager.getClientAutentificat();
    }
}

