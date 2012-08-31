package org.protege.owl.server.connect.local;

import org.protege.owl.server.api.AuthToken;

public interface LocalTransport {

    LocalClient getClient(AuthToken token);
    
    void registerObject(String key, Object o);
    
    Object getRegisteredObject(String key);
}
