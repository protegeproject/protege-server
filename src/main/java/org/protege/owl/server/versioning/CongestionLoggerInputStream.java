package org.protege.owl.server.versioning;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


public class CongestionLoggerInputStream extends FilterInputStream {
    private CongestionLogger logger = new CongestionLogger("read");
    
    public CongestionLoggerInputStream(InputStream delegate) {
        super(delegate);
    }
    
    @Override
    public int read() throws IOException {
        try {
            return super.read();
        }
        finally {
            logger.logBytes(1);
        }
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = super.read(b, off, len);
        logger.logBytes(bytesRead);
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        super.close();
        logger.close();
    }
    
}
