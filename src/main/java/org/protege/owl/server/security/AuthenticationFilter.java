package org.protege.owl.server.security;

import org.protege.owl.server.api.ServerFilterAdapter;
import org.protege.owl.server.api.ServerLayer;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.TransportHandler;

import edu.stanford.protege.metaproject.api.AuthenticationManager;

/**
 * Represents the authentication gate that will validate the user session in the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AuthenticationFilter extends ServerFilterAdapter {

    private final AuthenticationManager authManager;

    private SessionManager sessionManager = new SessionManager();

    private DefaultLoginService loginService;

    public AuthenticationFilter(ServerLayer delegate) {
        super(delegate);
        authManager = getConfiguration().getAuthenticationManager();
        loginService = new DefaultLoginService(authManager, sessionManager);
    }

    @Override
    public void setTransport(TransportHandler transport) throws OWLServerException {
        try {
            transport.bind(loginService);
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
        super.setTransport(transport);
    }
}
