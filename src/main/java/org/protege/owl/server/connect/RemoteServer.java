package org.protege.owl.server.connect;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.exception.ServerRequestException;

import edu.stanford.protege.metaproject.api.ClientConfiguration;
import edu.stanford.protege.metaproject.api.UserId;

public interface RemoteServer extends Server {

    ClientConfiguration getClientConfiguration(UserId userId) throws ServerRequestException;
}
