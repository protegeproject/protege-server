package org.protege.owl.server.connect.local;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ServerTransport;

public interface LocalTransport extends ServerTransport {

    LocalClient getClient(AuthToken token);
    
    void registerObject(String key, Object o);
    
    Object getRegisteredObject(String key);
}
