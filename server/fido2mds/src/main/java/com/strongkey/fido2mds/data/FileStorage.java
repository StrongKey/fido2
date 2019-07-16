/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileStorage extends Storage {

    private static final String BASE_DIR = "target/tmp/";

    public FileStorage() {
        super();
        createDirectory(BASE_DIR);
    }

    private void createDirectory(String dir) {
        try {
            Files.createDirectory(Paths.get(dir));
        } catch (IOException ex) {
            // probably already exists
        }
    }

    @Override
    public String loadData(String namespace, String key) {
        String fullPath = BASE_DIR + namespace + "/" + key;
        try (
                FileInputStream fis = new FileInputStream(fullPath);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            //System.out.println("Loading data from " + fullPath);
            return (String) ois.readObject();
        } catch (Exception ex) {
            //ex.printStackTrace();
            //System.out.println("Error reading data from "+fullPath);
        }
        return null;
    }

    @Override
    public void saveData(String namespace, String key, String data) {
        String fullPath = BASE_DIR + namespace + "/" + key;
        try {
            createDirectory(BASE_DIR + namespace);
            new File(fullPath).delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try (
                FileOutputStream fos = new FileOutputStream(fullPath);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(data);
            //System.out.println("Saved data to " + fullPath);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error saving data to " + fullPath);
        }
    }

}
