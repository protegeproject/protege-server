package org.protege.owl.server.api.client;

import org.protege.owl.server.api.server.ServerOntologyDocument;

public interface RemoteOntologyDocument extends RemoteServerDocument {

    ServerOntologyDocument createServerDocument();
}
