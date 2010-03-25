package org.protege.owl.server.osgi;

import org.osgi.framework.BundleContext;
import org.protege.owl.server.api.ServerConnection;

public interface OSGiConnection extends ServerConnection {
    void setBundleContext(BundleContext context);
}
