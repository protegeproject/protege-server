package org.protege.editor.owl.server.api.exception;

import java.io.IOException;


/**
 * 
 * @author redmond
 */
public class RuntimeIOException extends RuntimeException {
    private static final long serialVersionUID = -6102203015891611139L;

    public RuntimeIOException(IOException ioe) {
        super(ioe);
    }
    
    public IOException getCause() {
        return (IOException) super.getCause();
    }

}
