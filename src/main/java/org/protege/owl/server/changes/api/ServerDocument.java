package org.protege.owl.server.changes.api;

import org.protege.owl.server.api.server.ServerPath;

public interface ServerDocument extends Comparable<ServerDocument> {

    RemoteServerDocument createRemoteDocument(String scheme, String host, int port);

    ServerPath getServerPath();

    Object getProperty(String key);

    void setProperty(String key, Object value);
}
