package org.protege.owl.server.security;

import org.protege.owl.server.api.LoginService;
import org.protege.owl.server.api.ServerFilterAdapter;
import org.protege.owl.server.api.ServerLayer;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.TransportHandler;

import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
import edu.stanford.protege.metaproject.api.UserRegistry;

/**
 * Represents the authentication gate that will validate the user session in the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AuthenticationFilter extends ServerFilterAdapter {

    private final AuthenticationRegistry authRegistry;

    private SessionManager sessionManager = new SessionManager();

    private LoginService loginService;

    public AuthenticationFilter(ServerLayer delegate) {
        super(delegate);
        authRegistry = getConfiguration().getAuthenticationRegistry();
        UserRegistry userRegistry = getConfiguration().getMetaproject().getUserRegistry();
        loginService = new DefaultLoginService(authRegistry, userRegistry, sessionManager);
    }

    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
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
