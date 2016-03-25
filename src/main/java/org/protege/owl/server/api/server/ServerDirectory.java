package org.protege.owl.server.api.server;

import org.protege.owl.server.api.client.RemoteServerDirectory;

public interface ServerDirectory extends ServerDocument {

    public RemoteServerDirectory createRemoteDocument(final String scheme, final String host, final int port);
}
