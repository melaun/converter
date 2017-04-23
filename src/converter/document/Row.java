/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter.document;

/**
 *
 * @author Podzimek Vojtěch
 */
/**
 * This represemt one row in document one item
 */
public class Row {

    public String name = "";
    public String code = "";
    public String nc = "";
    public String dph = "";
    public String count = "";
    public String ean = "";
    public String ean2 = "";
    public String special = "";
    public String docDate = "";
    public String docNumber = "";
    public String filialka = "";
    public Row() {
    }

    @Override
    public String toString() {
        return docNumber + ";" + docDate + ";" + code + ";" + name + ";" + ean + ";" + count + ";" + nc + ";" + dph + ";" + special + ";" + ean2  + filialka +"\r\n";
    }

}
