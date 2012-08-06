package org.protege.owl.server.changes;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UncloseableInputStream extends FilterInputStream {

    public UncloseableInputStream(InputStream in) {
        super(in);
    }
    
    @Override
    public void close() throws IOException {
        // ignore!  - stream needs to be closed explicitly elsewhere.
    }

}
