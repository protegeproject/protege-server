package org.protege.owl.server.connection.servlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerFactory;
import org.semanticweb.owlapi.model.OWLOntology;

public class ServletActivator implements BundleActivator {
    private ServiceRegistration registration;

    public void start(final BundleContext context) {
        ServerFactory factory = new ServerFactory() {

            public Server createServer(OWLOntology metaproject) {
                return null;
            }

            public ServerConnection createServerConnection(OWLOntology metaproject) {
                return new OSGiServletConnection(context);
            }
            
        };
        registration = context.registerService(ServerFactory.class.getCanonicalName(), factory, null);
    }

    public void stop(BundleContext context) {
        registration.unregister();
    }

}
