package org.protege.owl.server.connection.servlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.configuration.ServerConfiguration;

public class ServletActivator implements BundleActivator {
    private ServiceRegistration registration;

    public void start(final BundleContext context) {
    	ServerFactory factory = new ServerFactory() {
        	
            @Override
            public boolean hasSuitableConnection(ServerConfiguration configuration) {
            	return true;
            }

            public ServerConnection createServerConnection(ServerConfiguration metaproject) {
                return new OSGiServletConnection(context);
            }
            
            @Override
            public boolean hasSuitableServer(ServerConfiguration configuration) {
            	return false;
            }

            @Override
            public Server createServer(ServerConfiguration configuration) {
            	return null;
            }

            @Override
            public boolean hasSuitableConflictManager(ServerConfiguration configuration) {
            	return false;
            }

            @Override
            public ConflictManager createConflictManager(ServerConfiguration configuration) {
            	return null;
            }
        };
        registration = context.registerService(ServerFactory.class.getCanonicalName(), factory, null);
    }

    public void stop(BundleContext context) {
        registration.unregister();
    }

}
