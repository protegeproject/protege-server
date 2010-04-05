package org.protege.owl.server.connection.servlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerConnectionFactory;
import org.protege.owl.server.configuration.ServerConfiguration;

public class ServletActivator implements BundleActivator {
    private ServiceRegistration registration;

    public void start(final BundleContext context) {
        ServerConnectionFactory factory = new ServerConnectionFactory() {
        	
        	@Override
        	public boolean isSuitable(ServerConfiguration serverConfiguration) {
        		return true;
        	}

        	@Override
            public ServerConnection createServerConnection(ServerConfiguration metaproject) {
                return new OSGiServletConnection(context);
            }
            
        };
        registration = context.registerService(ServerConnectionFactory.class.getCanonicalName(), factory, null);
    }

    public void stop(BundleContext context) {
        registration.unregister();
    }

}
