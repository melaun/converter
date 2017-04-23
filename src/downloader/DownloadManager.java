/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import javax.mail.Folder;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import converter.Dodavatel;
import java.util.logging.Logger;

/**
 *
 * @author Podzimek Vojtěch
 */
public class DownloadManager {

    /**
     * nemel bz stahovat to co uy je stayene cilova slozka nastavena u
     * dodavatele
     */
    private Message messages[];

    private final String homePath;
    private EmailConnector ec = null;
    private int counter;
    private Logger logger = Logger.getLogger("ConverterLog");
    private Dodavatel dodavatel = null;

    /**
     *
     * @param ec
     * @param homePath
     */
    public DownloadManager(EmailConnector ec, String homePath) {

        this.ec = ec;
        this.homePath = homePath;
        // otestuju home dir pokud neni vytvorim
        if (!new File(homePath).exists()) {
            new File(homePath).mkdir();
        }
    }

    /**
     * Download mesg from DOdavatel
     *
     * @param d
     */
    public void downloadMsgs(Dodavatel d) {
        dodavatel = d;
        String host = ec.getHost();
        String user = ec.getUser();
        String password = ec.getPassword();
        Session session;
        Properties prop;
        Store store;
        Folder folder;

        counter = 0;
        messages = null;
        prop = new Properties();
        session = Session.getDefaultInstance(prop);
        session.setDebug(false);

        try {
            store = session.getStore("imap");
            store.connect(host, user, password);
            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            messages = folder.search(d.getFilter());
            logger.info("U " + d.getName() + " bylo nalezno " + messages.length + " zpráv.");

            downloadAttachments(d.getName());
            logger.info("Od " + d.getName() + " jsem stáhnul " + counter + " novych dokladu.");
        } catch (MessagingException ex) {
            logger.log(Level.SEVERE, "downloadMsgs", ex.toString());
        }
    }

    /**
     *
     * @param folder
     */
    private void downloadAttachments(String folder) {
        if (messages != null) {
            for (Message msg : messages) {
                downloadAttachment(msg, homePath + folder + '/');
            }
        }
    }

    /**
     *
     * @param msg
     * @param path
     */
    public void downloadAttachment(Message msg, String path) {
        try {
            String contentType = msg.getContentType();
            if (contentType.contains("multipart")) {
                Multipart multiPart = (Multipart) msg.getContent();
                for (int i = 0; i < multiPart.getCount(); i++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
                    String fileName = part.getFileName();
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        if (!new File(path + fileName).exists()) {
                            File file = new File(path + fileName);
                            file.getParentFile().mkdir();
                            part.saveFile(file);
                            dodavatel.addDownloadMsgs();
                            if (fileName.substring(fileName.length() - 4).equals(".zip")) {
                                UnzipUtility unzipper = new UnzipUtility();
                                unzipper.unzip(path + fileName, path);
                            }
                            logger.log(Level.INFO, "Stazeno {0}", file.getPath());
                        }
                    }
                }
            }
        } catch (MessagingException | IOException ex) {
            logger.log(Level.SEVERE, "downloadAttachment", ex);
        }

    }
}
