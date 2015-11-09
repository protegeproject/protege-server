package org.protege.owl.server.connect.local;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.server.ServerInternals;
import org.protege.owl.server.api.server.ServerTransport;

public interface LocalTransport extends ServerTransport, ServerInternals {

    LocalClient getClient(AuthToken token);
    
    void registerObject(String key, Object o);
    
    Object getRegisteredObject(String key);
}
