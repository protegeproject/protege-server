package org.protege.owl.server.connect;

import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.ServerRequests;

import java.rmi.Remote;

import edu.stanford.protege.metaproject.api.ClientConfiguration;
import edu.stanford.protege.metaproject.api.UserId;

public interface RemoteServer extends ServerRequests, Remote {

    ClientConfiguration getClientConfiguration(UserId userId) throws ServerRequestException;
}
