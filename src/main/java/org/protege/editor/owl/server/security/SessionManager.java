package org.protege.editor.owl.server.security;

import javax.annotation.Nonnull;

import edu.stanford.protege.metaproject.api.AuthToken;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class SessionManager {

    private final TokenTable loginTokenTable;

    public SessionManager(@Nonnull TokenTable loginTokenTable) {
        this.loginTokenTable = loginTokenTable;
    }

    public void addSession(String tokenKey, AuthToken authToken) {
        loginTokenTable.put(tokenKey, authToken);
    }

    public AuthToken getAuthToken(String tokenKey) throws LoginTimeoutException {
        return loginTokenTable.get(tokenKey);
    }

    public boolean validate(AuthToken authToken, String tokenOwner) {
        if (!authToken.isAuthorized()) {
            return false;
        }
        if (loginTokenTable.contains(authToken)) {
            if (tokenOwner.equals(authToken.getUser().getId().get())) {
                return true;
            }
        }
        return false;
    }
}
