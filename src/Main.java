import app.Meniu;
import app.MeniuDB;
import database.*;
import model.*;
import service.AprobareTranzactiiService;


import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MeniuDB meniu = MeniuDB.getInstance();
        meniu.run();
//          Meniu meniu=Meniu.getInstance();
//          meniu.start();

    }
}




