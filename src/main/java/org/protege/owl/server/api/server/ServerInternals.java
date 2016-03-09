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

    /**
     * Inform the server and its filters about the transports that are being
     * used.
     * <p>
     * ServerFilters can use this to add their own functionality to the
     * transport mechanism and to determine that the initialization sequence has
     * been completed. This is used in conjuction with the ServerTransport.start
     * function. The right sequence is
     * 
     * <pre>
     * transport.start(server);
     * server.setTransports(transports);
     * </pre>
     * 
     * @param transports
     *            transports
     */
    void setTransports(Collection<ServerTransport> transports);

    Collection<ServerTransport> getTransports();

    void addServerListener(ServerListener listener);

    void removeServerListener(ServerListener listener);

    void shutdown();
}
