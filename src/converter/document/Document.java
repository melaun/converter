/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter.document;

import com.csvreader.CsvWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Podzimek Vojtěch
 */
public class Document {

    /**
     * List of rows jednotlie radkz v dokumentu
     */
    private ArrayList<Row> rows;
    /**
     * Document date
     */
    private String date = "empty";
    /**
     * Document numberContractor
     */
    private String numberContractor = "empty";
    /**
     * jmeno souboru pod kterzm bude uloyen
     */
    private String saveName = "empty";
    /**
     * filialka ke ktere patri
     */
    private String filialka = "empty";
    /**
     * jmeno dodavatele od ktereho je
     */
    private String nameContractor = "empty";
    /**
     * chyby vynikle pri konverzi
     */
    private List<String> errors = null;

    /**
     * Create konstruktor of Document make empty list of rows
     */
    public Document() {
        rows = new ArrayList<>();
        errors = new ArrayList<>();
        saveName = date + "_" + numberContractor + ".csv";
    }

    /**
     * Save document on path
     *
     * @param savePath save doc na this path
     * @throws IOException
     */
    public void saveDocument(String savePath) throws IOException {
        CsvWriter csv = new CsvWriter(savePath, ';', Charset.forName("UTF-8"));

        csv.write("name");
        csv.write("ean");
        csv.write("code");
        csv.write("count");
        csv.write("nc");
        csv.write("dph");
        csv.write("date");
        csv.write("number");
        csv.write("special");
        csv.write("ean2");
        csv.write("filialka");
        csv.endRecord();

        for (Row r : rows) {

            csv.write(r.name);
            csv.write(r.ean);
            csv.write(r.code);
            csv.write(r.count);
            csv.write(r.nc);
            csv.write(r.dph);
            csv.write(r.docDate);
            csv.write(r.docNumber);
            csv.write(r.special);
            csv.write(r.ean2);
            csv.write(r.filialka);
            csv.endRecord();
        }
        csv.close();
    }

    /**
     * Add row to lsit of rows
     *
     * @param row row in document
     */
    public void addRow(Row row) {
        rows.add(row);
    }

    public void print() {
        System.out.println( nameContractor + " " + date + " " + numberContractor + " " + filialka + " pocet chyb " + errors.size());
    }

    /**
     * vztiskne chyby vznikle pri converzi
     */
    public void printErrors() {
        errors.stream().forEach((err) -> {
            System.out.println("- "+err);
        });
    }

    /**
     * SET/GETS
     */
    /**
     * Repalce existing list of rows by list
     *
     * @param rows this list
     */
    public void setRows(ArrayList<Row> rows) {
        this.rows = rows;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNumberContractor(String number) {
        this.numberContractor = number;
    }

    public void setFilialka(String filialka) {
        this.filialka = filialka;
    }

    public void setName(String name) {
        this.nameContractor = name;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public String getFilialka() {
        return filialka;
    }

    public String getDate() {
        return date;
    }

    public String getNumberContractor() {
        return numberContractor;
    }

    public String getSaveName() {
        return saveName;
    }

    public String getNameContractor() {
        return nameContractor;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public String getErrors(){
        return String.valueOf(errors.size());
    }
    
    

}
