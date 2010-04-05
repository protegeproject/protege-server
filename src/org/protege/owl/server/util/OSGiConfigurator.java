package org.protege.owl.server.util;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.protege.owl.server.api.ServerConnectionFactory;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.configuration.ServerConfiguration;

public class OSGiConfigurator extends Configurator {
	private static Logger logger = Logger.getLogger(OSGiConfigurator.class);
    private BundleContext context;

    private ServiceListener listener = new ServiceListener() {
        public void serviceChanged(ServiceEvent event) {

            if (event.getType() == ServiceEvent.REGISTERED) {
            	ServiceReference reference = event.getServiceReference();
            	addServiceReference(reference);
            }
            else if (event.getType() == ServiceEvent.UNREGISTERING) {
            	ServiceReference reference = event.getServiceReference();
            	Bundle me = context.getBundle();
            	Object o = context.getService(reference);
                
                if (o instanceof ServerFactory) {
                	removeServerFactory((ServerFactory) context.getService(reference));
                }
                else if (o instanceof ServerConnectionFactory) {
                	removeServerConnectionFactory((ServerConnectionFactory) context.getService(reference));
                }
                else {
                	context.ungetService(reference);
                }
            }
        }
    };
    
    public OSGiConfigurator(BundleContext context, ServerConfiguration configuration) {
    	super(configuration);
    	this.context = context;
    	try {
    		ServiceReference[] srs = context.getServiceReferences(ServerFactory.class.getCanonicalName(), null);
    		if (srs != null) {
    			for (ServiceReference sr : srs)  {
    				addServiceReference(sr);
    			}
    		}
    		srs = context.getServiceReferences(ServerConnectionFactory.class.getCanonicalName(), null);
    		if (srs != null) {
    			for (ServiceReference sr : srs)  {
    				addServiceReference(sr);
    			}
    		}
    	}
    	catch (InvalidSyntaxException  e) {
    		logger.warn("Server configuration failed because of programmer error", e);
    	}
        context.addServiceListener(listener);
    }
    
    private void addServiceReference(ServiceReference reference) {
    	Object o = context.getService(reference);
    	if (o instanceof ServerFactory) {
    		addServerFactory((ServerFactory) o);
    	}
    	else if (o instanceof ServerConnectionFactory) {
    		addServerConnectionFactory((ServerConnectionFactory) o);
    	}
    	else {
    		context.ungetService(reference);
    	}
    }
}
