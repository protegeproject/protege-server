package org.protege.owl.server.api.server;

import org.protege.owl.server.api.client.RemoteOntologyDocument;

public interface ServerOntologyDocument extends ServerDocument {

    public RemoteOntologyDocument createRemoteDocument(String scheme, String host, int port);
}
