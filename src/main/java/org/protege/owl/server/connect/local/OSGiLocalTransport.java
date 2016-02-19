package org.protege.owl.server.connect.local;

import java.io.IOException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.protege.owl.server.api.server.Server;

public class OSGiLocalTransport extends LocalTransportImpl {
    private BundleContext context;
    private ServiceRegistration serviceRegistration;
    
    public void setBundleContext(BundleContext context) {
        this.context = context;
    }
    
    @Override
    public void start(Server server) throws IOException {
        super.start(server);
        serviceRegistration = context.registerService(LocalTransport.class, this, null);
    }
    
    @Override
    public void dispose() {
        if (serviceRegistration != null) {
           serviceRegistration.unregister();
        }
        super.dispose();
    }

}
