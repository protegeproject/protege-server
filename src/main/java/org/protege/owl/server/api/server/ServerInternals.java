package org.protege.owl.server.api.server;

import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.exception.OWLServerException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

public interface ServerInternals {

    @Deprecated
    DocumentFactory getDocumentFactory();

    @Deprecated
    InputStream getConfigurationInputStream(String fileName) throws OWLServerException;

    @Deprecated
    OutputStream getConfigurationOutputStream(String fileName) throws OWLServerException;

    @Deprecated
    InputStream getConfigurationInputStream(ServerDocument doc, String extension) throws OWLServerException;

    @Deprecated
    OutputStream getConfigurationOutputStream(ServerDocument doc, String extension) throws OWLServerException;

    @Deprecated
    void setTransports(Collection<ServerTransport> transports);

    @Deprecated
    Collection<ServerTransport> getTransports();

    void setTransport(TransportHandler transport) throws OWLServerException;

    void addServerListener(ServerListener listener);

    void removeServerListener(ServerListener listener);

    void shutdown();
}
