package org.protege.owl.server.api.client;

import org.protege.owl.server.api.server.ServerDirectory;

public interface RemoteServerDirectory extends RemoteServerDocument {
    
    ServerDirectory createServerDocument();
}
