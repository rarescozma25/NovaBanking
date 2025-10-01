package service;

import database.TranzactieSQL;
import util.AuditService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AprobareTranzactiiServiceDB {

    private final TranzactieSQL tranzDao = new TranzactieSQL();

    public void aprobareTranzactii(int contId) {
        Scanner sc=new Scanner(System.in);
        try {
            List<Integer> pendingIds = tranzDao.getPendingTransactionIds(contId);
            for (int txId : pendingIds) {
                String otp = OtpService.genereazaOtp();
                System.out.println("Cod OTP trimis. Verifica otp_log.txt");
                System.out.print("Introdu codul OTP pentru tranzactie " + txId + ": ");
                boolean aprobat = OtpService.valideaza(sc.nextLine().trim());

                StatusTranzactie nouStatus = aprobat ? StatusTranzactie.APROBATA : StatusTranzactie.RESPINSA;

                tranzDao.updateStatus(txId, nouStatus);
                if (aprobat) {
                    tranzDao.executaTranzactieBD(txId);
                    System.out.println("Tranzactia " + txId + " a fost aprobata si executata.");
                    AuditService.log("Tranzactie aprobata", "ID: " + txId);
                } else {
                    System.out.println("OTP invalid. Tranzactia " + txId + " a fost respinsa.");
                    AuditService.log("Tranzactie respinsa", "ID: " + txId);
                }
                OtpService.stergeCod();
            }
        } catch (SQLException e) {
            System.err.println("Eroare BD: " + e.getMessage());
        }
    }
}
