package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ParolaUtils {

    public static String hashParola(String parola) { //criptare de parola
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(parola.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash)
                hexString.append(String.format("%02x", b));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritm SHA-256 indisponibil", e);
        }
    }
}
