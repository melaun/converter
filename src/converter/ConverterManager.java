/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;

import converter.document.Document;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Podzimek Vojtěch
 */
public class ConverterManager {

    private final String homePath;
    private final String savePath;
    private int counter;
    private final Logger logger = Logger.getLogger("ConverterLog");
    private ArrayList<Document> documents;

    public ConverterManager(String homePath, String savePath) {
        this.homePath = homePath;
        this.savePath = savePath;
        this.documents = new ArrayList<>();
        /**
         * Pokud neexistuje savePath vytvorim ji
         */
        if (!new File(savePath).exists()) {
            new File(savePath).mkdir();
            logger.info("Byla vytvorena cesta "+savePath);
        }
    }

    public void readFiles(Dodavatel d) {
        logger.info("------ Konvertuji doklad od "+d.getName());
        counter = 0;
        File folder = new File(homePath + File.separator + d.getName());
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                Document doc = d.readFile(homePath + d.getName() + File.separator + fileEntry.getName());
                //doc = d.makeSpecial(doc);
                try {
                    // pokud neni slozka vytvorena vytvorim ji
                    File f = new File(savePath + File.separator + d.getName());
                    if (!f.exists()) {
                        f.mkdir();
                        logger.info("Byla vytvorena cesta "+savePath + File.separator + d.getName());
                    }
                    // jen pokud je dokument nacten
                    if (doc != null) {
                        // jen pokud uz neni covertovan
                        if (!new File(savePath + File.separator + d.getName() + File.separator + doc.getSaveName()).exists()) {
                            // ulozim dokument
                            doc.saveDocument(savePath + File.separator + d.getName() + File.separator + doc.getSaveName());
                            logger.info("Novy podklad od "+d.getName()+" - " + doc.getSaveName() + " byl ulozen.");counter++;
                            documents.add(doc);
                            d.addDocument(doc);
                        }
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE,"Read files - ukladani podkladu "+ doc.getSaveName(),ex);
                }
            }
        }
        logger.info("Od "+d.getName()+" jsem konvertoval "+counter+" podkladu.");  
    }

    public ArrayList<Document> getDocuments() {
        return documents;
    }
    
    


}
