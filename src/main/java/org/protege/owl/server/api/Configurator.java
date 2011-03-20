package org.protege.owl.server.api;

import org.protege.owl.server.configuration.ServerConfiguration;

/**
 * An implementation of this interface is responsible for configuring a server.  It is informed (via the 
 * OSGi infrastructure) of available server backends, conflict managers and ServerConnection implementations.
 * If if can find a combination of these elements that are consistent with the specified configuration it will 
 * build and start a server meeting those specifications.
 * @author tredmond
 *
 */
public interface Configurator {
    void setConfiguration(ServerConfiguration configuration);

    Server getCurrentServer();
    
    /**
     * This method is used by the OSGi infrastructure to inform the Configurator of an
     * available server backend, conflict manager or server connection.
     * @param factory a factory capable of building a server backend, conflict manager or server connection.
     */
    void addServerFactory(ServerFactory factory);

    /**
     * This method is used by the OSGi infrastructure to inform the Configurator that
     * a particular server backend, conflict manager or server is  no longer available.
     * @param factory a factory capable of building a server backend, conflict manager or server connection.
     */
    void removeServerFactory(ServerFactory factory);

    void start();
    
    void stop();

    boolean isReady();
}

