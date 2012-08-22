package org.protege.owl.server.policy;

import java.util.Set;

import org.protege.owl.server.api.UserId;

public class UserContainerImpl implements UserContainer {
    private boolean allowOwner;
    private Set<Group> groups;
    
    
    
    public UserContainerImpl(boolean allowOwner, Set<Group> groups) {
        this.allowOwner = allowOwner;
        this.groups = groups;
    }

    
    public boolean contains(UserDatabase db, UserId owner, UserId requestingUser) {
        if (allowOwner && requestingUser.equals(owner)) {
            return true;
        }
        for (Group requestingGroup : db.getGroups(requestingUser)) {
            if (groups.contains(requestingGroup)) {
                return true;
            }
        }
        return false;
    }
}
