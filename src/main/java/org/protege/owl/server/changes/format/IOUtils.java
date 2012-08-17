package org.protege.owl.server.changes.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



/**
 * 
 * @author redmond
 * @deprecated Replace with Matthew's format
 */
@Deprecated
public class IOUtils {
    public static final String UTF8 = "UTF-8";
    
    private IOUtils() {
        // TODO Auto-generated constructor stub
    }
    
    public static void writeInt(OutputStream os, int i) throws IOException {
        byte b4 = (byte) i;
        i = i / 256;
        byte b3 = (byte) i;
        i = i / 256;
        byte b2 = (byte) i;
        i = i / 256;
        byte b1 = (byte) i;
        os.write(b1);
        os.write(b2);
        os.write(b3);
        os.write(b4);
    }
    
    public static int readInt(InputStream is) throws IOException {
        int i;
        i = is.read();
        i = 256 * i + is.read();
        i = 256 * i + is.read();
        i = 256 * i + is.read();
        return i;
    }
    
    public static void writeString(OutputStream os, String s) throws IOException {
        byte[] bytes = s.getBytes(UTF8);
        writeInt(os, bytes.length);
        os.write(bytes);
    }
    
    public static String readString(InputStream is) throws IOException {
        int count = readInt(is);
        byte[] bytes = readBytes(is, count);
        return new String(bytes, UTF8);
    }
    
    public static byte[] readBytes(InputStream is, int count) throws IOException {
        byte[] bytes = new byte[count];
        int bytesRead = 0;
        do {
            bytesRead += is.read(bytes, bytesRead, count - bytesRead);
        } while (bytesRead < count);
        return bytes;
    }

}
