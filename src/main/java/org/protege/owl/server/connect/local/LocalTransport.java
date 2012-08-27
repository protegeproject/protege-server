package org.protege.owl.server.connect.local;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerTransport;

/*
 * ToDo - it would be nice to add a factory so that this auto-configures in OSGi.
 */
public class LocalTransport implements ServerTransport {
    private Server server;
    private Map<String, Object> registeredObjectMap = new TreeMap<String, Object>();

    @Override
    public void start(Server server) throws IOException {
        this.server = server;
    }
    
    public LocalClient getClient(AuthToken token) {
        return new LocalClient(token, server);
    }
    
    public void registerObject(String key, Object o) {
        registeredObjectMap.put(key, o);
    }

    public Object getRegisteredObject(String key) {
        return registeredObjectMap.get(key);
    }
    
    @Override
    public void dispose() {
        server = null;
        registeredObjectMap.clear();
    }

}
