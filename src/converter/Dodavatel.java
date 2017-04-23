/*
Trida dodavatel reprezentuje univerzalniho dodavatele
tria implementuje DocReader tzn je treba prepsat metodu readFile
ve ktere definujeme sloupecky souboru ktere pak namapujeme na vzstupni soubor
v teto metode upravujeme vzsledne data do zadane podoby

dale je treba nastavit filtr (term,vyhledavaci pravidl), podle ktereho se stahnou
prilohz mailu

v posledni casti lze priradit kod filialkz ktery pak nahradi dodavatelskz kod slouzi
pro rozrazeni na spravne filialky
 */
package converter;

import converter.document.DocReader;
import converter.document.Document;
import converter.document.Row;
import java.util.ArrayList;
import java.util.Date;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

/**
 *
 * @author Podzimek Vojtěch
 */
public abstract class Dodavatel implements DocReader {

    /**
     * nazev dodavatele
     */
    private String name = null;
    /**
     * hlavni Vzhledavaci pravidlo
     */
    private SearchTerm term;
    /**
     * vzorec na prepocet dnu
     */
    private static long DAY_IN_MS = 1000 * 60 * 60 * 24;
    /**
     * pocet dnu dozadu ve kterych budu vzhledavat
     */
    private int days = 30;
    /**
     * Datum od ktereho budu vzhledavat smerem k dnesku
     */
    private Date mezniDate;
    /**
     * Vyhledavaci pravidlo pro datum
     */
    private ReceivedDateTerm dateTerm;
    /**
     * Aktualne zapracovavana filialka
     */
    private String actualFilialka = "";
    /**
     * filialky ktere maji prirazeny kod pro vyhledavani
     */
    private ArrayList<Filialka> filialky;

    /**
     * list obsahujici konvertovane dokumenty
     */
    private ArrayList<Document> docs = null;
    private int downloadMsgs = 0;

    /**
     * Construktor
     */
    public Dodavatel() {
        docs = new ArrayList<>();
        filialky = new ArrayList();
        mezniDate = new Date(new Date().getTime() - (days * DAY_IN_MS));
        dateTerm = new ReceivedDateTerm(ComparisonTerm.GE, mezniDate);
    }

    /**
     * Construktor
     *
     * @param name jmeno dodavatele + jmeno slozky pro ulozeni
     */
    public Dodavatel(String name) {
        this();
        this.name = name;
    }

    /**
     * Nastavim vyhledavaci filtr vyhledavam pouze dle emailu staci zadata jen
     * cast
     *
     * @param email edmail dle ktereho vyhledavam
     */
    public void setFilter(String email) {
        term = new FromStringTerm((email));
        term = new AndTerm(term, dateTerm);
    }

    /**
     * Nastavim vyhledavaci filtr vyhledavam pouze dle emailu a predmetu staci
     * zadata jen cast, logika je AND
     *
     * @param email email podle ktereho vzhledavam
     * @param subject predmet podle ktereho vyhledavam
     */
    public void setFilter(String email, String subject) {
        term = new AndTerm(new FromStringTerm((email)), new SubjectTerm(subject));
        term = new AndTerm(term, dateTerm);
    }

    /**
     * Nastavim vyhledavaci filtr vyhledavam dle emailu, predmetu a casti textu
     * v tele emailu staci zadata jen cast vse je keysensitive logika je AND
     *
     * @param email email podle ktereho vzhledavam
     * @param subject predmet podle ktereho vyhledavam
     * @param partOfText cast textu podle ktere vuhledavam
     */
    public void setFilter(String email, String subject, String partOfText) {
        term = new AndTerm(new FromStringTerm((email)), new SubjectTerm(subject));
        term = new AndTerm(term, new BodyTerm(partOfText));
        term = new AndTerm(term, dateTerm);
    }

    public String getName() {
        return name;
    }

    public SearchTerm getFilter() {
        return term;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDownloadMsgs() {
        this.downloadMsgs += 1;
    }

    public void addDocument(Document doc) {
        docs.add(doc);
    }

    public int getDownloadMsgs() {
        return downloadMsgs;
    }

    public int getDocumentCount() {
        return docs.size();
    }

    /**
     * Prida filialku do listu filialek
     *
     * @param code
     * @param dodavatelName
     */
    public void addFilialka(String code, String dodavatelName) {
        filialky.add(new Filialka(code, dodavatelName));
    }

    /**
     * Vyhleda dadavatelskz kod v listu filialek a porovna se sloupeckem
     * filialky pokud je shoda nahradi dodaavtelskz kod kodem filialky
     *
     * @param rows
     * @return
     */
    public ArrayList<Row> chooseFilialka(ArrayList<Row> rows) {
        if (filialky.size() > 0 && rows.size() > 0) {
            for (Filialka f : filialky) {
                if (f.getDodavName().equals(rows.get(0).filialka)) {
                    actualFilialka = f.getCode();
                    ArrayList<Row> newar = new ArrayList<>();
                    for (Row r : rows) {
                        r.filialka = f.getCode();
                        newar.add(r);
                    }
                    return newar;
                }
            }
        }
        return rows;
    }

    public void print() {
        System.out.println(this.name + " stazeno: " + downloadMsgs + " konvertovano: " + getDocumentCount());
    }

    public String getActualFilialka() {
        return actualFilialka;
    }

    public ArrayList<Document> getDocs() {
        return docs;
    }

}
