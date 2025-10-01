package app;
import model.*;
import service.*;
import database.*;
import model.FonduriInsuficienteExceptie;
import util.AuditService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public final class MeniuDB {
    private final Scanner scanner;
    private final AutentificareServiceDB authService;
    private final SessionManager session;
    private final ClientSQL clientDao;
    private final ContCurentSQL contCurentDao;
    private final ContEconomiiSQL contEconomiiDao;
    private final BaniSQL baniDao;
    private final TranzactieSQL tranzactieDao;
    private final FacturaSQL facturaDao;
    private final CardSQL cardDao;
    private final List<PlataFacturaRecurenta> facturiRecurente;
    private LocalDate dataCurenta = LocalDate.now();

    private MeniuDB() {
        scanner = new Scanner(System.in);
        authService = new AutentificareServiceDB();
        session = SessionManager.getInstance();
        clientDao = new ClientSQL();
        contCurentDao = new ContCurentSQL();
        contEconomiiDao = new ContEconomiiSQL();
        baniDao = new BaniSQL();
        tranzactieDao = new TranzactieSQL();
        facturaDao = new FacturaSQL();
        cardDao = new CardSQL();
        facturiRecurente = new ArrayList<>();

    }
    private static final class SINGLETON_HOLDER{
        private static final MeniuDB INSTANCE = new MeniuDB();
    }

    public static MeniuDB getInstance(){
        return MeniuDB.SINGLETON_HOLDER.INSTANCE;
    }

    public void run() {
        while (true) {
            if (!session.esteAutentificat()) {
                System.out.println("1. Login");
                System.out.println("2. Creare cont");
                System.out.println("3. Resetare parola");
                System.out.println("0. Exit");
                System.out.print("Optiunea ta: ");
                String opt = scanner.nextLine().trim();
                switch (opt) {
                    case "1": login(); break;
                    case "2": creareCont(); break;
                    case "3": resetareParola(); break;
                    case "0": System.exit(0); break;
                    default: System.out.println("Optiune invalida.");
                }
            } else {
                afiseazaMeniuPrincipal();
                System.out.print("Optiunea ta: ");
                String opt = scanner.nextLine().trim();
                handleMainOption(opt);
            }
        }
    }

    private void afiseazaMeniuPrincipal() {
        Client c=authService.getClientAutentificat();
        System.out.println("\n=== Meniu Principal (" + c.getNume() + " " + c.getPrenume() + ") ===");
        System.out.println("1. Creare cont bancar");
        System.out.println("2. Interogare sold si total");
        System.out.println("3. Conversie valutara rapida");
        System.out.println("4. Depunere bani");
        System.out.println("5. Retragere bani");
        System.out.println("6. Transfer intre conturi");
        System.out.println("7. Generare extras de cont");
        System.out.println("8. Gestionare Carduri");
        System.out.println("9. Plata factura");
        System.out.println("10. Aplicare dobanda");
        System.out.println("11. Executare plati recurente");
        System.out.println("12. Adauga plata recurenta");
        System.out.println("13. Resetare parola");
        System.out.println("14. Aprobare tranzactii!");
        System.out.println("15. Simuleaza trecerea timpului");
        System.out.println("16. Logout");

    }

    private void handleMainOption(String opt) {
        switch (opt) {
           case "1": creareContBancar(); break;
           case "2": interogareSold(); break;
           case "3": conversieValutara(); break;
           case "4": depunere(); break;
           case "5": retragere(); break;
           case "6": transfer(); break;
           case "7": generareExtras(); break;
           case "8": gestionareCarduri(); break;
           case "9": plataFactura(); break;
          case "10": adaugaDobanda(); break;
          case "11": executaPlatiRecurente(); break;
          case "12": adaugaPlataRecurenta(); break;
          case "13": resetareParola(); break;
          case "14": aprobareTranzactii(); break;
          case "15": trecereTimp(); break;
          case "16": logout(); break;
          default: System.out.println("Optiune invalida.");
        }

    }

    private void creareCont() {
        try {
            System.out.print("Nume: "); String nume = scanner.nextLine().trim();
            System.out.print("Prenume: "); String prenume = scanner.nextLine().trim();
            System.out.print("CNP: "); String cnp = scanner.nextLine().trim();
            System.out.print("Parola: "); String parola = scanner.nextLine().trim();
            System.out.print("Strada: "); String strada = scanner.nextLine().trim();
            System.out.print("Oras: "); String oras = scanner.nextLine().trim();
            System.out.print("Cod postal: "); String codPostal = scanner.nextLine().trim();
            Adresa adresa = new Adresa(strada, oras, codPostal);
            authService.creareCont(nume, prenume, cnp, parola, adresa);
        } catch (Exception e) {
            System.err.println("Eroare la creare cont: " + e.getMessage());
        }
    }

    private void login() {
        System.out.print("CNP: ");
        String cnp = scanner.nextLine().trim();
        System.out.print("Parola: ");
        String parola = scanner.nextLine().trim();
        if (authService.login(cnp, parola)) {
            System.out.println("Bine ai venit, "+session.getClientAutentificat().getNume());
        }
    }

    private void resetareParola() {
        System.out.print("CNP: ");
        String cnp = scanner.nextLine().trim();
        System.out.print("Parola noua: ");
        String parolaNoua = scanner.nextLine().trim();
        if (authService.reseteazaParola(cnp, parolaNoua)) {
            System.out.println("Parola a fost resetata cu succes.");
        } else {
            System.out.println("Resetare parola esuata. Verifica CNP-ul.");
        }
    }



    private void conversieValutara() {
        System.out.print("Suma de convertit: ");
        float suma = Float.parseFloat(scanner.nextLine().trim());
        System.out.print("Moneda sursa (RON, EUR, USD): ");
        Moneda src = Moneda.valueOf(scanner.nextLine().trim());
        System.out.print("Moneda destinatie (RON, EUR, USD): ");
        Moneda dst = Moneda.valueOf(scanner.nextLine().trim());
        double curs=MonedaService.curs(src, dst);
        System.out.printf("Rezultat: %.2f %s%n", suma * curs, dst);

    }

    private void creareContBancar() {
        Client client = session.getClientAutentificat();
        System.out.println("Alege tipul contului: 1) Cont Curent  2) Cont de Economii");
        System.out.print("Optiunea ta: ");
        String categorie = scanner.nextLine().trim();
        if (!categorie.equals("1") && !categorie.equals("2")) return;

        Moneda moneda;
        while (true) {
            System.out.print("Ce moneda folosesti? (RON, EUR, USD): ");
            try {
                moneda = Moneda.valueOf(scanner.nextLine().trim());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida, reincearca.");
            }
        }

        float soldInit;
        System.out.println("Vrei sold initial la deschidere? (1. Da / 2. Nu)");
        System.out.print("Optiunea ta: ");
        String optSold = scanner.nextLine().trim();
        if ("1".equals(optSold)) {
            while (true) {
                System.out.print("Valoare sold (>=0): ");
                try {
                    soldInit = Float.parseFloat(scanner.nextLine().trim());
                    if (soldInit >= 0f) break;
                    System.out.println("Valoare invalida! Trebuie sa fie un numar pozitiv");
                } catch (NumberFormatException ex) {
                    System.out.println("Format invalid.");
                }
            }
        } else if ("2".equals(optSold)) {
            soldInit = 0f;
        } else {
            System.out.println("Optiune invalida! Revenim la meniul principal..");
            return;
        }

        Bani bani = new Bani(soldInit, moneda);
        try {
            baniDao.addBani(bani);
        } catch (SQLException e) {
            System.err.println("Eroare la salvarea soldului: " + e.getMessage());
            return;
        }

        try {
            if ("1".equals(categorie)) {
                System.out.println("Alege tip Cont Curent: Standard, Premium, Platinum");
                System.out.print("Optiunea ta: ");
                TipContCurent tipCurent = TipContCurent.valueOf(scanner.nextLine().trim());

                ContCurent cc = new ContCurent(client, bani, tipCurent);
                int contId = contCurentDao.addContCurent(cc);
                System.out.println("Cont curent creat cu ID: " + contId);
                client.adaugaCont(cc);

            } else {
                System.out.println("Alege tip Cont Economii: Trei_Luni, Sase_Luni, Un_An");
                System.out.print("Optiunea ta: ");
                TipContEconomii tipE = TipContEconomii.valueOf(scanner.nextLine().trim());

                ContEconomii ce = new ContEconomii(client, bani, tipE);
                int ceId = contEconomiiDao.addContEconomii(ce);
                System.out.println("Cont economii creat cu ID: " + ceId);
                client.adaugaCont(ce);
            }
        } catch (Exception e) {
            System.err.println("Eroare la creare cont bancar: " + e.getMessage());
        }
    }

    private void interogareSold() {
        Client c = session.getClientAutentificat();
        System.out.println("--- Solduri individuale pe conturi ---");
        double totalRon = 0.0;        try {
            for (ContCurent cont : contCurentDao.getAllConturi()) {
                if (!cont.getClient().getCnp().equals(c.getCnp()))
                    continue;
                Bani sold = cont.getSold();
                System.out.printf("Cont curent ID %d: %.2f %s%n", cont.getId(), sold.getValoare(), sold.getMoneda());
                double rate = MonedaService.curs(sold.getMoneda(), Moneda.RON);
                if (rate < 0) System.out.println("Eroare la conversie pentru contul ID " + cont.getId());
                else totalRon += sold.getValoare() * rate;
            }
            for (ContEconomii cont : contEconomiiDao.getAllContEconomii()) {
                if (!cont.getClient().getCnp().equals(c.getCnp()))
                    continue;
                Bani sold = cont.getSold();
                System.out.printf("Cont economii ID %d: %.2f %s%n",
                        cont.getId(), sold.getValoare(), sold.getMoneda());
                double rate = MonedaService.curs(sold.getMoneda(), Moneda.RON);
                if (rate < 0) System.out.println("Eroare la conversie pentru cont economii ID " + cont.getId());
                else totalRon += sold.getValoare() * rate;
            }
            System.out.println("---------------------------------------------------------");
            System.out.printf("Sold total (RON): %.2f%n", totalRon);
        } catch (Exception e) {
            System.err.println("Eroare la interogarea soldurilor: " + e.getMessage());
        }
    }

    private Cont selecteazaCont(String prompt) {
        Client client = authService.getClientAutentificat();
        List<Cont> lista = new ArrayList<>();

        try {
            lista.addAll(contCurentDao.getAllConturi().stream().filter(c -> c.getClient().getCnp().equals(client.getCnp())).toList()
            );
            lista.addAll(contEconomiiDao.getAllContEconomii().stream().filter(c -> c.getClient().getCnp().equals(client.getCnp())).toList()
            );
        } catch (Exception e) {
            System.err.println("Eroare la preluarea conturilor: " + e.getMessage());
            return null;
        }

        if (lista.isEmpty()) {
            System.out.println("Nu ai niciun cont deschis.");
            return null;
        }

        for (int i = 0; i < lista.size(); i++) {
            Cont c = lista.get(i);
            int id = getContId(c);
            System.out.printf("%d) ID %d – sold: %.2f %s%n", i + 1, id, c.getSold().getValoare(), c.getSold().getMoneda()
            );
        }

        System.out.print(prompt + " ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= lista.size()) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            System.out.println("Optiune invalida.");
            return null;
        }

        return lista.get(idx);
    }
    private void depunere() {
        Cont cont = selecteazaCont("Alege contul (prin numarul din lista) in care vrei sa depui:");
        if (cont == null) return;
        Moneda moneda;
        while (true) {
            System.out.print("Moneda (RON, EUR, USD): ");
            try {
                moneda = Moneda.valueOf(scanner.nextLine().trim());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida!");
            }
        }

        float valoare;
        System.out.print("Suma de depus: ");
        while (true) {
            try {
                valoare = Float.parseFloat(scanner.nextLine().trim());
                if (valoare >= 0) break;
                System.out.println("Trebuie un numar pozitiv!");
            } catch (NumberFormatException e) {
                System.out.println("Format invalid.");
            }
        }

        Bani suma = new Bani(valoare, moneda);
        try {
            baniDao.addBani(suma);
        } catch (SQLException e) {
            System.err.println("Eroare la salvarea sumei: " + e.getMessage());
            return;
        }

        cont.depune(suma, "Depunere cont ID " + getContId(cont));

        try {
            Depunere tranz = new Depunere(suma, "Depunere cont ID " + getContId(cont), cont);
            int contId = getContId(cont);
            int sumaId;
            try {
                sumaId=baniDao.addBani(suma);
            } catch (SQLException e) {
                System.err.println("Eroare la salvarea sumei: " + e.getMessage());
                return;
            }


            int trId =tranzactieDao.addTranzactie(tranz, null, contId, sumaId, null);
            System.out.println("Tranzactie Depunere ID: " + trId);
        } catch (SQLException e) {
            System.err.println("Eroare la salvarea tranzactiei: " + e.getMessage());
        }

        try {
            int contId = getContId(cont);
            boolean updated = (cont instanceof ContCurent) ? contCurentDao.updateSold(contId, valoare) : contEconomiiDao.updateSold(contId, valoare);
            if (!updated) {
                System.err.println("Eroare la actualizarea soldului în baza de date!");
            }
        } catch (SQLException e) {
            System.err.println("Eroare la actualizarea soldului: " + e.getMessage());
        }

        int contIdFinal = getContId(cont);
        System.out.printf("Sold nou (ID %d): %.2f %s%n", contIdFinal, cont.getSold().getValoare(), cont.getSold().getMoneda());
    }

    private void retragere() {
        Cont cont = selecteazaCont("Alege contul (prin numarul din lista) din care vrei sa retragi:");
        if (cont == null) return;
        if (!(cont instanceof ContCurent)) {
            System.out.println("Nu poti retrage decat dintr-un cont curent!");
            return;
        }
        int contId = getContId(cont);

        List<Card> cards;
        try {
            cards = cardDao.getCardsByContId(contId).stream().filter(c -> !c.verificareBlocat()).toList();
        } catch (SQLException e) {
            System.err.println("Eroare la încărcarea cardurilor: " + e.getMessage());
            return;
        }
        if (cards.isEmpty()) {
            System.out.println("Nu exista card activ asociat contului!");
            return;
        }

        Moneda moneda;
        while (true) {
            System.out.print("Moneda retragere (RON, EUR, USD): ");
            try {
                moneda = Moneda.valueOf(scanner.nextLine().trim());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida!");
            }
        }

        float valoare;
        while (true) {
            System.out.printf("Suma de retras (%s): ", moneda);
            try {
                valoare = Float.parseFloat(scanner.nextLine().trim());
                if (valoare < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Valoare invalida! Trebuie un numar pozitiv.");
            }
        }

        Moneda contMoneda = cont.getSold().getMoneda();
        double curs=MonedaService.curs(moneda, contMoneda);
        if (curs < 0) {
            System.out.println("Eroare la curs de schimb!");
            return;
        }
        float inCont = (float)(valoare * curs);
        float comision = 0f;
        TipContCurent tip=((ContCurent)cont).getTip();
        if (moneda != contMoneda) {
            comision = inCont * tip.getComision();
        }
        float totalScazut = inCont + comision;


        Bani bani = new Bani(totalScazut, contMoneda);
        int sumaId;
        try {
            sumaId = baniDao.addBani(bani);
        } catch (SQLException e) {
            System.err.println("Eroare la salvarea sumei: " + e.getMessage());
            return;
        }


        try {
            Retragere tranz = new Retragere(bani, "Retragere numerar cont ID " + contId, cont);
            int trId = tranzactieDao.addTranzactie(tranz, contId, null, sumaId, null);
            System.out.println("Tranzactie Retragere ID: " + trId);
        } catch (SQLException e) {
            System.err.println("Eroare la salvarea tranzacției: " + e.getMessage());
            return;
        }

        try {
            boolean ok = contCurentDao.updateSold(contId, -totalScazut);
            if (!ok) {
                System.err.println("Eroare la actualizarea soldului în baza de date!");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Eroare la actualizarea soldului: " + e.getMessage());
            return;
        }

        cont.getSold().setValoare(cont.getSold().getValoare() - totalScazut);
        System.out.printf("Retras %.2f %s (comision %.2f %s)%nSold nou (ID %d): %.2f %s%n", valoare, moneda, comision, contMoneda, contId, cont.getSold().getValoare(), contMoneda
        );
    }
    private void transfer() {
        Cont sursa = selecteazaCont("Alege contul (prin numarul din lista) din care vrei sa transferi:");
        if (sursa == null) return;
        if (sursa instanceof ContEconomii) {
            System.out.println("Nu poti transfera dintr-un cont de economii!");
            return;
        }
        int sursaId = getContId(sursa);

        System.out.print("Introdu CNP-ul beneficiarului: ");
        String cnp = scanner.nextLine().trim();
        Client beneficiar;
        try {
            beneficiar = clientDao.getClientByCnp(cnp);
        } catch (SQLException e) {
            System.err.println("Eroare la interogarea clientului: " + e.getMessage());
            return;
        }
        if (beneficiar == null) {
            System.out.println("Client inexistent!");
            return;
        }

        List<Cont> conturiBeneficiar = new ArrayList<>();
        try {
            contCurentDao.getAllConturi().stream().filter(c -> c.getClient().getCnp().equals(cnp)).forEach(conturiBeneficiar::add);
            contEconomiiDao.getAllContEconomii().stream().filter(c -> c.getClient().getCnp().equals(cnp)).forEach(conturiBeneficiar::add);
        } catch (Exception e) {
            System.err.println("Eroare la preluarea conturilor beneficiarului: " + e.getMessage());
            return;
        }

        if (conturiBeneficiar.isEmpty()) {
            System.out.println("Clientul beneficiar nu are conturi!");
            return;
        }

        System.out.println("Conturi ale beneficiarului:");
        for (int i = 0; i < conturiBeneficiar.size(); i++) {
            Cont c = conturiBeneficiar.get(i);
            int id = getContId(c);
            Bani s = c.getSold();
            System.out.printf("%d) ID %d – sold: %.2f %s%n", i + 1, id, s.getValoare(), s.getMoneda());
        }

        System.out.print("Selecteaza contul destinatar (numarul din lista): ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= conturiBeneficiar.size()) throw new NumberFormatException();

        } catch (NumberFormatException e) {
            System.out.println("Optiune invalida.");
            return;
        }
        Cont destinatar = conturiBeneficiar.get(idx);
        int destId = getContId(destinatar);

        Moneda moneda;
        while (true) {
            System.out.print("Moneda pentru transfer (RON, EUR, USD): ");
            try {
                moneda = Moneda.valueOf(scanner.nextLine().trim());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida!");
            }
        }
        float valoare;
        while (true) {
            System.out.printf("Suma de transfer (%s): ", moneda);
            try {
                valoare = Float.parseFloat(scanner.nextLine().trim());
                if (valoare < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Valoare invalida! Trebuie un numar pozitiv.");
            }
        }

        Moneda contMoneda = sursa.getSold().getMoneda();
        double rate = MonedaService.curs(moneda, contMoneda);
        if (rate < 0) {
            System.out.println("Eroare la cursul de schimb!");
            return;
        }
        float inCont = (float)(valoare * rate);
        float comision = 0f;
        if (!moneda.equals(contMoneda)) {
            comision = inCont * ((ContCurent)sursa).getTip().getComision();
        }
        float totalScazut = inCont + comision;
        Bani suma = new Bani(inCont, contMoneda);
        int sumaId;
        try {
            sumaId = baniDao.addBani(suma);
        } catch (SQLException e) {
            System.err.println("Eroare la salvarea sumei: " + e.getMessage());
            return;
        }

        try {
            StatusTranzactie statusTransfer = sursa.transferaDB(suma, "Transfer " + valoare + " " + moneda, destinatar, tranzactieDao, sursaId, destId, sumaId);

            if (statusTransfer == StatusTranzactie.ASTEPTARE) {
                System.out.println("Transferul necesita aprobare manuala datorita sumei mari.");
                System.out.println("Utilizeaza optiunea '14. Aprobare tranzactii!' pentru a aproba.");
                return;
            } else if (statusTransfer == StatusTranzactie.APROBATA) {
                Transfer tranz = new Transfer(suma, "Transfer " + valoare + " " + moneda, sursa, destinatar);
                tranz.setStatus(StatusTranzactie.APROBATA);
                int trId = tranzactieDao.addTranzactie(tranz, sursaId, destId, sumaId, null);
                
                contCurentDao.updateSold(sursaId, -totalScazut);
                if (destinatar instanceof ContCurent) {
                    contCurentDao.updateSold(destId, inCont);
                } else {
                    contEconomiiDao.updateSold(destId, inCont);
                }

                System.out.printf("Transferat %.2f %s din contul ID %d in contul ID %d%n", valoare, moneda, sursaId, destId);
                if (comision > 0) {
                    System.out.printf(" (+%.2f comision %s)%n", comision, contMoneda);
                }
                System.out.println("Tranzactie Transfer ID: " + trId);

                System.out.printf("Sold nou sursa (ID %d): %.2f %s%n", sursaId, sursa.getSold().getValoare(), sursa.getSold().getMoneda());
                System.out.printf("Sold nou destinatie (ID %d): %.2f %s%n", destId, destinatar.getSold().getValoare(), destinatar.getSold().getMoneda());
            }
        } catch (FonduriInsuficienteExceptie e) {
            System.out.println("Fonduri insuficiente!");
        } catch (Exception e) {
            System.err.println("Eroare la executarea transferului: " + e.getMessage());
        }
    }


    private void plataFactura() {
        float comision = 0.005f;
        Cont cont = selecteazaCont("Selecteaza contul din care vrei sa platesti factura!");
        System.out.print("Furnizor: ");
        String furnizor = scanner.nextLine().trim();

        Moneda facturaMoneda;
        while (true) {
            System.out.print("Moneda pentru plata facturii (RON, EUR, USD): ");
            try {
                facturaMoneda = Moneda.valueOf(scanner.nextLine().trim());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida!");
            }
        }

        float valoareFactura;
        while (true) {
            System.out.printf("Suma de platit (%s): ", facturaMoneda);
            try {
                valoareFactura = Float.parseFloat(scanner.nextLine().trim());
                if (valoareFactura < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Valoare invalida!");
            }
        }

        System.out.print("Descriere factura: ");
        String descriere = scanner.nextLine().trim();

        Moneda contMoneda = cont.getSold().getMoneda();
        float valoareInCont = facturaMoneda.equals(contMoneda) ? valoareFactura : (float)(valoareFactura * MonedaService.curs(facturaMoneda, contMoneda) * (1 - comision));

        Factura factura = new Factura(furnizor, new Bani(valoareInCont, contMoneda), descriere);
        try {
            int sumaId = baniDao.addBani(factura.getSuma());
            int facturaId = new FacturaSQL().addFactura(factura);
            Retragere tr = new Retragere(factura.getSuma(), "Plata factura " + furnizor, cont);
            tranzactieDao.addTranzactie(tr, getContId(cont), null, sumaId, facturaId);
            contCurentDao.updateSold(getContId(cont), -valoareInCont);
            cont.getSold().setValoare(cont.getSold().getValoare() - valoareInCont);

            System.out.printf("Factura catre %s de %.2f %s platita.%n", furnizor, valoareFactura, facturaMoneda);
            System.out.printf("Sold ramas: %.2f %s%n", cont.getSold().getValoare(), contMoneda);
        } catch (Exception e) {
            System.out.println("Eroare la plata facturii.");
        }
    }
    private void generareExtras() {
        Cont cont = selecteazaCont("Selecteaza un cont pentru a genera extrasul:");
        if (cont == null) return;
        int contId = getContId(cont);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate, endDate;
        while (true) {
            System.out.print("Data de inceput (yyyy-MM-dd): ");
            try {
                startDate = LocalDate.parse(scanner.nextLine().trim(), fmt);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Format invalid, reincearca.");
            }
        }
        while (true) {
            System.out.print("Data de sfarsit (yyyy-MM-dd): ");
            try {
                endDate = LocalDate.parse(scanner.nextLine().trim(), fmt);
                if (endDate.isBefore(startDate)) {
                    System.out.println("Data de sfarsit trebuie sa fie dupa data de inceput.");
                    continue;
                }
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Format invalid, reincearca.");
            }
        }
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to   = endDate.atTime(23, 59, 59);
        System.out.println("\n--- Extras de cont (ID " + contId + ") intre " + startDate + " si " + endDate + " ---");
        try {
            List<String> lines = tranzactieDao.getExtrasLines(contId, from, to);
            if (lines.isEmpty()) {
                System.out.println("Nu exista tranzactii in acest interval.");
            } else {
                for (String line : lines) {
                    System.out.println(line);
                }
            }
        } catch (SQLException e) {
            System.err.println("Eroare la generarea extrasului: " + e.getMessage());
        }
    }

    private void adaugaDobanda() {
        Client c = authService.getClientAutentificat();
        for (Cont cont : c.getConturi()) {
            if (cont instanceof ContCurent) {
                cont.calculeazaDobandaBD(dataCurenta);
            }
            else if (cont instanceof ContEconomii) {
                cont.calculeazaDobandaBD(dataCurenta);
            }
        }
    }


    private void executaPlatiRecurente() {
        for (PlataFacturaRecurenta p:facturiRecurente) {
            p.executaProgramatBD(dataCurenta);
            AuditService.log("A fost executata plata recurenta in valoare de "+p.getSuma()," pentru clientul cu numele "+p.getContSursa().getCont().getClient().getNume()+" "+p.getContSursa().getCont().getClient().getPrenume());
        }
    }

    private void adaugaPlataRecurenta() {

        Cont cont = selecteazaCont("Selecteaza contul pentru plata recurenta: ");
        Moneda contMoneda = cont.getSold().getMoneda();

        System.out.print("Furnizor: ");
        String furnizor = scanner.nextLine().trim();

        Moneda facturaMoneda;
        while (true) {
            System.out.print("Moneda plata recurenta (RON,EUR,USD): ");
            try {
                facturaMoneda=Moneda.valueOf(scanner.nextLine().trim());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida!");
            }
        }

        float valoareFactura;
        while (true) {
            System.out.printf("Suma plata recurenta (%s): ", facturaMoneda);
            try {
                valoareFactura=Float.parseFloat(scanner.nextLine().trim());
                if (valoareFactura<0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Trebuie introdus un numar pozitiv.");
            }
        }

        System.out.print("Descriere: ");
        String descriere = scanner.nextLine().trim();

        float valoareInCont;
        if (facturaMoneda.equals(contMoneda)) {
            valoareInCont = valoareFactura;
        } else {
            double curs = MonedaService.curs(facturaMoneda, contMoneda);

            valoareInCont = (float)(valoareFactura * curs);
        }

        int frecventa;
        while (true) {
            System.out.print("Frecventa platei (In zile): ");
            try {
                frecventa = Integer.parseInt(scanner.nextLine().trim());
                if (frecventa <= 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Trebuie introdus un numar pozitiv.");
            }
        }

        LocalDate dataStart;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print("Data start (yyyy-MM-dd): ");
            try {
                dataStart=LocalDate.parse(scanner.nextLine().trim(), fmt);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Format invalid.");
            }
        }

        Factura factura=new Factura(furnizor, new Bani(valoareInCont, contMoneda), descriere);
        PlataFacturaRecurenta p = new PlataFacturaRecurenta(cont, factura, frecventa, dataStart);
        facturiRecurente.add(p);

        System.out.println("Factura recuranta adaugata pentru contul cu IBAN-ul: " +cont.getIban());
    }

    private void aprobareTranzactii() {
        Cont cont = selecteazaCont("Selecteaza contul pentru aprobarea tranzactiilor:");
        if (cont == null) return;
        int contId = getContId(cont);
        new AprobareTranzactiiServiceDB().aprobareTranzactii(contId);
    }

    private int getContId(Cont cont) {
        if (cont instanceof ContCurent) {
            return ((ContCurent) cont).getId();
        } else if (cont instanceof ContEconomii) {
            return ((ContEconomii) cont).getId();
        } else {
            throw new IllegalArgumentException("Tip cont necunoscut: " + cont.getClass());
        }
    }


    private void gestionareCarduri() {
        System.out.println("\n--- Gestionare Carduri ---");
        System.out.println("1. Emite card nou");
        System.out.println("2. Listeaza carduri");
        System.out.println("3. Blocheaza card");
        System.out.println("4. Deblocheaza card");
        System.out.println("5. Stergere card");
        System.out.println("0. Inapoi");
        System.out.print("Optiune: ");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1": emiteCard(); break;
            case "2": listeazaCarduri(); break;
            case "3": schimbaStareCard(true); break;
            case "4": schimbaStareCard(false); break;
            case "5": stergeCard(); break;
            case "0": return;
            default: System.out.println("Optiune invalida.");
        }
    }

    private void emiteCard() {
        Cont cont = selecteazaCont("Selecteaza un cont pentru emiterea unui card:");
        if (cont == null) return;
        if (!(cont instanceof ContCurent)) {
            System.out.println("Cardurile se pot emite doar pe cont curent.");
            return;
        }
        int contId = getContId(cont);

        TipCard tip;
        while (true) {
            System.out.print("Tip card (VISA, MASTERCARD, AMEX, MAESTRO): ");
            try {
                tip = TipCard.valueOf(scanner.nextLine().trim().toUpperCase());
                break;            } catch (IllegalArgumentException e) {
                System.out.println("Tip invalid, reincearca.");
            }
        }

        Card card = new Card(cont, tip);
        AuditService.log("Card emis pentru contul cu IBAN-ul "+cont.getIban()," Pe acest cont detineti "+cont.getCarduri().size()+" carduri");
        try {
            int cardId = cardDao.addCard(card, contId);
            card.setId(cardId);
            System.out.println("Card emis: " + card.getNumarCard() + " | CVV=" + card.getCvv() + " | Expirare=" + card.getDataExpirare());
        } catch (SQLException e) {
            System.err.println("Eroare la emiterea cardului: " + e.getMessage());
        }
    }

    private void listeazaCarduri() {
        Cont cont = selecteazaCont("Selecteaza un cont pentru listarea cardurilor:");
        if (cont == null) return;
        int contId = getContId(cont);

        System.out.println("---------- Carduri ----------");
        try {
            List<Card> cards = cardDao.getCardsByContId(contId);
            if (cards.isEmpty()) {
                System.out.println("Niciun card emis pentru acest cont.");
                return;
            }
            cards.sort(Card::compareTo);
            int idx = 1;
            for (Card card : cards) {
                System.out.printf("%d) %s - Expira: %s - %s%n", idx++, card.getNumarCard(), card.getDataExpirare(), card.verificareBlocat() ? "Blocat" : "Activ");
            }
        } catch (SQLException e) {
            System.err.println("Eroare la listarea cardurilor: " + e.getMessage());
        }
    }
    private void logout(){
        authService.logout();
    }




    private void schimbaStareCard(boolean blocare) {
        Cont cont = selecteazaCont("Selecteaza cont pentru card:");
        if (cont == null) return;
        int contId = getContId(cont);

        List<Card> cards;
        try {
            cards = cardDao.getCardsByContId(contId);
        } catch (SQLException e) {
            System.err.println("Eroare la preluarea cardurilor: " + e.getMessage());
            return;
        }
        if (cards.isEmpty()) {
            System.out.println("Niciun card de gestionat.");
            return;
        }

        int idx = 1;
        for (Card card : cards) {
            System.out.printf("%d) %s - %s%n", idx++, card.getNumarCard(), card.verificareBlocat() ? "Blocat" : "Activ");
        }
        System.out.print("Alege index card: ");
        int sel;
        try {
            sel=Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (sel < 0 || sel >= cards.size()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Index invalid.");
            return;
        }
        Card card = cards.get(sel);

        try {
            boolean ok = cardDao.setBlockStatus(card.getNumarCard(), blocare);
            if (ok) {
                if (blocare){
                    System.out.println("Card blocat "+card.getNumarCard());
                    AuditService.log("Card blocat ",card.getNumarCard());

                }
                else{
                    System.out.println("Card deblocat "+card.getNumarCard());
                    AuditService.log("Card deblocat ",card.getNumarCard());
                }

            } else {
                System.out.println("Nu am gasit cardul in baza de date.");
            }
        } catch (SQLException e) {
            System.err.println("Eroare la actualizarea starii cardului: " + e.getMessage());
        }
    }

    private void stergeCard(){
        Cont cont = selecteazaCont("Selecteaza cont pentru card:");
        if (cont == null) return;
        int contId = getContId(cont);

        List<Card> cards;
        try {
            cards = cardDao.getCardsByContId(contId);
        } catch (SQLException e) {
            System.err.println("Eroare la preluarea cardurilor: " + e.getMessage());
            return;
        }
        if (cards.isEmpty()) {
            System.out.println("Niciun card de gestionat.");
            return;
        }

        int idx = 1;
        for (Card card : cards) {
            System.out.printf("%d) %s", idx++, card.getNumarCard());
        }
        System.out.print("Alege index card: ");
        int sel;
        try {
            sel=Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (sel < 0 || sel >= cards.size()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Index invalid.");
            return;
        }
        Card card = cards.get(sel);
        try{
            cardDao.deleteCard(card.getNumarCard());
            System.out.println("Card sters "+card.getNumarCard());
            AuditService.log("Card sters ",card.getNumarCard());
        } catch (SQLException e) {
            System.err.println("Eroare la stergerea cardului: " + e.getMessage());
        }

    }

    private void trecereTimp(){
        System.out.print("Cu cate luni vrei sa avansezi data? ");
        int luni=Integer.parseInt(scanner.nextLine());
        dataCurenta=dataCurenta.plusMonths(luni);
        System.out.println("Data curenta simulata este: " + dataCurenta);


    }

}
