package org.protege.owl.server.policy;

import org.protege.owl.server.api.ServerPath;

public class ProtectedServerDocument implements ProtectedObject {
    private ServerPath path;
    
    public ProtectedServerDocument(ServerPath path) {
        this.path = path;
    }
    
    public ServerPath getServerPath() {
        return path;
    }
    
    @Override
    public String getPolicyRepresentation() {
        return path.pathAsString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProtectedServerDocument)) {
            return false;
        }
        ProtectedServerDocument other = (ProtectedServerDocument) obj;
        return path.equals(other.path);
    }
    
    @Override
    public int hashCode() {
        return path.hashCode();
    }
    
    @Override
    public String toString() {
        return path.toString();
    }
}
