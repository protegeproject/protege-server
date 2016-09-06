package org.protege.editor.owl.server.security;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class LoginTimeoutException extends Exception {

    private static final long serialVersionUID = 3656231819885187701L;

    public LoginTimeoutException() {
        super("User session has expired due to inactivity");
    }
}