package database;

import database.AdresaSQL;
import database.DbConnection;
import model.Adresa;
import model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientSQL {

    public boolean addClient(Client c, int idAdresa) throws SQLException {
        String query = "INSERT INTO Client (nume, prenume, cnp, parola, adresa_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {

            statement.setString(1, c.getNume());
            statement.setString(2, c.getPrenume());
            statement.setString(3, c.getCnp());
            statement.setString(4, c.getParola());
            statement.setInt(5, idAdresa);

            return statement.executeUpdate() > 0;
        }
    }

    public Client getClientById(int id) throws SQLException {
        String query = "SELECT nume, prenume, cnp, parola, adresa_id FROM Client WHERE id = ?";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet rez = statement.executeQuery()) {
                if (rez.next()) {
                    String nume = rez.getString("nume");
                    String prenume = rez.getString("prenume");
                    String cnp = rez.getString("cnp");
                    String parola = rez.getString("parola");
                    int idAdresa = rez.getInt("adresa_id");
                    Adresa adresa = new AdresaSQL().getAdresaById(idAdresa);
                    return new Client(id, nume, prenume, cnp, adresa, parola);
                }
            }
        }
        return null;
    }




    public boolean updateParola(String cnp, String parolaNoua) throws SQLException {
        String query = "UPDATE Client SET parola = ? WHERE cnp = ?";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setString(1, parolaNoua);
            statement.setString(2, cnp);
            return statement.executeUpdate() > 0;
        }
    }



    public Client getClientByCnp(String cnp) throws SQLException {
        String query = "SELECT id, nume, prenume, parola, adresa_id FROM Client WHERE cnp = ?";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setString(1, cnp);
            try (ResultSet rez = statement.executeQuery()) {
                if (rez.next()) {
                    int id = rez.getInt("id");
                    String nume = rez.getString("nume");
                    String prenume = rez.getString("prenume");
                    String parola = rez.getString("parola");
                    int idAdresa = rez.getInt("adresa_id");
                    Adresa adresa = new AdresaSQL().getAdresaById(idAdresa);
                    Client client = new Client(id, nume, prenume, cnp, adresa, parola);
                    return client;
                }
            }
        }
        return null;
    }
    public Client getClientByCnpAndParola(String cnp, String parola) throws SQLException {
        String query = "SELECT id, nume, prenume, parola, adresa_id FROM Client WHERE cnp = ? AND parola = ?";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setString(1, cnp);
            statement.setString(2, parola);
            try (ResultSet rez = statement.executeQuery()) {
                if (rez.next()) {
                    int id = rez.getInt("id");
                    String nume = rez.getString("nume");
                    String prenume = rez.getString("prenume");
                    int idAdresa = rez.getInt("adresa_id");
                    Adresa adresa = new AdresaSQL().getAdresaById(idAdresa);
                    Client client = new Client(id, nume, prenume, cnp, adresa, parola);
                    return client;
                }
            }
        }
        return null;
    }
}
