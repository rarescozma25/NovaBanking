package util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public class LoggerService {
    private static final String FILE_PATH="notificari_log.txt";

    public static void scrieMesaj(String mesaj) {
        String entry = LocalDateTime.now() + " - " + mesaj + System.lineSeparator();
        try (FileOutputStream out = new FileOutputStream(FILE_PATH, true)) {
            out.write(entry.getBytes());
        } catch (IOException e) {
            System.err.println("Nu se poate scrie in fisier: " + e.getMessage());
        }
    }
}
