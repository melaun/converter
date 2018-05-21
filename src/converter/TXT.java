/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;

import converter.document.Row;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Podzimek Vojtěch
 */
public class TXT {

    private BufferedReader br = null;
    private BufferedWriter wr = null;

    public ArrayList<Row> getItems(String url) {

        try {
            br = new BufferedReader(new FileReader(url));
            if (url.substring(url.length() - 3).equals("jas")) {
                return read();
            } else {
                return null;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TXT.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private ArrayList read() {

        String line = "";
        ArrayList<Row> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("ddmmyyyy");
        try {
            while ((line = br.readLine()) != null) {
                if (line.equals("")){break;}
                Row row = new Row();
                String dodkod = line.substring(0, 6).replaceAll("\\s+", "");
                String datum = line.substring(7, 15).replaceAll("\\s+", "");
                String cislodl = line.substring(16, 28).replaceAll("\\s+", "");
                String plu = line.substring(29, 44).replaceAll("\\s+", "");
                String name = line.substring(45, 95);
                String mn = line.substring(96, 108).replaceAll("\\s+", "").replace(',', '.');
                String dph = line.substring(109, 111).replaceAll("\\s+", "");
                String cenabdph = line.substring(112, 124).replaceAll("\\s+", "").replace(',', '.');
                String eanbal = line.substring(125, 145).replaceAll("\\s+", "");
                String odb = line.substring(177, 190).replaceAll("\\s+", "");
                String ksbal = line.substring(198, 201).replaceAll("\\s+", "").replace(',', '.');
                String ean = line.substring(202, 222).replaceAll("\\s+", "");
                String jednotka = line.substring(171, 176).replaceAll("\\s+", "");
                //System.out.println(dodkod+", "+datum+", "+cislodl+", "+plu+", "+name+", "+mn+", "+dph+", "+cenabdph+", "+eanbal+", "+odb+", "+ksbal+", "+ean);

                row.name = name;
                row.ean = ean;
                if (row.ean.equals("")) {
                    row.ean = eanbal;
                }
                row.nc = cenabdph;
                row.count = mn;
                System.out.println(cislodl+" - "+ jednotka);
                if (!jednotka.equals("KS") && !jednotka.equals("KR")) {
                    Double ks = Double.valueOf(mn);
                    Double bal = Double.valueOf(ksbal);
                    Double nc = Double.valueOf(cenabdph);
                    row.count = String.valueOf(ks * bal);
                    row.nc= String.valueOf(nc/bal);
                }

                row.code = plu;
                row.ean2 = eanbal;
                row.dph = dph;
                row.special = "";

                row.docDate = datum;
                try {
                    Date dat = sdf.parse(row.docDate);
                    sdf.applyPattern("yyyymmdd");
                    row.docDate = sdf.format(dat);
                } catch (ParseException ex) {
                    Logger.getLogger(TXT.class.getName()).log(Level.SEVERE, null, ex);
                }
                row.docNumber = cislodl;
                row.filialka = odb;

                items.add(row);
            }
            return items;
        } catch (IOException ex) {
            Logger.getLogger(TXT.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void separateFile(String pathName, String path) {
        try {
            String line = "";
            String number = "";
            br = new BufferedReader(new FileReader(pathName));

            while ((line = br.readLine()) != null) {
                if (number.equals(line.substring(16, 28).replaceAll("\\s+", ""))) {
                    wr.write(line);
                    wr.newLine();
                } else {
                    if (wr != null) {
                        wr.close();
                    }
                    number = line.substring(16, 28).replaceAll("\\s+", "");
                    wr = new BufferedWriter(new FileWriter(path + File.separator + number + ".jas"));
                    wr.write(line);
                    wr.newLine();
                }
            }
            if (wr != null) {
                wr.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(TXT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TXT.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
