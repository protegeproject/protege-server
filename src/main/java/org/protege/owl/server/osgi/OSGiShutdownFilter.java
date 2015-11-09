package org.protege.owl.server.osgi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.util.ServerFilterAdapter;

public class OSGiShutdownFilter extends ServerFilterAdapter {
    private Logger logger = Logger.getLogger(OSGiShutdownFilter.class.getCanonicalName());
    
    private BundleContext context;
    
    public OSGiShutdownFilter(Server delegate, BundleContext context) {
        super(delegate);
        this.context = context;
    }
    
    @Override
    public void shutdown(AuthToken u) throws OWLServerException {
        super.shutdown(u);
        Bundle systemBundle = context.getBundle(0);
        try {
            systemBundle.stop();
        }
        catch (BundleException be) {
            logger.log(Level.SEVERE, "Exception caught trying to shutdown server.", be);
        }
    }

}
