package database;

import model.Bani;
import model.Client;
import model.ContEconomii;
import model.TipContEconomii;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class ContEconomiiSQL {

    public int addContEconomii(ContEconomii cont) throws SQLException {
        int clientId = cont.getClient().getId();
        if (clientId <= 0) {
            throw new IllegalArgumentException("Client ID invalid: " + clientId);
        }

        BaniSQL baniDAO = new BaniSQL();
        int soldId = baniDAO.addBani(cont.getSold());

        String sql = "INSERT INTO ContEconomii (client_id, sold_id, tip, ultimaDobandaAplicata) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, clientId);
            ps.setInt(2, soldId);
            ps.setString(3, cont.getTip().name());
            ps.setDate(4, Date.valueOf(cont.getUltimaDobanda_Aplicata()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    cont.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean updateUltimaDobanda(int contId, LocalDate dataDobanda) throws SQLException {
        String sql = "UPDATE ContEconimii SET ultimaDobandaAplicata = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(dataDobanda));
            ps.setInt(2, contId);
            return ps.executeUpdate() > 0;
        }
    }

    public ContEconomii getContEconomiiById(int id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT client_id, sold_id, tip, ultimaDobandaAplicata FROM ContEconomii WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Client client = new ClientSQL().getClientById(rs.getInt("client_id"));
                    Bani sold = new BaniSQL().getBaniById(rs.getInt("sold_id"));
                    TipContEconomii tip = TipContEconomii.valueOf(rs.getString("tip"));
                    LocalDate ultima = rs.getDate("ultimaDobandaAplicata").toLocalDate();

                    ContEconomii cont = new ContEconomii(client, sold, tip);
                    cont.setId(id);
                    cont.setUltimaDobandaAplicata(ultima);
                    return cont;
                }
            }
        }
        return null;
    }


    public List<ContEconomii> getAllContEconomii() throws SQLException, ClassNotFoundException {
        List<ContEconomii> list = new ArrayList<>();
        String sql = "SELECT id FROM ContEconomii";
        try (Connection conn = DbConnection.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                list.add(getContEconomiiById(rs.getInt("id")));
            }
        }
        return list;
    }



    public boolean updateSold(int contId, float delta) throws SQLException {
        // 1) Get sold_id from the account
        String query = "SELECT sold_id FROM ContEconomii WHERE id = ?";
        int soldId;
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, contId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                soldId = rs.getInt("sold_id");
            }
        }
        String update = "UPDATE Bani SET valoare = valoare + ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setDouble(1, delta);
            ps.setInt(2, soldId);
            return ps.executeUpdate() > 0;
        }
    }



}