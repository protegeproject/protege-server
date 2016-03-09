package org.protege.owl.server.security;

import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerFilterAdapter;

/**
 * Represents the authentication gate that will validate the user session in the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AuthenticationFilter extends ServerFilterAdapter {

    public AuthenticationFilter(Server delegate) {
        super(delegate);
    }
}
