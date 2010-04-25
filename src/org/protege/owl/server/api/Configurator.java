package org.protege.owl.server.api;

import org.protege.owl.server.configuration.ServerConfiguration;

public interface Configurator {
    void setConfiguration(ServerConfiguration configuration);

    Server getCurrentServer();
    
    void addServerFactory(ServerFactory factory);

    void removeServerFactory(ServerFactory factory);

    void start();
    
    void stop();

    boolean isReady();
}

