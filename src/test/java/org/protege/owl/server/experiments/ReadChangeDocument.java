package org.protege.owl.server.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.changes.DocumentFactoryImpl;

public class ReadChangeDocument {

    /**
     * @param args	args
     * @throws IOException	IOException
     */
    public static void main(String[] args) throws IOException {
        DocumentFactory factory = new DocumentFactoryImpl();
        InputStream in = new FileInputStream(new File("src/test/resources/Pizza-ubuntu.history"));
        factory.readChangeDocument(in, null, null);
    }

}
