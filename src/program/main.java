/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package program;

import converter.Dodavatel;
import downloader.EmailConnector;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Podzimek Vojtěch
 */
public class main  {

    /**
     * @param args -v visiual
     */
    public static void main(String[] args) {
        //launch(args);
        ArrayList<Dodavatel> dodavatele;
        EmailConnector ec = new EmailConnector("imap.forpsi.com", "edi@korunapb.cz", "852456ban");
        
        if (args.length > 0) {
            if (args[0].equals("-d")) {
                System.out.println("Pouštím aplikaci s výpisem logu");
                Converter converter = new Converter();
                converter.initLogger(true);
                dodavatele = converter.initDodavatele();
                converter.downloadMsq(ec, dodavatele);
                converter.convertMsq(dodavatele);
                converter.printIt();
            }
        } else {
            System.out.println("Pouštím aplikaci");
                
            Converter converter = new Converter();
            converter.initLogger(false);
            dodavatele = converter.initDodavatele();
            converter.downloadMsq(ec, dodavatele);
            converter.convertMsq(dodavatele);
            converter.printIt();
        }
        
    }

    
}
