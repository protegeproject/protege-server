package org.protege.owl.server.changes.api;

public interface ServerDirectory extends ServerDocument {

    public RemoteServerDirectory createRemoteDocument(final String scheme, final String host, final int port);
}
