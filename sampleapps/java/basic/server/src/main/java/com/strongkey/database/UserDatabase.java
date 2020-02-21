/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */

package com.strongkey.database;

import com.strongkey.utilities.Configurations;
import com.strongkey.utilities.WebauthnTutorialLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

// A mock database for "user accounts". It is expected that RPs will have their
// own concept of what a "user" means in their application and will need to
// create similar functionality according to their use cases.
@Startup
@Singleton
public class UserDatabase {
    private static final String CLASSNAME = UserDatabase.class.getName();
    private Set<String> users = new HashSet();
    private final String BACKUPFILENAME = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.backupfilelocation");

    @PostConstruct
    public synchronized void loadDatabase(){
        File backupFile = new File(BACKUPFILENAME);
        if (backupFile.exists() && backupFile.isFile()) {
            try (ObjectInputStream backup = new ObjectInputStream(new FileInputStream(BACKUPFILENAME))){
                users = (Set<String>) backup.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                WebauthnTutorialLogger.logp(Level.WARNING, CLASSNAME, "loadDatabase",
                        "WEBAUTHN-MSG-1001", ex.getLocalizedMessage());
            }
        }
    }

    // Checks if a user exists in the "database"
    public synchronized boolean doesUserExist(String username){
        return users.contains(username);
    }

    // Adds a user to the "database"
    public synchronized void addUser(String username){
        users.add(username);
        storeDatabase();
    }

    // Removes a user from the "database"
    public synchronized void deleteUser(String username) {
        users.remove(username);
        storeDatabase();
    }

    private synchronized void storeDatabase(){
        createDatabase();
        try (ObjectOutputStream backup = new ObjectOutputStream(new FileOutputStream(BACKUPFILENAME))) {
            backup.writeObject(users);
            backup.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            WebauthnTutorialLogger.logp(Level.WARNING, CLASSNAME, "storeDatabase",
                    "WEBAUTHN-MSG-1002", ex.getLocalizedMessage());
        }
    }

    private synchronized void createDatabase(){
        try {
            File backupFile = new File(BACKUPFILENAME);
            if (!backupFile.exists()) {
                backupFile.getParentFile().mkdirs();
                backupFile.createNewFile();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            WebauthnTutorialLogger.logp(Level.WARNING, CLASSNAME, "createDatabase",
                    "WEBAUTHN-MSG-1002", ex.getLocalizedMessage());
        }
    }
}
