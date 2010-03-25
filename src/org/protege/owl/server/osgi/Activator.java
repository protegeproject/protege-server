package org.protege.owl.server.osgi;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.protege.owl.server.api.Server;

public class Activator implements BundleActivator {
    private Logger logger = Logger.getLogger(Activator.class);
    private OSGiConnection connection;

    @Override
    public void start(final BundleContext context) throws Exception {
        ServiceReference sr = context.getServiceReference(Server.class.toString());
        if (sr != null) {
            startServer(context, (Server) context.getService(sr));
        }
        else {
            context.addServiceListener(new ServiceListener() {
                @Override
                public void serviceChanged(ServiceEvent event) {
                    if (event.getType() == ServiceEvent.REGISTERED 
                            && event.getServiceReference().isAssignableTo(context.getBundle(), Server.class.toString())) {
                        try {
                            startServer(context, (Server) context.getService(event.getServiceReference()));
                        } catch (IOException e) {
                            logger.warn("Exception caught trying to start the server", e);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (connection != null) {
            connection.dispose();
        }
    }
    
    private void startServer(BundleContext context, Server server) throws IOException {
        OSGiConnection connection = null;
        try {
            Class clz = Class.forName("org.protege.owl.server.connection.servlet.OSGiServletConnection");
            connection = (OSGiConnection) clz.newInstance();
        }
        catch (Throwable t) {
            logger.info("Servlet based connection failed");
        }
        if (connection != null) {
            connection.setBundleContext(context);
            connection.initialize(server);
        }
    }

}
