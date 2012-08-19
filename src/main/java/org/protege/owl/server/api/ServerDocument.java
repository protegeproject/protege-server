package org.protege.owl.server.api;


public interface ServerDocument extends Comparable<ServerDocument> {
    RemoteServerDocument createRemoteDocument(String scheme, String host, int port);
    
    ServerPath getServerPath();
}
