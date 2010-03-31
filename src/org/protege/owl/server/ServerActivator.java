package org.protege.owl.server;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerFactory;

public class ServerActivator implements BundleActivator {
    private Logger logger = Logger.getLogger(ServerActivator.class);
    private BundleContext context;
    private Server server;
    private ServerConnection connection;
    private BundleActivator servletRegistration;
    
    
    private ServiceListener listener = new ServiceListener() {
        public void serviceChanged(ServiceEvent event) {
            if (event.getType() == ServiceEvent.REGISTERED) {
                ServiceReference reference = event.getServiceReference();
                Bundle me = context.getBundle();
                if (reference != null 
                        && reference.isAssignableTo(me, ServerFactory.class.getCanonicalName())) {
                    createConnectedServer(reference);
                }
                if (isReady()) {
                    context.removeServiceListener(listener);
                }
            }
        }
    };

    public void start(final BundleContext context) throws Exception {
        this.context = context;
        try {
            servletRegistration = (BundleActivator) Class.forName("org.protege.owl.server.connection.servlet.ServletActivator").newInstance();
            servletRegistration.start(context);
        }
        catch (Throwable t) {
            logger.info("Servlet based connection not registered: " + t);
        }
        createConnectedServer();
        if (!isReady()) {
            context.addServiceListener(listener);
        }
    }

    public void stop(BundleContext context) throws Exception {
        if (connection != null) {
            connection.dispose();
        }
        if (servletRegistration != null) {
            servletRegistration.stop(context);
        }
    }
    
    private void createConnectedServer() {
        try {
            for (ServiceReference sr : context.getServiceReferences(ServerFactory.class.getCanonicalName(), null)) {
                createConnectedServer(sr);
            }
        }
        catch (InvalidSyntaxException e) {
            logger.error("Programmer must have made a mistake!");
        }
    }
    
    private void createConnectedServer(ServiceReference sr) {
        ServerFactory factory = (ServerFactory) context.getService(sr);
        if (server == null) {
            server = factory.createServer(null);
        }
        if (connection == null) {
            connection = factory.createServerConnection(null);
        }
        if (isReady()) {
            try {
                connection.initialize(server);
                logger.info("Server Started!");
            }
            catch (Throwable e) {
                logger.error("Server could not be connected using connection " + connection, e);
                connection = null;
            }
        }
    }
    
    public boolean isReady() {
        return server != null && connection != null;
    }
}
