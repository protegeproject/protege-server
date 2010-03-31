package org.protege.owl.server.connection.servlet;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;

public class OSGiServletConnection implements ServerConnection {
    private Logger logger = Logger.getLogger(getClass());
    private Server server;
    private BundleContext context;
    private ServiceListener httpServiceListener = new ServiceListener() {
        public void serviceChanged(ServiceEvent event) {
            if (event.getType() == ServiceEvent.REGISTERED
                    && event.getServiceReference().isAssignableTo(context.getBundle(), HttpService.class.getCanonicalName())) {
                try {
                    registerServlets(event.getServiceReference());
                }
                catch (IOException ioe) {
                    logger.error("Exception found starting servlets", ioe);
                }
            }
        }
    };
    
    public OSGiServletConnection(BundleContext context) {
        this.context = context;
    }

    public void initialize(Server server) throws IOException {
        this.server = server;
        try {
            ServiceReference [] srs = context.getServiceReferences(HttpService.class.getCanonicalName(), null);
            if (srs != null) {
                for (ServiceReference sr : srs) {
                    registerServlets(sr);
                }
            }
        }
        catch (InvalidSyntaxException e) {
            logger.warn("Programmer error", e);
        }
        context.addServiceListener(httpServiceListener);
    }
    
    private void registerServlets(ServiceReference sr) throws IOException {
        HttpService http = (HttpService) context.getService(sr);
        try {
            http.registerServlet(OntologyListServlet.PATH, new OntologyListServlet(server), null, null);
            logger.info("Registered Servlet at " + OntologyListServlet.PATH);
            http.registerServlet(MarkedOntologyServlet.PATH, new MarkedOntologyServlet(server), null, null);
            logger.info("Resistered Servlet at " + MarkedOntologyServlet.PATH);
            http.registerServlet(OntologyDeltaServlet.PATH, new OntologyDeltaServlet(server), null, null);
            logger.info("Resistered Servlet at " + OntologyDeltaServlet.PATH);
            logger.info("Servlets registered");
        }
        catch (Throwable t) {
            IOException ioe = new IOException();
            ioe.initCause(t);
            throw ioe;
        }
    }
    
    public Object getUserToken() {
        // TODO Auto-generated method stub
        return null;
    }

    public void dispose() {
        // TODO Auto-generated method stub
    
    }

}
