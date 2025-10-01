package service;

import model.Tranzactie;

import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Scanner;

public class OtpService {
    private static final SecureRandom rnd = new SecureRandom();
    private static String cod=null;

    public static String genereazaOtp() {
        int otp=100000+rnd.nextInt(900000);
        cod = String.valueOf(otp);
        scrieOtpInFisier(cod);
        return cod;
    }
    private static void scrieOtpInFisier(String cod) {
        try (FileWriter writer = new FileWriter("otp_log.txt", false)) {
            writer.write("Cod OTP: "+cod);
        } catch (IOException e) {
            System.err.println("Eroare la scrierea OTP în fișier: " + e.getMessage());
        }
    }

    public static boolean valideaza(String codIntrodus) {
        return cod != null && cod.equals(codIntrodus);
    }

    public static void stergeCod() {
        cod=null;
        scrieOtpInFisier("");
    }


}
