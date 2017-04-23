/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;

/**
 *
 * @author Podzimek Vojtěch
 */
public class Filialka {

    private String code;
    private String dodavName;
    
    public Filialka() {
    }

    public Filialka(String code, String dodavName) {
        this.code = code;
        this.dodavName = dodavName;
    }
    

    
    public void setCode(String code) {
        this.code = code;
    }

    public void setDodavName(String dodavName) {
        this.dodavName = dodavName;
    }

    public String getCode() {
        return code;
    }

    public String getDodavName() {
        return dodavName;
    }

    
    
}
