package org.protege.owl.server.api;

/**
 * A server filter is a wrapper around an existing implementation of a server that adds some functionality.
 * 
 * Typically a running server will be wrapped by a sequence of several Server Filters.  These ServerFilters are arranged
 * as a delegation chain.  Thus for example a typical configuration might consist of the following components:
 * <ol>
 * <li> an authentication server filter which is responsible for ensuring that the User properly authenticates himself
 *      before accessing the server.</li>
 * <li> a policy server filter which is responsible for ensuring that the system policy regarding ontology access is upheld.</li>
 * <li> a conflict management server filter which is responsible for ensuring that conflicts between users updating the ontology.</li>
 * </ol>
 * In this example, the authentication server filter delegates to the policy server filter which in turn delegates to the conflict management server filter which finally 
 * delegates to the core server.  The core server in isolation has none of the functionality that has been added by the server filters; it does not
 * authenticate the user, it does not understand any system policy and it ignores possible conflicts.  The server filters add these essential capabilities to the server.
 * <p/>
 * This approach to starting a server is highly pluggable.  The server configuration builder automatically follows a specification of the needed Server Filter components.
 * The specification used by the configuration builder can choose the Server Filters involved to  do such things as
 * <ul>
 * <li> the server policy implementation,</li>
 * <li> the server conflict management strategy</li>
 * <li> the server authentication management</li>
 * <li> server side logging mechanisms, etc.</li>
 * </ul>
 * The server builder will not start a server until all the needed server filters have been successfully 
 * 
 * @author tredmond
 *
 */
public abstract class ServerFilter implements Server {
	private Server delegate;
	
	public ServerFilter(Server delegate) {
		this.delegate = delegate;
	}
	
	public Server getDelegate() {
		return delegate;
	}
	
}
