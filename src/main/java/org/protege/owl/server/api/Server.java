package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.ServerListener;
import org.protege.owl.server.api.server.ServerRequests;
import org.protege.owl.server.api.server.TransportHandler;

public interface Server extends ServerRequests {

    void setTransport(TransportHandler transport) throws OWLServerException;

    void addServerListener(ServerListener listener);

    void removeServerListener(ServerListener listener);
}
