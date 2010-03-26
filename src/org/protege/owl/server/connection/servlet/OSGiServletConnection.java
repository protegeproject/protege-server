package org.protege.owl.server.connection.servlet;

import java.io.IOException;

import org.osgi.framework.BundleContext;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;

public class OSGiServletConnection implements ServerConnection {
    private Server server;
    private BundleContext context;
    
    public OSGiServletConnection(BundleContext context) {
        this.context = context;
    }

    @Override
    public void initialize(Server server) throws IOException {
        this.server = server;
    }
    
    @Override
    public Object getUserToken() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    
    }

}
