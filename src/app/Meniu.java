package app;

import model.*;
import service.AprobareTranzactiiService;
import service.AutentificareService;
import service.MonedaService;
import service.OtpService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public final class Meniu {
    private final AutentificareService authService;
    private final Scanner scanner;
    private final List<PlataFacturaRecurenta> facturiRecurente;
    private LocalDate dataCurenta = LocalDate.now();
    private final AprobareTranzactiiService aprobareTranzactiiService;


    private Meniu(){
        this.authService = new AutentificareService();
        this.scanner = new Scanner(System.in);
        this.facturiRecurente = new ArrayList<>();
        this.aprobareTranzactiiService = new AprobareTranzactiiService();
    }

    private static final class SINGLETON_HOLDER{
        private static final Meniu INSTANCE = new Meniu();
    }

    public static Meniu getInstance(){
        return SINGLETON_HOLDER.INSTANCE;
    }

    public void start() {
        while (true) {
            if (!authService.esteAutentificat()) {
                afiseazaMeniuAuth();
                handleAuthOption(scanner.nextLine());
            } else {
                afiseazaMeniuPrincipal();
                handleMainOption(scanner.nextLine());
            }
        }
    }

    private void afiseazaMeniuAuth() {
        System.out.println("\n===Autentificare/Inregistrare===");
        System.out.println("1.Creare cont");
        System.out.println("2.Login");
        System.out.println("3.Resetare parolă");
        System.out.println("0.Ieșire");
        System.out.print("Opțiune: ");
    }

    private void handleAuthOption(String opt) {
        switch (opt) {
            case "1": creareCont(); break;
            case "2": login(); break;
            case "3": resetareParolaAuth(); break;
            case "0": System.exit(0); break;
            default: System.out.println("Optiune invalida.");
        }
    }

    private void afiseazaMeniuPrincipal() {
        Client c=authService.getClientAutentificat();
        System.out.println("\n=== Meniu Principal (" + c.getNume() + " " + c.getPrenume() + ") ===");
        System.out.println("1. Creare cont bancar");
        System.out.println("2. Interogare sold și total");
        System.out.println("3. Conversie valutară rapidă");
        System.out.println("4. Depunere bani");
        System.out.println("5. Retragere bani");
        System.out.println("6. Transfer între conturi");
        System.out.println("7. Generare extras de cont");
        System.out.println("8. Gestionare Carduri");
        System.out.println("9. Plata factura");
        System.out.println("10. Aplicare dobânda");
        System.out.println("11. Executare plati recurente");
        System.out.println("12. Adauga plata recurenta");
        System.out.println("13. Resetare parola");
        System.out.println("14. Aprobare tranzactii!");
        System.out.println("15. Simuleaza trecerea timpului");
        System.out.println("16. Logout");

        System.out.print("Optiune: ");
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
            case "13": resetareParolaAuth(); break;
            case "14": aprobareTranzactii(); break;
            case "15": trecereTimp(); break;
            case "16": logout(); break;
            default: System.out.println("Opțiune invalida.");
        }
    }

    private void creareCont() {
        System.out.print("Nume: "); String nume = scanner.nextLine();
        System.out.print("Prenume: "); String prenume = scanner.nextLine();
        System.out.print("CNP: "); String cnp = scanner.nextLine();
        System.out.print("Parola: "); String parola = scanner.nextLine();
        System.out.print("Strada: "); String strada = scanner.nextLine();
        System.out.print("Oras: "); String oras = scanner.nextLine();
        System.out.print("Cod postal: "); String codPostal = scanner.nextLine();
        Adresa adresa = new Adresa(strada, oras, codPostal);
        authService.creareCont(nume, prenume, cnp, parola, adresa);
    }

    private void login() {
        System.out.print("CNP: "); String cnp = scanner.nextLine();
        System.out.print("Parola: "); String parola = scanner.nextLine();
        authService.login(cnp, parola);
    }

    private void resetareParolaAuth() {
        System.out.print("CNP pentru resetare: ");
        String cnp=scanner.nextLine();
        System.out.print("Introdu noua parola: ");
        String parola_noua= scanner.nextLine();
        OtpService.genereazaOtp();
        System.out.println("Cod OTP generat și scris în otp_log.txt.");
        System.out.print("Introdu codul OTP: ");
        String codIntrodus = scanner.nextLine();

        if (OtpService.valideaza(codIntrodus)) {
            boolean ok=authService.reseteazaParola(cnp, parola_noua);
            System.out.println(ok ? "Parola ta a fost resetata cu succes!" : "Eroare la resetarea parolei.");
            OtpService.stergeCod();
        } else {
            System.out.println("OTP invalid!");
        }

    }
    private void logout(){
        authService.logout();
    }

    private void creareContBancar() {
        Client client = authService.getClientAutentificat();
        System.out.println("Alege tipul contului: 1) Cont Curent    2) Cont de Economii");
        System.out.print("Opțiunea ta: ");
        String categorie = scanner.nextLine();
        if(!categorie.equals("1") && !categorie.equals("2")) return;
        Moneda moneda;
        while (true) {
            System.out.print("Ce moneda folosesti? (RON, EUR, USD): ");
            try {
                moneda = Moneda.valueOf(scanner.nextLine());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Monedă invalidă, reîncearcă.");
            }
        }

        float soldInit;
        System.out.println("Vrei sold initial la deschidere? (1. Da / 2. Nu)");
        System.out.print("Optiunea ta: ");
        String optSold=scanner.nextLine().trim();
        if ("1".equals(optSold)) {
            while (true) {
                System.out.print("Valoare sold (>=0): ");
                try {
                    soldInit = Float.parseFloat(scanner.nextLine().trim());
                    if (soldInit >= 0f) break;
                    System.out.println("Valorea invalida! Trebuie sa fie un numar pozitiv");
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

        Cont cont;
        if ("1".equals(categorie)) {
            System.out.println("Alege tip Cont Curent: Standard, Premium, Platinum");
            System.out.print("Opțiunea ta: ");
            TipContCurent tipCurent = TipContCurent.valueOf(scanner.nextLine());
            cont = new ContCurent(client, new Bani(soldInit, moneda), tipCurent);

        } else if ("2".equals(categorie)) {
            System.out.println("Alege tip Cont Economii: Trei_Luni, Sase_Luni, Un_An");
            System.out.print("Opțiunea ta: ");
            TipContEconomii tipE = TipContEconomii.valueOf(scanner.nextLine());
            cont = new ContEconomii(client, new Bani(soldInit, moneda), tipE);

        } else {
            System.out.println("Categorie cont invalida!");
            return;
        }

        client.adaugaCont(cont);
        System.out.println("Cont creat cu IBAN-ul: " + cont.getIban()
                + " pentru clientul " + client.getNume() + " " + client.getPrenume());
    }

    private void interogareSold() {
        Client c = authService.getClientAutentificat();
        System.out.println("--- Solduri individuale pe conturi ---");
        double totalRon = 0.0;
        for (Cont cont : c.getConturi()) {
            Bani sold = cont.getSold();
            System.out.printf("Cont IBAN %s: %.2f %s%n", cont.getIban(), sold.getValoare(), sold.getMoneda());
            double rate = MonedaService.curs(sold.getMoneda(), Moneda.RON);
            if (rate < 0) {
                System.out.println("Eroare la conversie pentru contul " + cont.getIban());
            } else {
                totalRon+=sold.getValoare()*rate;
            }
        }
        System.out.println("---------------------------------------------------------");
        System.out.println("Sold total (RON): "+ totalRon);
    }
    private void conversieValutara() {
        System.out.print("Sumă de convertit: ");
        float suma = Float.parseFloat(scanner.nextLine().trim());
        System.out.print("Moneda sursa (RON, EUR, USD): ");
        Moneda src = Moneda.valueOf(scanner.nextLine().trim());
        System.out.print("Monedă destinație (RON, EUR, USD): ");
        Moneda dst = Moneda.valueOf(scanner.nextLine().trim());
        double curs=MonedaService.curs(src, dst);
        System.out.printf("Rezultat: %.2f %s%n", suma * curs, dst);

    }

    private void depunere(){
        Cont cont=selecteazaCont("Alege contul in care vrei sa depui!");
        Moneda moneda;
        while(true){
            System.out.println("Moneda pe care vrei sa o folosesti la depunere (RON,EUR,USD)");
            try {
                moneda = Moneda.valueOf(scanner.nextLine().trim());
                break;
            }catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida!");
            }
        }
        float valoare;
        System.out.println("Ce suma doresti sa depui?");
        while (true) {
            try {
                valoare = Float.parseFloat(scanner.nextLine().trim());
                if (valoare >= 0) break;
                System.out.println("Valoare invalida. Suma depusa trebuie sa fie pozitiva.");
            } catch (NumberFormatException e) {
                System.out.println("Format invalid. Trebuie introdus un numar.");
            }
        }

        Bani suma_depunere=new Bani(valoare,moneda);
        cont.depune(suma_depunere, "Depunere in contul cu IBAN-ul "+cont.getIban());

        System.out.printf("Sold actualizat (%s): %.2f %s%n",
                cont.getIban(),
                cont.getSold().getValoare(),
                cont.getSold().getMoneda());
    }

    private void retragere() {
        Cont cont = selecteazaCont("Alege contul din care vrei sa retragi!");
        if (cont instanceof ContEconomii) {
            System.out.println("Nu poti retrage dintr-un cont de economii!");
            return;
        }
        Moneda moneda;
        while (true) {
            System.out.print("Moneda retragere(RON,EUR,USD): ");
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
                valoare=Float.parseFloat(scanner.nextLine().trim());
                if (valoare < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Valoare invalida! Trebuie introdus un numar pozitiv.");
            }
        }
        Moneda contMoneda=cont.getSold().getMoneda();
        float curs=1;
        if (!moneda.equals(contMoneda)) {
            curs=(float) MonedaService.curs(moneda, contMoneda);
        }
        float inCont=valoare*curs;
        float comision=0f;
        if (moneda != contMoneda) {
            comision=inCont*((ContCurent)cont).getTip().getComision();
        }
        float total=inCont+comision;
        try {
            cont.retrage(new Bani(total, contMoneda), "Retragere numerar din contul cu IBAN-ul "+ cont.getIban());
            System.out.printf("Retras %.2f %s", valoare, moneda);
            if (comision>0) {
                System.out.printf(" + comision %.2f %s", comision, contMoneda);
            }
        } catch (FonduriInsuficienteExceptie e) {
            System.out.println("Fonduri insuficiente!");
        }
          catch(RuntimeException r){
              System.out.println("Nu exista un card/ nu exista un card activ asociat contului din care vrei sa retragi!");
          }
    }

    private void transfer(){
        Cont sursa=selecteazaCont("Alege contul din care vrei sa transferi: ");
        if(sursa instanceof ContEconomii){
            System.out.println("Nu poti transfera dintr-un cont de economii!");
            return;
        }

        System.out.println("Introdu CNP-ul beneficiarului: ");
        String cnp=scanner.nextLine().trim();
        Client beneficiar=authService.getClient(cnp);
        if(beneficiar==null){
            System.out.println("Client indexistent!");
            return;
        }

        List <Cont> conturiBeneficiar=new ArrayList<>(beneficiar.getConturi());
        if(conturiBeneficiar.isEmpty()){
            System.out.println("Cleintul caruia vrei sa-i transferi nu are conturi asociate!");
            return;
        }
        for (int i = 0; i < conturiBeneficiar.size(); i++) {
            Cont c = conturiBeneficiar.get(i);
            System.out.printf("%d) %s (%.2f %s)%n",
                    i+1, c.getIban(), c.getSold().getValoare(), c.getSold().getMoneda());
        }

        System.out.println("Selecteaza contul destinatarului: ");
        int index=Integer.parseInt(scanner.nextLine().trim())-1;
        Cont destinatar;
        while(true) {
             destinatar = conturiBeneficiar.get(index);
             if(destinatar.getIban().equals(sursa.getIban())){
                 System.out.println("Nu poti transfera dintr-un cont in acelasi cont.");
             }
             else {
                 break;
             }

        }

        Moneda moneda;

        while(true){
            System.out.println("Moneda pentru transfer (EUR,RON,USD)");
            try{
                moneda=Moneda.valueOf(scanner.nextLine().trim());
                break;
            }catch(IllegalArgumentException e){
                System.out.println("Moneda invalida!");
            }
        }
        float valoare;
        while(true){
            System.out.printf("Sumă de transfer (%s): ", moneda);
            try{
                valoare=Float.parseFloat(scanner.nextLine().trim());
                if(valoare<0) throw new NumberFormatException();
                break;
            }catch(NumberFormatException e){
                System.out.println("Valoare invalida! Trebuie un numar pozitiv!");
            }
        }

        float comision=0.005f;
        Moneda contMoneda = sursa.getSold().getMoneda();
        float valoare_transfer;
        if(moneda.equals(contMoneda)){
            valoare_transfer=valoare;
        }
        else{
            double curs=MonedaService.curs(moneda,contMoneda);
            double curs_comision=curs*(1-comision);
            valoare_transfer=(float)(valoare*curs_comision);
        }
        Bani suma=new Bani(valoare_transfer,contMoneda);

        try{
            sursa.transfera(suma,"Transfer catre "+destinatar.getIban(),destinatar);
            System.out.printf("Ai transferat %.2f %s către %s.%n",
                    valoare, moneda, destinatar.getIban());
            System.out.printf("Sold rămas: %.2f %s%n",
                    sursa.getSold().getValoare(), contMoneda);

        }
        catch (FonduriInsuficienteExceptie e) {
            System.out.println("Fonduri insuficiente!");
        }

    }


    private void plataFactura() {
        float comision = 0.005f;
        Cont cont = selecteazaCont("Selecteaza contul din care vrei sa platesti factura! ");

        System.out.print("Furnizor: ");
        String furnizor = scanner.nextLine().trim();
        Moneda facturaMoneda;
        while (true) {
            System.out.print("Moneda utilizata pentru plata facturii (RON,EUR,USD): ");
            try {
                facturaMoneda= Moneda.valueOf(scanner.nextLine().trim());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Moneda invalida!");
            }
        }
        float valoareFactura;
        while (true) {
            System.out.printf("Suma de platit (%s): ", facturaMoneda);
            try {
                valoareFactura=Float.parseFloat(scanner.nextLine().trim());
                if (valoareFactura < 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Valoare invalida! Trebuie introdus un numar pozitiv.");
            }
        }
        System.out.print("Descriere factură: ");
        String descriere = scanner.nextLine().trim();

        Moneda contMoneda=cont.getSold().getMoneda();
        float valoareInCont;
        if (facturaMoneda.equals(contMoneda)) {
            valoareInCont=valoareFactura;
        } else {
            double cursOficial = MonedaService.curs(facturaMoneda, contMoneda);
                        double curs_comision = cursOficial * (1 - comision);
            valoareInCont = (float)(valoareFactura * curs_comision);
        }

        Factura factura=new Factura(
                furnizor,
                new Bani(valoareInCont, contMoneda),
                descriere
        );

        try {
            cont.platesteFactura(factura);
            System.out.printf("Factura catre %s de %.2f %s a fost platita cu succes.%n",
                    furnizor, valoareFactura, facturaMoneda);
            System.out.printf("Sold ramas: %.2f %s%n",
                    cont.getSold().getValoare(), contMoneda);
        } catch (FonduriInsuficienteExceptie e) {
            System.out.println("Fonduri insuficiente pentru plata facturii.");
        }
    }


    private void generareExtras() {
        Cont cont = selecteazaCont("Selecteaza un cont pentru a genera extrasul: ");
        
        DateTimeFormatter format=DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate, endDate;
        while (true) {
            System.out.print("Data de inceput(yyyy-MM-dd): ");
            try {
                startDate = LocalDate.parse(scanner.nextLine().trim(), format);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Format invalid..");
            }
        }
        while (true) {
            System.out.print("Data de sfarsit(yyyy-MM-dd): ");
            try {
                endDate=LocalDate.parse(scanner.nextLine().trim(), format);
                if (endDate.isBefore(startDate)) {
                    System.out.println("Trebuie sa fie o data aflata dupa data de inceput.");
                    continue;
                }
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Format invalid, reîncearcă.");
            }
        }

        LocalDateTime start=startDate.atStartOfDay();
        LocalDateTime end=endDate.atTime(23, 59, 59);

        System.out.println("\n--- Extras de cont între "
                + startDate + " și " + endDate + " ---");
        cont.afiseazaExtras(start,end);
    }

    private void adaugaDobanda() {
        Client c=authService.getClientAutentificat();
        for (Cont cont:c.getConturi()) {
            cont.calculeazaDobanda(dataCurenta);
        }
    }

    private void executaPlatiRecurente() {
        for (PlataFacturaRecurenta p:facturiRecurente) {
            p.executaProgramat(dataCurenta);
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
                dataStart = LocalDate.parse(scanner.nextLine().trim(), fmt);
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



    public void aprobareTranzactii(){
        Cont cont = selecteazaCont("Selecteaza contul pentru aprobarea tranzactiilor: ");
        aprobareTranzactiiService.aprobareTranzactii(cont.gettranzactii());

    }

    private void gestionareCarduri() {
        System.out.println("\n--- Gestionare Carduri ---");
        System.out.println("1. Emite card nou");
        System.out.println("2. Listeaza carduri");
        System.out.println("3. Blocheaza card");
        System.out.println("4. Deblocheaza card");
        System.out.println("0. Inapoi");
        System.out.print("Opțiune: ");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1": emiteCard();break;
            case "2": {
                Cont cont = selecteazaCont("Selecteaza un cont pentru listarea cardurilor: ");
                listeazaCarduri(cont);
                break;
            }
            case "3": schimbaStareCard(true);break;
            case "4": schimbaStareCard(false);break;
            case "0": return;
            default: System.out.println("Optiune invalida.");
        }
    }

    private void emiteCard() {
        Cont cont=selecteazaCont("Selecteaza un cont pentru emiterea unui card: ");
        TipCard tip;
        while (true) {
            System.out.print("Tip card (VISA, MASTERCARD, AMEX, MAESTRO): ");
            try {
                tip = TipCard.valueOf(scanner.nextLine().trim().toUpperCase());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Nu exista acest tip de card!.");
            }
        }
        Card card=new Card(cont, tip);
        cont.adaugaCard(card);
        System.out.println("Card emis: " + card + ", CVV=" + card.getCvv());
    }

    private void listeazaCarduri(Cont cont) {
        List<Card> cards = cont.getCarduri();
        cards.sort(Card::compareTo);
        if (cards.isEmpty()) {
            System.out.println("Niciun card emis pentru acest cont.");
            return;
        }
        System.out.println("---------- Carduri ----------");
        int idx = 1;
        for (Card card : cards) {
            System.out.printf("%d) %s - Expira: %s - %s%n",
                    idx++, card.getNumarCard(), card.getDataExpirare(),
                    card.verificareBlocat() ? "Blocat" : "Activ");
        }
    }

    private void schimbaStareCard(boolean blocare) {
        Cont cont = selecteazaCont("Selectează cont pentru card: ");
        List<Card> cards = cont.getCarduri();
        if (cards.isEmpty()) {
            System.out.println("Niciun card de gestionat.");
            return;
        }
        listeazaCarduri(cont);
        System.out.print("Alege index card: ");
        int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
        Card card = cards.get(idx);
        if (blocare) card.blocheazaCard(); else card.deblocheazaCard();
        System.out.println((blocare ? "Card blocat:" : "Card deblocat:") + " " + card.getNumarCard());
    }

    private Cont selecteazaCont(String prompt) {
        Client client = authService.getClientAutentificat();
        List<Cont> conturi = new ArrayList<>(client.getConturi());
        for (int i = 0; i < conturi.size(); i++) {
            System.out.printf("%d) %s (%s %.2f)%n",
                    i+1, conturi.get(i).getIban(),
                    conturi.get(i).getSold().getMoneda(), conturi.get(i).getSold().getValoare());
        }
        System.out.print(prompt);
        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        return conturi.get(index);
    }

    private void trecereTimp(){
        System.out.print("Cu cate luni vrei sa avansezi data? ");
        int luni=Integer.parseInt(scanner.nextLine());
        dataCurenta=dataCurenta.plusMonths(luni);
        System.out.println("Data curenta simulata este: " + dataCurenta);
    }




}





