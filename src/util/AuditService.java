package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditService {
    private static final String FILE_NAME="audit_log.csv";
    private static final String HEADER="Actiunile din sistem!";

    public static void log(String actiune, String detalii) {
        File file=new File(FILE_NAME);
        try (FileWriter fw=new FileWriter(file, true)) {
            if (file.length()==0) {
                fw.write(HEADER+"\n");
            }
            String ts=LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            fw.write(ts + "," +actiune + "," + detalii + "\n");
        } catch (IOException e) {
            System.err.println("Eroare la audit: " + e.getMessage());
        }
    }
}
