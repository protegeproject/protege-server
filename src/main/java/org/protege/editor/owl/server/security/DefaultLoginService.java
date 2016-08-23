package org.protege.editor.owl.server.security;

import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.*;
import edu.stanford.protege.metaproject.api.exception.UnknownUserIdException;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;
import edu.stanford.protege.metaproject.impl.AuthorizedUserToken;
import org.protege.editor.owl.server.api.exception.ServerServiceException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultLoginService implements SaltedChallengeLoginService {
	
    private ServerConfiguration config;
   
    public DefaultLoginService() {}   

    @Override
    public AuthToken login(UserId username, Password password) throws ServerServiceException {
    	SaltedPasswordDigest saltedPassword;
    	
        if (password instanceof SaltedPasswordDigest) {
            saltedPassword = (SaltedPasswordDigest) password;
        }
        else {
        	PolicyFactory f = ConfigurationManager.getFactory();
 			Salt userSalt = (Salt) getSalt(username);
 			PlainPassword pwd = f.getPlainPassword(password.getPassword());
 			
 			saltedPassword = f.getPasswordHasher().hash(pwd, userSalt);
        }
        
        return login(username, saltedPassword);
    }

    @Override
    public AuthToken login(UserId userId, SaltedPasswordDigest password) throws ServerServiceException {
        try {
            if (config.hasValidCredentials(userId, password)) {
                User user = config.getUser(userId);
                AuthToken authToken = new AuthorizedUserToken(user);
                return authToken;
            }
            throw new ServerServiceException("Invalid combination of username and password");
        }
        catch (UnknownUserIdException e) {
            throw new ServerServiceException("Bad error. User has the credential but not registered", e);
        }

    }

    @Override
    public Salt getSalt(UserId userId) throws ServerServiceException {
        try {
            return config.getSalt(userId);
        }
        catch (UserNotRegisteredException e) {
            throw new ServerServiceException("Unknown user id: " + userId.get(), e);
        }
    }


	@Override
	public void setConfig(ServerConfiguration config) {
		this.config = checkNotNull(config);
		
	}
}
