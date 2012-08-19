package org.protege.owl.server.api;


public interface ServerOntologyDocument extends ServerDocument {
    @Override
    public RemoteOntologyDocument createRemoteDocument(String scheme, String host, int port);
}
