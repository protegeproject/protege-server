package org.protege.owl.server;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    private Logger logger = Logger.getLogger(Activator.class);

    private BundleActivator servletRegistration;
    
	@Override
	public void start(BundleContext context) throws Exception {
        try {
            servletRegistration = (BundleActivator) Class.forName("org.protege.owl.server.connection.servlet.ServletActivator").newInstance();
            servletRegistration.start(context);
        }
        catch (Throwable t) {
            logger.info("Servlet based connection not registered: " + t);
        }
	}

	@Override
	public void stop(BundleContext context) throws Exception {
        if (servletRegistration != null) {
            servletRegistration.stop(context);
        }
	}

}
