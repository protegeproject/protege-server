package org.protege.owl.server.changes;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CongestionLoggerOutputStream extends FilterOutputStream {
    private CongestionLogger logger = new CongestionLogger("written");

    public CongestionLoggerOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        logger.logBytes(b.length);
    }

    @Override
    public void close() throws IOException {
        out.close();
        logger.close();
    }
}
