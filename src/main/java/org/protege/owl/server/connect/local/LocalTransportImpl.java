package org.protege.owl.server.connect.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerDocument;
import org.protege.owl.server.api.server.ServerListener;
import org.protege.owl.server.api.server.ServerTransport;

/*
 * ToDo - it would be nice to add a factory so that this auto-configures in OSGi.
 */
public class LocalTransportImpl implements LocalTransport {

    public void shutdown() {
        server.shutdown();
    }

    private Server server;
    private Map<String, Object> registeredObjectMap = new TreeMap<String, Object>();

    @Override
    public void start(Server server) throws IOException {
        this.server = server;
    }
    
    @Override
    public LocalClient getClient(AuthToken token) {
        return new LocalClient(token, server);
    }
    
    @Override
    public void registerObject(String key, Object o) {
        registeredObjectMap.put(key, o);
    }

    @Override
    public Object getRegisteredObject(String key) {
        return registeredObjectMap.get(key);
    }
    
    @Override
    public void dispose() {
        server = null;
        registeredObjectMap.clear();
    }
    
    /*
     * Server internals methods so that plugins and such will not have to make a filter just to get access to these methods.
     */
    
    @Override
    public DocumentFactory getDocumentFactory() {
        return server.getDocumentFactory();
    }

    @Override
    public InputStream getConfigurationInputStream(String fileName) throws OWLServerException {
        return server.getConfigurationInputStream(fileName);
    }

    @Override
    public OutputStream getConfigurationOutputStream(String fileName) throws OWLServerException {
        return server.getConfigurationOutputStream(fileName);
    }

    @Override
    public InputStream getConfigurationInputStream(ServerDocument doc, String extension) throws OWLServerException {
        return server.getConfigurationInputStream(doc, extension);
    }

    @Override
    public OutputStream getConfigurationOutputStream(ServerDocument doc, String extension) throws OWLServerException {
        return server.getConfigurationOutputStream(doc, extension);
    }

    @Override
    public void setTransports(Collection<ServerTransport> transports) {
        server.setTransports(transports);
    }

    @Override
    public Collection<ServerTransport> getTransports() {
        return server.getTransports();
    }

    @Override
    public void addServerListener(ServerListener listener) {
        server.addServerListener(listener);
    }

    @Override
    public void removeServerListener(ServerListener listener) {
        server.removeServerListener(listener);
    }


}
