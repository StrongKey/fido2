/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */
package com.strongkey.utilities;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Singleton
public class EmailService {
    
    private Session session;
    
    private final String CLASSNAME = EmailService.class.getName();
    
    @PostConstruct
    private void init(){
        String mailhostType = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.mailhost.type");
        
        //Set mail configurations
        Properties properties = System.getProperties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", 
                Configurations.getConfigurationProperty("webauthntutorial.cfg.property.mailhost"));
        properties.setProperty("mail.smtp.port", 
                Configurations.getConfigurationProperty("webauthntutorial.cfg.property.mail.smtp.port"));
        properties.setProperty("mail.smtp.from", 
                Configurations.getConfigurationProperty("webauthntutorial.cfg.property.smtp.from"));
        properties.setProperty("mail.smtp.auth",  String.valueOf(!mailhostType.equalsIgnoreCase("SendMail")));
        Authenticator auth = new PasswordAuthenticator(
                Configurations.getConfigurationProperty("webauthntutorial.cfg.property.smtp.auth.user"),
                Configurations.getConfigurationProperty("webauthntutorial.cfg.property.smtp.auth.password"));
        
        //TLS
        if(mailhostType.equalsIgnoreCase("StartTLS")){
            properties.setProperty("mail.smtp.starttls.enable", "true");
            session = Session.getInstance(properties, auth);
        }
        //SSL
        else if(mailhostType.equalsIgnoreCase("SSL")){
            properties.setProperty("mail.smtp.ssl.enable", "true");
            session = Session.getInstance(properties, auth);
        }
        else{
            session = Session.getInstance(properties);
        }
    }
    
    public void sendEmail(String email, String subjectline, String content) throws UnsupportedEncodingException{
        try{
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                    Configurations.getConfigurationProperty("webauthntutorial.cfg.property.smtp.from"),
                    Configurations.getConfigurationProperty("webauthntutorial.cfg.property.smtp.fromName")));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(subjectline);
            
            if(Configurations.getConfigurationProperty("webauthntutorial.cfg.property.email.type").equalsIgnoreCase("HTML")){
                message.setContent(content, "text/html; charset=utf-8");
            }
            else{
                message.setText(content);
            }
            
            Transport.send(message);
        } catch (MessagingException ex) {
            ex.printStackTrace();
            WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "callSKFSRestApi", "WEBAUTHN-ERR-5001", ex.getLocalizedMessage());
        }
    }
    
    private class PasswordAuthenticator extends Authenticator{
        private final String username;
        private final String password;
        
        public PasswordAuthenticator(String username, String password){
            this.username = username;
            this.password = password;
        }
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}
