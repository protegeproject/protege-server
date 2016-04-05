package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.ServerRequests;

import edu.stanford.protege.metaproject.api.ClientConfiguration;
import edu.stanford.protege.metaproject.api.UserId;

public interface Server extends ServerRequests {

    ClientConfiguration getClientConfiguration(UserId userId) throws OWLServerException;
}
