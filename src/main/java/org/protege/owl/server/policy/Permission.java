package org.protege.owl.server.policy;

import java.util.Map;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.UserId;

public class Permission {
    private Map<Operation, UserContainer> permissionMap;
    
    public Permission(Map<Operation, UserContainer> permissionMap) {
        this.permissionMap = permissionMap;
    }
    
    
    public boolean isAllowed(UserDatabase db, UserId owner, UserId requestingUser, Operation op) {
        UserContainer container = permissionMap.get(op);
        return container != null && container.contains(db, owner, requestingUser);
    }

}
