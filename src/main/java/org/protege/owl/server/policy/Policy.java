package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.UserId;

public class Policy {
    private Map<ProtectedObject, Permission> permissionMap = new HashMap<ProtectedObject, Permission>();
    
    public void addPolicyEntry(String id, Permission permission) {
        if (id.startsWith("/")) {
            permissionMap.put(new ProtectedServerDocument(new ServerPath(id)), permission);
        }
        else {
            for (ProtectedEnumeratedObjects pObject : ProtectedEnumeratedObjects.values()) {
                if (pObject.getPolicyRepresentation().equals(id)) {
                    permissionMap.put(pObject, permission);
                    break;
                }
            }
        }
    }
    
    void write(Writer writer) throws IOException {
        for (Entry<ProtectedObject, Permission> entry : permissionMap.entrySet()) {
            ProtectedObject po = entry.getKey();
            Permission perm = entry.getValue();
            perm.write(writer);
            writer.write("\n\t on /");
            writer.write(po.getPolicyRepresentation());
            writer.write(';');
        }
    }
    
    public boolean checkPermission(UserDatabase db, UserId requestingUser, ProtectedObject pObject, Operation op) {
        if (pObject instanceof ProtectedServerDocument) {
            return checkPermission(db, requestingUser, ((ProtectedServerDocument) pObject).getServerPath(), op);
        }
        return isDirectlyAllowed(db, requestingUser, pObject, op);
    }
    
    public boolean checkPermission(UserDatabase db, UserId requestingUser, ServerPath path, Operation op) {
        return isDirectlyAllowed(db, requestingUser, path, op) && checkReadParents(db, requestingUser, path);
    }
    
    private boolean isDirectlyAllowed(UserDatabase db, UserId requestingUser, ServerPath path, Operation op) {
        return isDirectlyAllowed(db, requestingUser, new ProtectedServerDocument(path), op);
    }
    
    private boolean isDirectlyAllowed(UserDatabase db, UserId requestingUser, ProtectedObject pObject, Operation op) {
        Permission p = permissionMap.get(pObject);
        if (p == null) {
            return true;
        }
        return p.isAllowed(db, requestingUser, op);
    }
    
    private boolean checkReadParents(UserDatabase db, UserId requestingUser, ServerPath p) {
        if (p.isRoot()) {
            return true;
        }
        ServerPath parent = p.getParent();
        return parent.isRoot() || (isDirectlyAllowed(db, requestingUser, p, Operation.READ) && checkReadParents(db, requestingUser, parent));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Policy)) {
            return false;
        }
        Policy other = (Policy) obj;
        return permissionMap.equals(other.permissionMap);
    }
    
    @Override
    public int hashCode() {
        return 42 + permissionMap.hashCode();
    }
    
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            write(writer);
        }
        catch (IOException ioe) {
            return "<A funny policy (io error occured in printout)>";
        }
        return writer.toString();
    }

}
