package org.protege.editor.owl.server.api;

import org.protege.editor.owl.server.api.exception.ServerServiceException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Password;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * Represents the login service provided by the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface LoginService {

    AuthToken login(UserId username, Password password) throws ServerServiceException;
    
    void setConfig(ServerConfiguration config);
}
