package org.protege.owl.server.exception;

public class InvalidRemoteOntologySpec extends RemoteOntologyException {
    private static final long serialVersionUID = 3237430998097279790L;

    public InvalidRemoteOntologySpec() {
    }
    
    public InvalidRemoteOntologySpec(String message) {
        super(message);
    }
    
    public InvalidRemoteOntologySpec(String message, Throwable t) {
        super(message, t);
    }
    
    public InvalidRemoteOntologySpec(Throwable t) {
        super(t);
    }
}
