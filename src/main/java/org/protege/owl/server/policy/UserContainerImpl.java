package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.Writer;
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
    
    
    @Override
    public void write(Writer writer) throws IOException {
        if (allowOwner) {
            writer.write("\t\tOwner\n");
        }
        if (!groups.isEmpty()) {
            writer.write("\t\tGroup");
            for (Group group : groups) {
                writer.write(" ");
                writer.write(group.getGroupName());
            }
            writer.write("\n");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserContainerImpl)) {
            return false;
        }
        UserContainerImpl other = (UserContainerImpl) obj;
        return allowOwner == other.allowOwner && groups.equals(other.groups);
    }
    
    @Override
    public int hashCode() {
        return groups.hashCode() + (allowOwner ? 42 : 0);
    }
}
