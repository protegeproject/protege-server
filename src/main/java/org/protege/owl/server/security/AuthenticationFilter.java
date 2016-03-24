package org.protege.owl.server.security;

import org.protege.owl.server.api.ServerFilterAdapter;
import org.protege.owl.server.api.ServerLayer;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.TransportHandler;

/**
 * Represents the authentication gate that will validate the user session in the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AuthenticationFilter extends ServerFilterAdapter {

    private SessionManager sessionManager = new SessionManager();

    private DefaultLoginService loginService;

    public AuthenticationFilter(ServerLayer delegate) {
        super(delegate);
//        loginService = new DefaultLoginService(sessionManager);
    }

    @Override
    public void setTransport(TransportHandler transport) throws OWLServerException {
        try {
            transport.bind(this);
            transport.bind(loginService);
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
        super.setTransport(transport);
    }
}
