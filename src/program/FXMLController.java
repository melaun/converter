/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package program;

import converter.Dodavatel;
import converter.document.Document;
import downloader.EmailConnector;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author Podzimek Vojtěch
 */
public class FXMLController implements Initializable {

    ArrayList<Document> documents;

    @FXML
    private Label label;

    @FXML
    private TableView<Document> table;

    @FXML
    private Button startBut;
    
    private Converter converter;

    private ObservableList<Document> docs;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //ObservableList<Document> data = documents;
        label.setText("Vojta");

    }

    @FXML
    private void pressStartButt(ActionEvent e) {
        ArrayList<Dodavatel> dodavatele;
        EmailConnector ec = new EmailConnector("imap.forpsi.com", "edi@korunapb.cz", "852456ban");
        converter = new Converter();
        converter.initLogger(false);
        dodavatele = converter.initDodavatele();
        converter.downloadMsq(ec, dodavatele);
        converter.convertMsq(dodavatele);
        converter.printIt();
        docs = FXCollections.observableArrayList(converter.getAllDocuments());
        table.setItems(docs);
        
        TableColumn<Document, String> colContractor = new TableColumn<>("Dodavatel");
        TableColumn<Document, String> colDocumentNumber = new TableColumn<>("číslo dokladu");
        TableColumn<Document, String> colFilialka = new TableColumn<>("Filialka");
        TableColumn<Document, Integer> colErrors = new TableColumn<>("Chyby");
        
        
        colContractor.setCellValueFactory(new PropertyValueFactory("nameContractor"));
        colDocumentNumber.setCellValueFactory(new PropertyValueFactory("numberContractor"));
        colFilialka.setCellValueFactory(new PropertyValueFactory("filialka"));
        colErrors.setCellValueFactory(new PropertyValueFactory("getErrors"));
        
        
        table.getColumns().setAll(colContractor,colDocumentNumber,colFilialka);
        
    }

}
