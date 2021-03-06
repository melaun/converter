/**
 * Converter slouzi ke stazení dokumentu a prevodu na format prijatelny pri program boss
 * skláda se z modulu downloader a converter
 */
package program;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import converter.Dodavatel;
import converter.CSV;
import converter.ConverterManager;
import converter.DBF;
import converter.document.Document;
import converter.PDFPac;
import converter.TXT;
import converter.document.Row;
import downloader.DownloadManager;
import downloader.EmailConnector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Podzimek Vojtěch
 */
public class Converter {

    private final String milin = "13";
    private final String nemocnice = "11";
    private final String pec = "18";
    private final String kozarovice = "19";
    private final String zdice = "16";
    private final String tocnik = "09";
    private final String hut = "17";
    private final String cashPB = "01";
    private final String vo = "31";
    private final String jablonna = "25";
    private final String milesov = "32";
    private final String trebsko = "28";
    /**
     *
     */
    private ConverterManager cm = null;
    /**
     * Inicializace logeru ConverterLog
     */
    private final Logger log = Logger.getLogger("ConverterLog");
    /**
     *
     */
    private FileHandler fh;
    /**
     * Vychozi cesta k logu
     */
    private String defaultLogPath = System.getProperty("user.home") + File.separator + "JavaConverter" + File.separator;
    /**
     * Výchozi cesta pro ukladani dokladu od dodavatele
     */
    private String homePath = System.getProperty("user.home") + File.separator + "JavaConverter" + File.separator;
    /**
     * Výchozi cesta pro konvertovane doklady
     */

    private String savePath = "P:\\" + File.separator + "prijem" + File.separator;
    /**
     * cesta pro vzdalené ukládání nových dokladů je to kopie savePath ale
     * nedoplnuje se o smazane
     */
    private String externalPath = "";
    /**
     * výchozí velikost pro soubor logu 1MB
     */
    private static final int FILE_SIZE = 1024 * 1024;
    /**
     * list s dodavateli
     */
    private ArrayList<Dodavatel> dodavatele = new ArrayList<>();

    /**
     * vytvori Converter
     */
    public Converter() {
        if (!new File(homePath + "config.properties").exists()) {
            //make default file
            createDefaultConfigFile();
        }
        loadConfig();

    }

