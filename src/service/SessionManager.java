package service;

import model.Client;

public class SessionManager {
    private static SessionManager instance; // instanță unică
    private Client clientAutentificat;

    private SessionManager() {
        this.clientAutentificat = null;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Client client) {
        this.clientAutentificat = client;
    }

    public void logout() {
        this.clientAutentificat = null;
    }

    public boolean esteAutentificat() {
        return clientAutentificat != null;
    }

    public Client getClientAutentificat() {
        return clientAutentificat;
    }
}
