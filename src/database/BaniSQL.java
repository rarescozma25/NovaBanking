
package database;

import model.Bani;
import model.Moneda;

import java.sql.*;


public class BaniSQL {
    public int addBani(Bani bani) throws SQLException {
        String query = "INSERT INTO Bani (valoare, moneda) VALUES (?, ?)";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setDouble(1, bani.getValoare());
            statement.setString(2, bani.getMoneda().name());
            statement.executeUpdate();
            try (ResultSet rez = statement.getGeneratedKeys()) {
                if (rez.next()) return rez.getInt(1);
            }
        }
        return -1;
    }

    public Bani getBaniById(int id) throws SQLException {
        String query = "SELECT valoare, moneda FROM Bani WHERE id = ?";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet rez = statement.executeQuery()) {
                if (rez.next()) {
                    return new Bani(rez.getFloat("valoare"), Moneda.valueOf(rez.getString("moneda"))
                    );
                }
            }
        }
        return null;
    }


}