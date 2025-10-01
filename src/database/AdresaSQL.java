package database;

import model.Adresa;

import java.sql.*;

public class AdresaSQL {

    public int addAdresa(Adresa addr) throws SQLException {
        String query = "INSERT INTO Adresa (strada, oras, codPostal) VALUES (?, ?, ?)";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, addr.adresa());
            statement.setString(2, addr.oras());
            statement.setString(3, addr.codPostal());
            statement.executeUpdate();
            try (ResultSet rez = statement.getGeneratedKeys()) {
                if (rez.next()) {
                    return rez.getInt(1);
                }
            }
        }
        return -1;
    }

    public Adresa getAdresaById(int id) throws SQLException {
        String query = "SELECT strada, oras, codPostal FROM Adresa WHERE id = ?";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet rez = statement.executeQuery()) {
                if (rez.next()) {
                    return new Adresa(
                            rez.getString("strada"),
                            rez.getString("oras"),
                            rez.getString("codPostal")
                    );
                }
            }
        }
        return null;
    }
}
