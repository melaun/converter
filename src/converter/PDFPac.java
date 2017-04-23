/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;

import converter.document.Row;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
// musi tu byt
import org.apache.fontbox.*;
import org.apache.commons.logging.*;

/**
 *
 * @author Podzimek Vojtěch
 */
public class PDFPac {

    private ArrayList<Row> rows = null;
    private final Logger logger = Logger.getLogger("ConverterLog");
    private String filialkaID = "";
    private String line = "";
    private List<String> errs = null;

    public ArrayList<Row> getRows(String url) {
        errs = new ArrayList<>();
        //vemu text z pdf
        String text = getTextFromPDF(url);
        //rozdelim na radky
        String[] lines = parseText(text);
        //vyfiltruju jen lajny s polozkama
        ArrayList<String> lineWithItems = lineFilter(lines);
        
        return makeRows(lineWithItems);
    }

    private ArrayList makeRows(ArrayList<String> lines) {
        rows = new ArrayList<>();
        for (String line : lines){
            rows.add(makeRow(line));
        }
        return rows;
    }

    private Row makeRow(String line) {
        this.line = line;
        Row row = new Row();
        // pocitadlo
        int count = 0;
        String name = "";
        String[] words = line.split(" ");
        String word = words[0];

        if (isNumber(word)) {
            row.code = word;
        }
        for (count = 1; count < words.length; count++) {
            if (countTest(words[count])) {
                row.count = makeClearCount(words[count]);
                break;
            } else {
                
                name += words[count];
            }
        }
        row.filialka = filialkaID;
        row.name = name;
        String priceAll = words[count+1].replace(",", ".");
        row.nc = calculatePrice(row.count, priceAll);
        return row;
    }

    private String makeClearCount(String word) {

        return word.substring(0, word.length() - 2);
    }

    private String calculatePrice(String count, String priceAll) {
        try{
        Double mnozstvi = Double.parseDouble(count);
        Double cena = Double.parseDouble(priceAll);
        Double celkem = cena / mnozstvi;
        Double cc = Math.round(celkem * 100.0) / 100.0;
        return String.valueOf(cc);
        }catch(NumberFormatException ex ){
            logger.warning("Chyba v radku " + line);
            errs.add(line);
        }
        
        return "99.99";

    }

    private boolean countTest(String word) {
        if ((word.toLowerCase().contains("ks") || word.contains("kg") ) && isNumber(word.substring(0, word.length()-2))) {
            return true;
        }
     
        return false;
    }

    /**
     * vrati text z pdf
     *
     * @param url
     * @return
     */
    private String getTextFromPDF(String url) {
        String text = "";
        try {
            PDDocument pdDoc = PDDocument.load(new File(url));
            PDFTextStripper pdfStripper = new PDFTextStripper();
            text = pdfStripper.getText(pdDoc);
            pdDoc.close();
           
        } catch (IOException ex) {
            logger.warning("PDFPac soubor nebyl nalezen " + url + "chyba " + ex);
        }
        return text;
    }

    private String[] parseText(String text) {
        // rozdeli text na radky
        return text.split(System.getProperty("line.separator"));

    }

    /**
     * vybere jen radky se zbozim a nastavim promenou filialkaID
     */
    private ArrayList lineFilter(String[] lines) {
        ArrayList<String> itemsLine = new ArrayList<>();
        for (String line : lines) {
            String[] words = line.split(" ");
            if (isNumber(words[0]) && words[0].length() < 5) {
                //super radek zacina cislem pridame radek
                itemsLine.add(line);
                //jeste zkusim jestli to neni radek s cisle odberatele
            } else if (line.contains("Konečný příjemce")) {
                //jupi je to radek s odberatelem
                //vycucnu cislo odberatele
                for (String word : words) {
                    if (isNumber(word)) {
                        filialkaID = word;
                        break;
                    }
                }
            }
        }
        return itemsLine;
    }

    /**
     * kdyz je string cislo vrati true
     *
     * @param s string
     * @return true/false
     */
    private boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public List<String> getErrs() {
        return errs;
    }
    
    

}
