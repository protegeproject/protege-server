package org.protege.owl.server.osgi;

import org.osgi.framework.BundleContext;

public interface OSGiAware {

    void activate(BundleContext context);
    
    void deactivate(BundleContext context);
}
