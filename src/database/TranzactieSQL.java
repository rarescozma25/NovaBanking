package database;

import model.*;
import service.StatusTranzactie;
import service.MonedaService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class TranzactieSQL {
    public int addTranzactie(Tranzactie tranz, Integer idContSursa, Integer idContDest, int idSuma, Integer idFactura)
            throws SQLException {
        String query = "INSERT INTO Tranzactie " + "(cont_sursa_id, cont_destinatie_id, suma_id, factura_id, data, tip, descriere, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, idContSursa, Types.INTEGER);
            statement.setObject(2, idContDest, Types.INTEGER);
            statement.setInt(3, idSuma);
            statement.setObject(4, idFactura, Types.INTEGER);
            statement.setTimestamp(5, Timestamp.valueOf(tranz.getDataTranzactie()));
            statement.setString(6, tranz.getClass().getSimpleName());
            statement.setString(7, tranz.getDescriere());
            statement.setString(8, tranz.getStatus().name());
            statement.executeUpdate();
            try (ResultSet rez = statement.getGeneratedKeys()) {
                if (rez.next()) {
                    return rez.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<String> getExtrasLines(int idCont, LocalDateTime dataStart, LocalDateTime dataEnd) throws SQLException {
        String query = """
        SELECT
          DATE_FORMAT(data, '%Y-%m-%d %H:%i:%s') AS dt,
          tip,
          descriere,
          cont_sursa_id   AS sursa,
          cont_destinatie_id AS dest,
          suma_id,
          factura_id
        FROM Tranzactie
        WHERE (cont_sursa_id = ? OR cont_destinatie_id = ?)
          AND data BETWEEN ? AND ?
        ORDER BY data""";

        List<String> linii = new ArrayList<>();
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {

            statement.setInt(1, idCont);
            statement.setInt(2, idCont);
            statement.setTimestamp(3, Timestamp.valueOf(dataStart));
            statement.setTimestamp(4, Timestamp.valueOf(dataEnd));

            try (ResultSet rez = statement.executeQuery()) {
                while (rez.next()) {
                    String dataRez = rez.getString("dt");
                    String tipTranz = rez.getString("tip");
                    String descriere = rez.getString("descriere");
                    Integer idSursa = rez.getObject("sursa", Integer.class);
                    Integer idDest = rez.getObject("dest", Integer.class);
                    int idSuma = rez.getInt("suma_id");
                    int idFactura = rez.getInt("factura_id");

                    String linie = String.format("%s | %-10s | %-30s | src=%s | dest=%s | sumaId=%d | facturaId=%d", dataRez, tipTranz, descriere, idSursa != null ? idSursa.toString() : "-", idDest != null ? idDest.toString() : "-", idSuma, idFactura
                    );
                    linii.add(linie);                }
            }
        }
        return linii;
    }

    public List<Integer> getPendingTransactionIds(int idCont) throws SQLException {
        List<Integer> lista = new ArrayList<>();
        String query = "SELECT id FROM Tranzactie WHERE cont_sursa_id = ? AND status = 'ASTEPTARE'";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setInt(1, idCont);
            ResultSet rez = statement.executeQuery();
            while (rez.next()) lista.add(rez.getInt("id"));
        }
        return lista;
    }

    public void updateStatus(int idTranz, StatusTranzactie status) throws SQLException {
        String query = "UPDATE Tranzactie SET status = ? WHERE id = ?";
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setString(1, status.name());
            statement.setInt(2, idTranz);
        }
    }
    public void executaTranzactieBD(int idTranz) throws SQLException {
        String query = "SELECT cont_sursa_id, cont_destinatie_id, suma_id FROM Tranzactie WHERE id = ?";
        int idSursa, idDest = 0, idSuma;
        try (Connection conex = DbConnection.getConnection();
             PreparedStatement statement = conex.prepareStatement(query)) {
            statement.setInt(1, idTranz);
            ResultSet rez = statement.executeQuery();
            if (!rez.next()) return;
            idSursa = rez.getInt("cont_sursa_id");
            Integer dest = (Integer)rez.getObject("cont_destinatie_id");
            if (dest != null) idDest = dest;
            idSuma = rez.getInt("suma_id");
        }

        Bani sumaOriginala = new BaniSQL().getBaniById(idSuma);
        
        ContCurentSQL ccDao = new ContCurentSQL();
        ContEconomiiSQL ceDao = new ContEconomiiSQL();

        Cont contSursa;
        try {
            contSursa = ccDao.getContCurentById(idSursa);
        } catch (Exception e) {
            try {
                contSursa = ceDao.getContEconomiiById(idSursa);
            } catch (Exception e2) {
                throw new SQLException("Nu s-a gasit contul sursa cu ID: " + idSursa);
            }
        }

        Moneda monedaInput = sumaOriginala.getMoneda();
        Moneda monedaContSursa = contSursa.getSold().getMoneda();
        
        float valSursa = sumaOriginala.getValoare();
        if (!monedaInput.equals(monedaContSursa)) {
            double curs = MonedaService.curs(monedaInput, monedaContSursa);
            valSursa *= curs;
        }

        ccDao.updateSold(idSursa, -valSursa);
        if (idDest != 0) {
            Cont contDest;
            try {
                contDest = ccDao.getContCurentById(idDest);
            } catch (Exception e) {
                try {
                    contDest = ceDao.getContEconomiiById(idDest);
                } catch (Exception e2) {
                    throw new SQLException("Nu s-a gasit contul destinatar cu ID: " + idDest);
                }
            }

            Moneda monedaContDest = contDest.getSold().getMoneda();
            
            float valDest = valSursa;
            if (!monedaContSursa.equals(monedaContDest)) {
                double cursFinal = MonedaService.curs(monedaContSursa, monedaContDest);
                valDest *= cursFinal;
            }

            try {
                ccDao.updateSold(idDest, valDest);
            } catch (SQLException e) {
                ceDao.updateSold(idDest, valDest);
            }
        }
    }

}