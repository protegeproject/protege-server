package org.protege.owl.server.osgi;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.connection.servlet.OSGiServletConnection;
import org.protege.owl.server.protegedb.DatabaseServer;

public class Activator implements BundleActivator {
    private Logger logger = Logger.getLogger(Activator.class);
    private ServerConnection connection;

    @Override
    public void start(final BundleContext context) throws Exception {
        Server server = getServer();
        connection = getServerConnection(context, server);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (connection != null) {
            connection.dispose();
        }
    }
    
    // TODO make the following methods pluggable from the metaproject.
    private Server getServer() throws IOException, SQLException, ClassNotFoundException {
        return new DatabaseServer("jdbc:postgres://localhost/protege4", "protege", "troglodyte");
    }
    
    private ServerConnection getServerConnection(BundleContext context, Server server) throws IOException {
        ServerConnection connection = new OSGiServletConnection(context);
        connection.initialize(server);
        return connection;
    }
 
}
