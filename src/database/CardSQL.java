package database;

import database.DbConnection;
import model.Card;
import model.Cont;
import model.ContCurent;
import model.ContEconomii;
import model.TipCard;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CardSQL {
    private final ContCurentSQL contCurentDao = new ContCurentSQL();
    private final ContEconomiiSQL contEconomiiDao = new ContEconomiiSQL();

    public int addCard(Card card, int contId) throws SQLException {
        String sql = "INSERT INTO Card (cont_id, numarCard, numeTitular, cvv, dataExpirare, tip, esteBlocat) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, contId);
            ps.setString(2, card.getNumarCard());
            ps.setString(3, card.getNumeTitular());
            ps.setString(4, card.getCvv());
            ps.setDate(5, Date.valueOf(card.getDataExpirare()));
            ps.setString(6, card.getTipCard().name());
            ps.setBoolean(7, card.verificareBlocat());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public boolean setBlockStatus(String numarCard, boolean blocat) throws SQLException {
        String sql = "UPDATE Card SET esteBlocat = ? WHERE numarCard = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, blocat);
            ps.setString(2, numarCard);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCard(String numarCard) throws SQLException {
        String sql = "DELETE FROM Card WHERE numarCard = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numarCard);
            return ps.executeUpdate() > 0;
        }
    }



    public List<Card> getCardsByContId(int contId) throws SQLException {
        String sql = """
            SELECT id, numarCard, numeTitular, cvv, dataExpirare, tip, esteBlocat
            FROM Card
            WHERE cont_id = ?
            """;
        List<Card> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id            = rs.getInt("id");
                    String numar      = rs.getString("numarCard");
                    String titular    = rs.getString("numeTitular");
                    String cvv        = rs.getString("cvv");
                    LocalDate exp     = rs.getDate("dataExpirare").toLocalDate();
                    TipCard tip       = TipCard.valueOf(rs.getString("tip"));
                    boolean blocat    = rs.getBoolean("esteBlocat");

                    // Reconstruiește Cont-ul corect
                    Cont cont = getContById(contId);

                    // Folosește constructorul de încărcare din BD (trebuie adăugat în model)
                    Card card = new Card(id, numar, titular, cvv, exp, cont, tip, blocat);
                    list.add(card);
                }
            }
        }
        return list;
    }

    private Cont getContById(int contId) {
        try {
            return contCurentDao.getContCurentById(contId);
        } catch (Exception e) {
            try {
                return contEconomiiDao.getContEconomiiById(contId);
            } catch (Exception ex) {
                throw new RuntimeException("Cont cu ID " + contId + " nu a fost găsit", ex);
            }
        }
    }
}
