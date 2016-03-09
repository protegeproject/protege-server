package org.protege.owl.server.policy;

import org.protege.owl.server.api.UserId;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * ToDo Add token expiration...
 */

@Deprecated
public class UserDatabase {
    private Map<UserId, String> passwordMap = new TreeMap<UserId, String>();
    private Map<UserId, Collection<Group>> userToGroupsMap = new TreeMap<UserId, Collection<Group>>();
    private Map<UserId, SimpleAuthToken> tokenMap = new TreeMap<UserId, SimpleAuthToken>();
    
    
    public UserDatabase() {
        Map<Operation, UserContainer> permissionMap = new TreeMap<Operation, UserContainer>();
        permissionMap.put(Operation.READ, UserContainer.EVERYONE);
        permissionMap.put(Operation.WRITE, UserContainer.EVERYONE);
    }
    
    public UserId addUser(String userName, String password) {
        UserId u = new UserId(userName);
        passwordMap.put(u, password);
        return u;
    }
    
    public void addGroup(UserId u, Group g) {
        Collection<Group> groups = userToGroupsMap.get(u);
        if (groups == null) {
            groups = new TreeSet<Group>();
            userToGroupsMap.put(u, groups);
        }
        groups.add(g);
    }
    
    void write(Writer writer) throws IOException {
        for (Entry<UserId, String> entry : passwordMap.entrySet()) {
            UserId u = entry.getKey();
            String password = entry.getValue();
            writer.write("User: ");
            writer.write(u.getUserName());
            writer.write(" Password: ");
            writer.write(password);
            Collection<Group> groups = getGroups(u);
            if (!groups.isEmpty()) {
                writer.write("\n    Groups: ");
                for (Group group : groups) {
                    writer.write(' ');
                    writer.write(group.getGroupName());
                }
            }
            writer.write(" ;\n");
        }
    }
    
    public Collection<Group> getGroups(UserId u) {
        Collection<Group> groups = userToGroupsMap.get(u);
        if (groups == null) {
            return Collections.emptySet();
        }
        return new TreeSet<Group>(groups);
    }
    
    public Collection<UserId> getUsers() {
        return passwordMap.keySet();
    }
    
    public boolean checkPassword(UserId u, String password) {
        String actualPassword = passwordMap.get(u);
        return actualPassword != null && actualPassword.equals(password);
    }
    
    public SimpleAuthToken getToken(UserId u) {
        return tokenMap.get(u);
    }
    
    public void addToken(SimpleAuthToken token) {
        tokenMap.put(token.getUserId(), token);
    }
    
    public boolean isValid(SimpleAuthToken token) {
        return tokenMap.values().contains(token);
    }
    
    @Override
    public int hashCode() {
        return passwordMap.hashCode() + 42 * userToGroupsMap.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserDatabase)) {
            return false;
        }
        UserDatabase other = (UserDatabase) obj;
        return passwordMap.equals(other.passwordMap) && userToGroupsMap.equals(other.userToGroupsMap);
    }

}