    /**
     * načte nastavení z config file
     */
    private void loadConfig() {
        FileInputStream input = null;
        try {
            input = new FileInputStream(homePath + "config.properties");
            Properties prop = new Properties();
            prop.load(input);
            savePath = prop.getProperty("savePath");
            externalPath = prop.getProperty("extraSavePath");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * vytvori vychozi config file
     */
    private void createDefaultConfigFile() {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            File file = new File(homePath + "config.properties");
            output = new FileOutputStream(homePath + "config.properties");
            prop.setProperty("savePath", System.getProperty("user.home") + File.separator + "Documents" + File.separator + "prijem");
            prop.setProperty("extraSavePath", "P:" + File.separator + "prijem" + File.separator);
            prop.store(output, homePath);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * inicializace loggeru log se uklada do adresare viz DefaultLogPath
     *
     * @param debug
     */
    public void initLogger(boolean debug) {
        try {
            if (!new File(defaultLogPath).exists()) {
                new File(defaultLogPath).mkdir();
            }
            fh = new FileHandler(defaultLogPath + "Converter.log", FILE_SIZE, 1, true);
            log.addHandler(fh);
            log.setUseParentHandlers(debug);
            SimpleFormatter sp = new SimpleFormatter();
            fh.setFormatter(sp);
            log.info("---------- Start ----------");
        } catch (IOException | SecurityException e) {
            System.err.println("Log file error. " + e);
        }
    }

    /**
     * Stahne prilohy msq do homePath pokud uz jsou stazeny znova je stahovat
     * nebude
     *
     * @param ec
     * @param dodavatele
     */
    public void downloadMsq(EmailConnector ec, ArrayList<Dodavatel> dodavatele) {

        DownloadManager dw = new DownloadManager(ec, homePath);
        for (Dodavatel d : dodavatele) {
            dw.downloadMsgs(d);
        }
    }

    /**
     * Projde slozku homePath a konvertuje soubory, pokud jiz byly konvertovany
     * preskoci je
     *
     * @param dodavatele
     */
    public void convertMsq(ArrayList<Dodavatel> dodavatele) {
        cm = new ConverterManager(homePath, savePath, externalPath);

        dodavatele.stream().forEach((d) -> {
            cm.readFiles(d);
        });
        ArrayList<Document> documents = cm.getDocuments();

    }

    /**
     * inicializace dodavatelu
     *
     *
     * @return vraci arraylist s dodavately
     */
    public ArrayList<Dodavatel> initDodavatele() {
        /**
         * Definuji PNS denni
         */
        Dodavatel pnsD = new Dodavatel("1393") {

            @Override
            public Document readFile(String path) {
                try {
                    CSV csv = new CSV();
                    csv.colCount = "Mn. [KS]";
                    csv.colDPH = "DPH";
                    csv.colEan = "EAN Bez.S.";
                    csv.colNC = "OC";
                    csv.colName = "Nazev";
                    csv.colSpecial = "T";
                    csv.date = "Datum Dod.";
                    csv.docNumber = "Dod.L.";
                    csv.filialka = "POS";

                    Document doc = new Document();
                    ArrayList<Row> rows = csv.getItems(path, ';', "Windows-1250");
//                    double obchMarze = 12.00;
                    String datum = rows.get(0).docDate;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
                    if (datum.equals("")) {
                        return null;
                    }
                    Date dat = sdf.parse(datum);
                    sdf.applyPattern("yyyymmdd");
                    datum = sdf.format(dat);
                    /**
                     * musim pres iterator kvuli mazani radku v klasickem loopu
                     * to nelze
                     */
                    for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext();) {
                        Row r = iterator.next();
                        r.docDate = datum;
//                        double dph = Double.valueOf(r.dph);
//                        r.nc =String.valueOf(Double.valueOf(r.nc)/(dph/100+1)*(1-obchMarze/100));
                        if (r.special.equals("R")) {
                            r.count = String.valueOf(Double.valueOf(r.count) * -1);
                        }

                    }
                    /**
                     * Choose filialky
                     */
                    rows = chooseFilialka(rows);
                    doc.setDate(datum);
                    doc.setFilialka(getActualFilialka());
                    doc.setRows(rows);
                    doc.setName("PNSD");
                    String number = doc.getRows().get(0).docNumber;

                    doc.setDate(datum);
                    doc.setDate(number);
                    doc.setSaveName(datum + "_" + number + ".csv");
                    return doc;
                } catch (ParseException ex) {
                    System.err.println("Nasatveni datumu nelze provest " + this.getName() + " " + ex);
                }
                return null;
            }
        };
        pnsD.addFilialka(jablonna, "6000056063");
        pnsD.addFilialka(zdice, "6000051461");
        pnsD.addFilialka(tocnik, "6000040892");
        pnsD.addFilialka(milesov, "6000060928");
        pnsD.setName("1393");
        pnsD.setFilter("mailbot@pns.cz", "elektronická data", "data za dodávky za dny");
        /**
         * Definuji PNS mesicni
         */
        Dodavatel pns = new Dodavatel("1393") {

            @Override
            public Document readFile(String path) {
                try {
                    CSV csv = new CSV();
                    csv.colCount = "MNOZSTVI";
                    csv.colDPH = "SAZBADPH";
                    csv.colEan = "EAN";
                    csv.colNC = "OBCHCENA";
                    csv.colName = "MATNAZEV";
                    csv.colSpecial = "TYP";
                    csv.date = "DATUMDUZP";
                    csv.docNumber = "VARSYMB";
                    csv.filialka = "PRIJEMCEID";

                    Document doc = new Document();
                    ArrayList<Row> rows = csv.getItems(path, ';', "Windows-1250");

                    String datum = rows.get(0).docDate;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy");
                    if (datum.equals("")) {
                        return null;
                    }
                    Date dat = sdf.parse(datum);
                    sdf.applyPattern("yyyymmdd");
                    datum = sdf.format(dat);
                    /**
                     * musim pres iterator kvuli mazani radku v klasickem loopu
                     * to nelze
                     */
                    for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext();) {
                        Row r = iterator.next();
                        r.docDate = datum;
                        if (r.special.equals("D")) {
                            r.count = String.valueOf(Double.valueOf(r.count) * -1);
                        }
                        if (r.special.equals("V")) {
                            iterator.remove();
                        }
                    }
                    /**
                     * Choose filialky
                     */
                    rows = chooseFilialka(rows);
                    doc.setDate(datum);
                    doc.setFilialka(getActualFilialka());
                    doc.setRows(rows);
                    doc.setName("PNS");
                    String number = doc.getRows().get(0).docNumber;

                    doc.setDate(datum);
                    doc.setDate(number);
                    doc.setSaveName(datum + "_" + number + ".csv");
                    return doc;
                } catch (ParseException ex) {
                    System.err.println("Nasatveni datumu nelze provest " + this.getName() + " " + ex);
                }
                return null;
            }
        };

        pns.addFilialka(hut, "5000033400");
        pns.addFilialka(tocnik, "5000055724");
        pns.addFilialka(milin, "5000060925");
        pns.addFilialka(nemocnice, "5000057714");
        pns.addFilialka(pec, "5000065344");
        pns.addFilialka(zdice, "5000064794");
        pns.addFilialka(kozarovice, "5000067084");
        pns.addFilialka(jablonna, "5000068738");
        pns.addFilialka(milesov, "5000071825");
        pns.setName("1393");
        pns.setFilter("mailbot@pns.cz", "elektronická data", "PNS");

        /**
         * Definuji Vodicku
         */
        Dodavatel vodicka = new Dodavatel() {
            @Override
            public Document readFile(String path) {
                try {
                    CSV csv = new CSV();
                    csv.colCount = "Odběratel (fakt. adresa) - PSČ";
                    csv.colDPH = "Dodavatel - IČO";
                    csv.colNumber = "Dodavatel - SWIFT";
                    csv.colNC = "Cena s DPH v sazbě 0% - vyúčtování";
                    csv.colName = "Odběratel (fakt. adresa) - stát";
                    csv.colSpecial = "Header";
                    csv.date = "Datum vystavení";
                    csv.docNumber = "Variabilní symbol";
                    csv.filialka = "Konečný příjemce - název firmy";
                    Document doc = new Document();
                    ArrayList<Row> rows = csv.getItems(path, ';', "Windows-1250");

                    String number = rows.get(0).docNumber;
                    String datum = rows.get(0).docDate;
                    String filialka = rows.get(0).filialka;

                    SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy");
                    if (datum.equals("")) {
                        return null;
                    }
                    Date dat = sdf.parse(datum);
                    sdf.applyPattern("yyyymmdd");
                    datum = sdf.format(dat);
                    ArrayList<Row> myRows = new ArrayList<>();
                    int poradi = 0;
                    for (Row r : rows) {
                        if (r.special.equals("Detail 1 - Detail 1 - Detail 1 - Detail 1")) {
                            r.docDate = datum;
                            r.docNumber = number;
                            r.filialka = filialka;
                            if (poradi % 2 != 0) {
                                myRows.add(r);
                            }
                            poradi++;
                        }
                    }
                    myRows = chooseFilialka(myRows);
                    doc.setRows(myRows);
                    doc.setName("Vodicka");
                    doc.setDate(datum);
                    doc.setDate(number);
                    doc.setFilialka(getActualFilialka());
                    doc.setSaveName(datum + "_" + number + ".csv");

                    return doc;

                } catch (Exception e) {
                    System.err.println("Chyba reader vodicka - " + e);
                }

                return null;
            }
        };
        vodicka.setName("1298");
        vodicka.addFilialka(milin, "Potraviny BALA Milín");
        vodicka.addFilialka(nemocnice, "Potraviny BALA u nemocnice");
        vodicka.setFilter("money@pekarny-vodicka.cz");
        /**
         * Definuji Alimpex
         */
        Dodavatel alimpex = new Dodavatel("0093") {
            @Override
            public Document readFile(String path) {

                DBF dbf = new DBF();
                dbf.colName = "NAME";
                dbf.colEan = "EAN";
                dbf.colCount = "BASKET";
                dbf.colNC = "P_W_VC";
                dbf.colNumber = "PLU";
                dbf.colFilialka = "ID_SUP";

                Document doc = new Document();
                ArrayList<Row> rows = dbf.getRows(path, "CP852");

                File f = new File(path);
                String fileName = f.getName();
                String[] spe = fileName.split("_");
                if (spe.length > 0) {
                    String docNumber = spe[1];
                    String filialka = spe[0];
                    for (Row r : rows) {
                        r.docNumber = docNumber;
                        r.filialka = filialka;

                    }
                    doc.setName("0093");
                    rows = chooseFilialka(rows);
                    doc.setNumberContractor(docNumber);
                    doc.setFilialka(getActualFilialka());
                    doc.setRows(rows);
                    doc.setSaveName(docNumber + ".csv");
                }

                return doc;

            }
        };
        alimpex.addFilialka(milin, "130355");
        alimpex.addFilialka(nemocnice, "128253");
        alimpex.addFilialka(pec, "133027");
        alimpex.addFilialka(kozarovice, "133901");
        alimpex.addFilialka(tocnik, "27050");
        alimpex.addFilialka(jablonna, "12365");
        alimpex.addFilialka(milesov, "12393");
        alimpex.addFilialka(trebsko, "136011");
        
        alimpex.setFilter("@alimpex.cz");

        Dodavatel alimpexServus = new Dodavatel("0093") {
            @Override
            public Document readFile(String path) {

                DBF dbf = new DBF();
                dbf.colName = "NAME";
                dbf.colEan = "EAN";
                dbf.colCount = "BASKET";
                dbf.colNC = "P_W_VC";
                dbf.colNumber = "PLU";
                dbf.colFilialka = "ID_SUP";

                Document doc = new Document();
                ArrayList<Row> rows = dbf.getRows(path, "CP852");

                File f = new File(path);
                String fileName = f.getName();
                String[] spe = fileName.split("_");
                if (spe.length > 0) {
                    String docNumber = spe[1];
                    String filialka = spe[0];
//                    System.out.println("Filialka cislo "+filialka+" cislo "+docNumber);
                    for (Row r : rows) {
                        r.docNumber = docNumber;
//                        System.out.println("filialka: "+filialka);
                        r.filialka = filialka;

                    }
                    doc.setName("0093");
                    rows = chooseFilialka(rows);
                    doc.setNumberContractor(docNumber);
                    doc.setFilialka(getActualFilialka());
                    doc.setRows(rows);
                    doc.setSaveName(docNumber + ".csv");
                }

                return doc;

            }
        };
        alimpexServus.addFilialka(jablonna, "12365");
        alimpexServus.addFilialka(milesov, "12393");
        alimpexServus.setFilter("@servus-vlasim.cz");

        /**
         * Definuji Unikom
         */
        Dodavatel unikom = new Dodavatel("1599") {
            @Override
            public Document readFile(String path) {

                CSV csv = new CSV();
                csv.colName = "NAZEV";
                csv.colEan = "EAN1";
                csv.colCount = "MN";
                csv.colNC = "CJ";
                csv.colDPH = "SAZDPH";
                csv.date = "DATUM";
                csv.filialka = "ICO2";
                csv.docNumber = "CISDOK";

                Document doc = new Document();
                ArrayList<Row> rows = csv.getItems(path, ';', "Windows-1250");
                String datum = "";
                try {
                    datum = rows.get(0).docDate;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yy");
                    if (datum.equals("")) {
                        return null;
                    }
                    Date dat = sdf.parse(datum);
                    sdf.applyPattern("yyyymmdd");
                    datum = sdf.format(dat);
                } catch (ParseException e) {
                    System.err.println(e);
                }

                for (Row r : rows) {
                    r.docDate = datum;
                    if (r.ean.length() > 4) {
                        String prefixEan = r.ean.substring(0, 5);
                        if (prefixEan.equals("00000")) {
                            r.ean = r.ean.substring(5, r.ean.length());
                        } else if (prefixEan.equals("0000")) {
                            r.ean = r.ean.substring(4, r.ean.length());
                        }
                    }
                }
                rows = chooseFilialka(rows);

                if (rows.size() > 0) {
                    String cislo = rows.get(0).docNumber;
                    doc.setRows(rows);
                    doc.setSaveName(datum + "_" + cislo + ".csv");
                    doc.setDate(datum);
                    doc.setFilialka(getActualFilialka());
                    doc.setNumberContractor(cislo);
                }
                doc.setName("1599");
                return doc;
            }
        };
        unikom.addFilialka(milin, "A003");
        unikom.addFilialka(nemocnice, "A004");
        unikom.addFilialka(pec, "A002");
        unikom.addFilialka(vo, "A006");
        unikom.addFilialka(cashPB, "A001");
        unikom.setFilter("nav@unikom.cz");

        /**
         * Definuji toner
         */
        Dodavatel toner = new Dodavatel("1582") {
            @Override
            public Document readFile(String path) {
                CSV csv = new CSV();
                csv.colName = "name";
                csv.colEan = "ean";
                csv.colDPH = "vatcode";
                csv.colCount = "basket";
                csv.colNC = "p_w_vc";
                csv.filialka = "office_id";
                Document doc = new Document();
                File f = new File(path);
                String number = f.getName();

                try {
                    number = f.getName().substring(19, f.getName().length() - 0);
                } catch (Exception e) {

                }
                ArrayList<Row> rows = csv.getItems(path, ',', "Windows-1250");

                for (Row r : rows) {
                    r.docNumber = number;
                }
                rows = chooseFilialka(rows);
                doc.setRows(rows);
                doc.setSaveName(number + ".csv");
                doc.setNumberContractor(number);
                doc.setName("Toner");
                return doc;
            }
        };
        toner.setFilter("@toner-rl.cz");
        toner.addFilialka(nemocnice, "6978");
        toner.addFilialka(pec, "5874");
        toner.addFilialka(kozarovice, "1432");
        toner.addFilialka(milin, "1234");
        toner.addFilialka(tocnik, "2334");
        toner.addFilialka(zdice, "2334");
        toner.addFilialka(jablonna, "3412");
        toner.addFilialka(milesov, "1199");
        /**
         * PAC
         */
        Dodavatel pac = new Dodavatel("1295") {
            @Override
            public Document readFile(String path) {
                Document doc = new Document();
                File f = new File(path);
                String number = f.getName().substring(2, f.getName().length() - 4);
                PDFPac pac = new PDFPac();
                ArrayList<Row> rows = pac.getRows(path);
                doc.setErrors(pac.getErrs());
                rows = chooseFilialka(rows);
                doc.setNumberContractor(number);
                doc.setFilialka(getActualFilialka());
                doc.setRows(rows);
                for (Row r : doc.getRows()) {
                    r.docNumber = number;
                    //System.out.println("name "+r.name);
                }
                doc.setName("1295");
                doc.setSaveName(number + ".csv");
                return doc;
            }

        };
        pac.addFilialka(nemocnice, "312079");
        pac.addFilialka(pec, "312175");
        pac.addFilialka(milin, "312114");
        pac.addFilialka(zdice, "202688");
        pac.addFilialka(kozarovice, "325073");
        pac.addFilialka(tocnik, "202638");
        pac.setFilter("@pekarnahorovice.cz", "Faktura");

        
        Dodavatel jas = new Dodavatel("0525") {
            @Override
            public Document readFile(String path) {
                Document doc = new Document();
                File f = new File(path);
                String number = f.getName();
                TXT jas = new TXT();
                ArrayList<Row> rows = jas.getItems(path);
                
                //doc.setErrors(jas.getErrs());
                
                //doc.setNumberContractor(number);
                doc.setFilialka(getActualFilialka());
                if (rows!=null){
                    rows = chooseFilialka(rows);
                    doc.setRows(rows);
                }else{
                    return null;
                }
                
//                for (Row r : doc.getRows()) {
//                    r.docNumber = number;
//                    //System.out.println("name "+r.name);
//                }
                doc.setName("0525");
                doc.setSaveName(number + ".csv");
                return doc;
            }

        };
          jas.addFilialka(cashPB, "POBOCKA");
          jas.addFilialka(milin, "POBOCKA1");
          jas.addFilialka(nemocnice, "POBOCKA2");
//          jas.addFilialka(milin, "POBOCKA3");
//          jas.addFilialka(milin, "POBOCKA4");
//          jas.addFilialka(milin, "POBOCKA5");
//          jas.addFilialka(milin, "POBOCKA6");
          jas.addFilialka(pec, "POBOCKA7");
          jas.addFilialka(kozarovice, "POBOCKA8");
          jas.addFilialka(milin, "POBOCKA9");
          jas.addFilialka(jablonna, "POBOCKA10");
          jas.addFilialka(milin, "POBOCKA11");
          jas.addFilialka(milesov, "POBOCKA12");
          jas.addFilialka(trebsko, "POBOCKA13");
          
//        pac.addFilialka(nemocnice, "312079");
//        pac.addFilialka(pec, "312175");
//        pac.addFilialka(milin, "312114");
//        pac.addFilialka(zdice, "202688");
//        pac.addFilialka(kozarovice, "325073");
//        pac.addFilialka(tocnik, "202638");
        jas.setFilter("neodpovidat@jas-cr.cz", "JAS CR");

        /**
         * Nacteni dodavatelu
         */
        dodavatele = new ArrayList<>();
        dodavatele.add(pac);
        dodavatele.add(pns);
        dodavatele.add(pnsD);
        dodavatele.add(alimpex);
        dodavatele.add(alimpexServus);
        dodavatele.add(unikom);
        dodavatele.add(vodicka);
        dodavatele.add(jas);

        return dodavatele;
    }

    /**
     * nastavi cestu pro dokladz od dodavatelu kdyz se nenastavi pouzije vychozi
     *
     *
     * @param homePath
     */
    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    /**
     * nastavi cestu pro konvertovane doklady kdyz se nenastavi pouzije vychozi
     *
     * @param savePath
     */
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    /**
     * nastavi cestu pro log kdyz se nenastavi pouzije vychozi
     *
     * @param defaultLogPath
     */
    public void setDefaultLogPath(String defaultLogPath) {
        this.defaultLogPath = defaultLogPath;
    }

    /**
     *
     */
    public void printIt() {

        for (Dodavatel d : dodavatele) {
            d.print();
            System.out.println("---- " + d.getName() + " ---");

            for (Document doc : d.getDocs()) {
                doc.print();
                doc.printErrors();
            }
            System.out.println("-------------------------");
            System.out.println("");

        }
    }

    /**
     * vrati dokumenty od vsech dodavatelu
     *
     * @return arraylist dokumentu
     */
    public ArrayList<Document> getAllDocuments() {
        ArrayList<Document> documents = new ArrayList<>();
        for (Dodavatel d : dodavatele) {

            for (Document doc : d.getDocs()) {
                documents.add(doc);
            }

        }
        return documents;
    }

}
