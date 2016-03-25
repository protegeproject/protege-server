package org.protege.owl.server.changes.api;

public interface ServerOntologyDocument extends ServerDocument {

    public RemoteOntologyDocument createRemoteDocument(String scheme, String host, int port);
}
