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
public interface DocReader {

    /**
     * Defined file reader for import document
     * @param path
     * @return Document
     */
    Document readFile(String path);
    
    //Document makeSpecial(Document d);

}
