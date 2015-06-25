package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.protege.owl.server.api.User;

public class UserContainerImpl implements UserContainer {
    private Set<User> users;
    private Set<Group> groups;
    
    public UserContainerImpl(Set<User> users, Set<Group> groups) {
        this.users  = users;
        this.groups = groups;
    }

    
    public boolean contains(UserDatabase db, User requestingUser) {
        if (users.contains(requestingUser)) {
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
        if (!users.isEmpty()) {
            writer.write("\n\t\tUser");
            for (User u : users) {
                writer.write(' ');
                writer.write(u.getUserName());
            }
            writer.write(';');
        }
        if (!groups.isEmpty()) {
            writer.write("\n\t\tGroup");
            for (Group group : groups) {
                writer.write(" ");
                writer.write(group.getGroupName());
            }
            writer.write(';');
        }
        writer.write("\n");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserContainerImpl)) {
            return false;
        }
        UserContainerImpl other = (UserContainerImpl) obj;
        return users.equals(other.users) && groups.equals(other.groups);
    }
    
    @Override
    public int hashCode() {
        return users.hashCode() + groups.hashCode() + 42;
    }
}
