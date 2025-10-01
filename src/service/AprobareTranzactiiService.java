package service;

import model.Tranzactie;
import util.AuditService;

import java.util.List;
import java.util.Scanner;

public class AprobareTranzactiiService {
    public void aprobareTranzactii(List<Tranzactie> tranzactii){
        tranzactii.stream().filter(t->t.getStatus()==StatusTranzactie.ASTEPTARE).
                forEach(t->{
                    String otp=OtpService.genereazaOtp();
                    System.out.println("Cod OTP trimis. Verifica otp_log.txt");
                    Scanner sc=new Scanner(System.in);
                    System.out.print("Introdu codul OTP: ");
                    String cod=sc.nextLine();
                    if (OtpService.valideaza(cod)) {
                        t.setStatus(StatusTranzactie.APROBATA);
                        t.executaTranzactie();
                        AuditService.log("Tranzacție aprobata", "ID: " + t.getIdTranzactie());
                        System.out.println("Tranzactia a fost executata.");
                    } else {
                        t.setStatus(StatusTranzactie.RESPINSA);
                        AuditService.log("Tranzacție respinsa", "OTP invalid pentru ID: " + t.getIdTranzactie());
                        System.out.println("Cod OTP invalid. Tranzacția a fost respinsa.");
                    }
                    OtpService.stergeCod();
                });
    }
}
