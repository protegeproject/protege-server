package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.ServerListener;
import org.protege.owl.server.api.server.TransportHandler;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

public abstract class ServerLayer implements Server {

    private List<ServerListener> listeners = new ArrayList<>();

    /**
     * Get the server configuration
     *
     * @return Server configuration
     */
    protected abstract ServerConfiguration getConfiguration();

    public abstract void setTransport(TransportHandler transport) throws OWLServerException;

    public void addServerListener(ServerListener listener) {
        listeners.add(listener);
    }

    public void removeServerListener(ServerListener listener) {
        int index = listeners.indexOf(listener);
        if (index != -1) {
            listeners.remove(index);
        }
    }
}
