/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

/**
 *
 * @author Podzimek Vojtěch
 */
public class EmailConnector {
    
    private String host;
    private String user;
    private String password;

    public EmailConnector(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }
    
    
    
    
}
