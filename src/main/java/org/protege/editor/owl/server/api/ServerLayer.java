package org.protege.editor.owl.server.api;

import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.User;

import org.protege.editor.owl.server.api.exception.OWLServerException;

import java.util.ArrayList;
import java.util.List;

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

    protected static String printLog(User requester, String operation, String message) {
        if (requester != null) {
            String template = "Receive [%s] request from %s (%s) - %s";
            return String.format(template, operation, requester.getName().get(), requester.getId().get(), message);
        }
        else {
            String template = "Receive [%s] request - %s";
            return String.format(template, operation, message);
        }
    }
}
