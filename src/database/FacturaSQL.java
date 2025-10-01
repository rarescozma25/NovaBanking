package database;

import model.Bani;
import model.Factura;

import java.sql.*;

public class FacturaSQL {

    public int addFactura(Factura factura) throws SQLException {
        BaniSQL baniDAO = new BaniSQL();
        int sumaId = baniDAO.addBani(factura.getSuma());

        String sql = "INSERT INTO Factura (furnizor, descriere, suma_id) VALUES (?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, factura.getFurnizor());
            ps.setString(2, factura.getDescriere());
            ps.setInt(3, sumaId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }




}