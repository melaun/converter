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
import converter.document.Row;
import downloader.DownloadManager;
import downloader.EmailConnector;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
    private String defaultLogPath = System.getProperty("user.home") + File.separator + ".JavaConverter" + File.separator;
    /**
     * Výchozi cesta pro ukladani dokladu od dodavatele
     */
    private String homePath = System.getProperty("user.home") + File.separator + ".JavaConverter" + File.separator;
    /**
     * Výchozi cesta pro konvertovane doklady
     */
    private String savePath = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "prijem";

//private String savePath = "P:\\" + File.separator + "prijem" + File.separator;
    /**
     * výchozí velikost pro soubor logu 1MB
     */
    private static final int FILE_SIZE = 1024 * 1024;
    /**
     * list s dodavateli
     */
    private ArrayList<Dodavatel> dodavatele = new ArrayList<>();

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
        cm = new ConverterManager(homePath, savePath);
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
         * Definuji PNS
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
        pns.setName("1393");
        pns.setFilter("podzimek.vojtech@korunapb.cz", "elektronická data", "PNS");

        /**
         * Definuji Vodicku
         */
        Dodavatel vodicka = new Dodavatel() {
            @Override
            public Document readFile(String path) {
                try {
                    CSV csv = new CSV();
                    csv.colCount = "Odběratel (fakt. adresa) - stát";
                    csv.colDPH = "Dodavatel - IČO";
                    csv.colNumber = "Dodavatel - SWIFT";
                    csv.colNC = "Cena s DPH v sazbě 0% - vyúčtování";
                    csv.colName = "Odběratel - IČO";
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
        alimpex.addFilialka(zdice, "133901");
        alimpex.setFilter("@alimpex.cz");

        /**
         * Definuji Unikom
         */
        Dodavatel unikom = new Dodavatel("1599") {
            @Override
            public Document readFile(String path) {

                DBF dbf = new DBF();
                dbf.colName = "NAZEV";
                dbf.colEan = "EAN1";
                dbf.colCount = "MN";
                dbf.colNC = "CJ";
                dbf.colDPH = "SAZDPH";
                dbf.colDocNumber = "CISDOK";
                dbf.colDate = "DATUM";
                dbf.colFilialka = "ICO2";

                Document doc = new Document();
                ArrayList<Row> rows = dbf.getRows(path, "CP852");

                for (Row r : rows) {
                    String prefixEan = r.ean.substring(0, 5);
                    if (prefixEan.equals("00000")) {
                        r.ean = r.ean.substring(5, r.ean.length());
                    } else if (prefixEan.equals("0000")) {
                        r.ean = r.ean.substring(4, r.ean.length());
                    }
                }
                rows = chooseFilialka(rows);

                if (rows.size() > 0) {
                    String datum = rows.get(0).docDate;
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
        unikom.addFilialka(milin, "003");
        unikom.addFilialka(nemocnice, "004");
        unikom.addFilialka(pec, "002");
        unikom.addFilialka(vo, "006");
        unikom.setFilter("@unikom.cz");

        /**
         * Definuji toner
         */
        Dodavatel toner = new Dodavatel("Toner") {
            @Override
            public Document readFile(String path) {
                CSV csv = new CSV();
                csv.colName = "name";
                csv.colEan = "ean";
                csv.colDPH = "vatcode";
                csv.colCount = "basket";
                csv.colNC = "p_w_vc";
                Document doc = new Document();
                File f = new File(path);
                String number = f.getName().substring(0, f.getName().length() - 4);
                doc.setRows(csv.getItems(path, ',', "Windows-1250"));
                for (Row r : doc.getRows()) {
                    r.docNumber = number;
                }
                doc.setSaveName(number + ".csv");
                doc.setNumberContractor(number);
                doc.setName("Toner");
                return doc;
            }
        };
        toner.setFilter("@toner-rl.cz");
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
        pac.setFilter("@pekarnahorovice.cz", "Faktura");

        /**
         * Nacteni dodavatelu
         */
        dodavatele = new ArrayList<>();
        dodavatele.add(pac);
        dodavatele.add(pns);
        dodavatele.add(alimpex);
        dodavatele.add(unikom);
        dodavatele.add(vodicka);
        dodavatele.add(toner);

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
