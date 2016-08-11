package org.protege.editor.owl.server.security;

import static com.google.common.base.Preconditions.checkNotNull;

import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.api.exception.ServerServiceException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Password;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.UnknownUserIdException;
import edu.stanford.protege.metaproject.impl.AuthorizedUserToken;

public class LDAPLoginService implements LoginService {
	
    private ServerConfiguration config;
   
    public LDAPLoginService() {}   

    @Override
    public AuthToken login(UserId userid, Password password) throws ServerServiceException {
    	
    	// TODO: implements logic based on specs from NCI, TBD.
    	
    	AuthToken authToken = null;
		try {
			authToken = new AuthorizedUserToken(config.getUser(userid));
		} catch (UnknownUserIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return authToken;
    	
    }

	@Override
	public void setConfig(ServerConfiguration config) {
		this.config = checkNotNull(config);
		
	}
}
