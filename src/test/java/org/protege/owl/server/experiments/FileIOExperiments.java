package org.protege.owl.server.experiments;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileIOExperiments {
    public static int BUFFER=50000;
    public static String FILE = "/home/redmond/Downloads/xubuntu-12.04-desktop-amd64.iso";
    public static String FILE2 = "/home/redmond/Downloads/foo1.iso";
    public static String FILE3 = "/home/redmond/Downloads/foo2.iso";
    // public static String FILE = "/home/redmond/Downloads/ubuntu-12.04-desktop-amd64.iso";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        periodicRead(new FileInputStream(FILE), false);
        periodicRead(new BufferedInputStream(new FileInputStream(FILE2)), false);
        periodicRead(new FileInputStream(FILE3), true);
    }
    

    public static void periodicRead(InputStream in, boolean buffering) throws IOException {
        System.out.println("Using " + in.getClass() + (buffering ? " with " : " without ") + "buffering.");
        int count = 0;
        long startTime = System.currentTimeMillis();
        byte[] buffer = new byte[BUFFER];
        try {
            boolean eof = false;
            while (!eof) {
                /*
                if (in.skip(BUFFER) < 0) {
                    System.out.println("Skip failed");
                    break;
                }
                if (in.read() < 0) {
                    break;
                }
                */
                @SuppressWarnings("resource")
                InputStream bufIn = buffering ? new BufferedInputStream(in) : in;
                int bytesRead = 0;
                do {
                    int ret = bufIn.read(buffer, bytesRead, BUFFER - bytesRead);
                    if (ret < 0) {
                        System.out.println("At end of buffer");
                        eof = true;
                        break;
                    }
                    bytesRead += ret;
                } while (bytesRead < count);
                count++;
            }
        }
        catch (EOFException e) {
            System.out.println("EOF exception");
        }
        finally {
            in.close();
        }
        System.out.println("Reached end of file after " + count + " iterations");
        System.out.println("Took " + ((System.currentTimeMillis() - startTime) + " ms."));
    }
}
