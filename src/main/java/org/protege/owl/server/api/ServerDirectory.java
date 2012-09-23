package org.protege.owl.server.api;



public interface ServerDirectory extends ServerDocument {
    public RemoteServerDirectory createRemoteDocument(final String scheme, final String host, final int port);
}
