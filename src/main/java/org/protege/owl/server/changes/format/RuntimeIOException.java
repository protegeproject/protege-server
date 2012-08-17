package org.protege.owl.server.changes.format;

import java.io.IOException;


/**
 * 
 * @author redmond
 * @deprecated Replace with Matthew's format
 */
@Deprecated
public class RuntimeIOException extends RuntimeException {
    private static final long serialVersionUID = -6102203015891611139L;

    public RuntimeIOException(IOException ioe) {
        super(ioe);
    }
    
    public IOException getCause() {
        return (IOException) super.getCause();
    }

}
