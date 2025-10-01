package model;

import database.ContCurentSQL;
import database.ContEconomiiSQL;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class PlataFacturaRecurenta extends PlataFactura {
    private final int frecventaZile;
    private final LocalDate dataStart;
    private LocalDate ultimaExecutie;

    public PlataFacturaRecurenta(Cont contSursa, Factura factura, int frecventaZile, LocalDate dataStart) {
        super(contSursa, factura);
        this.frecventaZile = frecventaZile;
        this.dataStart = dataStart;
        this.ultimaExecutie = null;
    }

    public boolean trebuieExecutatIn(LocalDate dataSimulata) {
        if (ultimaExecutie==null) {
            return !dataSimulata.isBefore(dataStart);
        }
        long zile=ChronoUnit.DAYS.between(ultimaExecutie,dataSimulata);
        return zile>=frecventaZile;
    }

    public void executaProgramat(LocalDate dataSimulata) {
        if (trebuieExecutatIn(dataSimulata)) {
            super.getContSursa().platesteFactura(super.getFactura());
            this.ultimaExecutie = LocalDate.now();
            System.out.println("Plata programata executata automat.");
        } else {
            System.out.println("Plata nu este inca scadenta.");
        }
    }

    public void executaProgramatBD(LocalDate dataSimulata) {
        if (trebuieExecutatIn(dataSimulata)) {
            super.getContSursa().platesteFactura(super.getFactura());
            
            Cont cont = super.getContSursa();
            float sumaScazuta =-(super.getFactura().getSuma().getValoare());

            try {
                if (cont instanceof ContCurent) {
                    int contId = ((ContCurent) cont).getId();
                    new ContCurentSQL().updateSold(contId, sumaScazuta);
                }
                else if (cont instanceof ContEconomii) {
                    int contId = ((ContEconomii) cont).getId();
                    new ContEconomiiSQL().updateSold(contId, sumaScazuta);
                }
            } catch (SQLException ex) {
                System.err.println("Eroare la actualizarea soldului în BD: " + ex.getMessage());
                return;
            }

            this.ultimaExecutie = dataSimulata;
            System.out.println("Plata programata executata automat la " + dataSimulata);
        }
        else {
            System.out.println("Plata nu este inca scadenta.");
        }
    }


}
