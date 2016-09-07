package org.protege.editor.owl.server.security;

import static com.google.common.base.Preconditions.checkNotNull;

import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.api.exception.ServerServiceException;

import com.unboundid.ldap.sdk.LDAPBindException;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Password;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.UnknownUserIdException;
import edu.stanford.protege.metaproject.impl.AuthorizedUserToken;

public class LDAPLoginService implements LoginService {
	
    private ServerConfiguration config;
    
    private static final String LDAPHOST = "ldap_host";
    private static final String LDAPPORT = "ldap_port";
   
    public LDAPLoginService() {}   

    @Override
    public AuthToken login(UserId userid, Password password) throws ServerServiceException {
    	
    	// TODO: implements logic based on specs from NCI, TBD
    	
    	try {
    		String host = config.getProperty(LDAPHOST);
    		int port = Integer.parseInt(config.getProperty(LDAPPORT));
    		
			LDAPConnection ldap = new LDAPConnection(host, port,
					"CN=" + userid.get() + ",OU=NCI,OU=NIH,OU=AD,DC=Nih,DC=GOV", password.getPassword());
			
			
			
			if (ldap.isConnected()) {
				try {
					return new AuthorizedUserToken(config.getUser(userid));
				} catch (UnknownUserIdException e) {
					throw new ServerServiceException("Invalid user id", e);					
				}
				
			} else {
				throw new ServerServiceException("Bad LDAP connection");
				
			}
		} catch (LDAPException e1) {
			if (e1 instanceof LDAPBindException) {
				LDAPBindException ex = (LDAPBindException) e1;
				ex.getBindResult().getResultCode().intValue();
				throw new ServerServiceException("Issue with LDAP " + ex.getBindResult().getResultString(), ex);
			}
			throw new ServerServiceException("Issue with LDAP ",e1);		
		} 	
    }

	@Override
	public void setConfig(ServerConfiguration config) {
		this.config = checkNotNull(config);
		
	}
}
