/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;

import converter.document.Row;
import com.csvreader.CsvReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Podzimek Vojtěch
 */
public class CSV {

    public String filePath = null;
    public String colName = null;
    public String colEan = null;
    public String colCount = null;
    public String colNC = null;
    public String colDPH = null;
    public String colSpecial = null;
    public String colNumber = null;
    public String date = null;
    public String docNumber = null;
    public String filialka = null;

    public ArrayList<Row> getItems(String url, char str, String charSet) {

        try {
            CsvReader reader = new CsvReader(url, str, Charset.forName(
                    charSet));
            return readCSV(reader);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSV.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private ArrayList readCSV(CsvReader reader) {
        try {
            reader.readHeaders();
            ArrayList<Row> items = new ArrayList<>();

            while (reader.readRecord()) {

                Row row = new Row();
                row.name = reader.get(colName);
                row.ean = reader.get(colEan);
                row.count = reader.get(colCount).replace(',', '.');
                row.nc = reader.get(colNC);
                row.code = reader.get(colNumber);
                row.ean2 = reader.get("");
                row.dph = reader.get(colDPH);
                row.special = reader.get(colSpecial);
                row.docDate = reader.get(date);
                row.docNumber = reader.get(docNumber);
                if (filialka != null) {
                    row.filialka = reader.get(filialka);
                } else {
                    row.filialka = "000";
                }
                items.add(row);

            }
            return items;
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }

    }

}
