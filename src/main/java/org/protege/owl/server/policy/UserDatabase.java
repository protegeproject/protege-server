package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.api.User;

/*
 * ToDo Add token expiration...
 */

public class UserDatabase {
    private Map<User, String> passwordMap = new TreeMap<User, String>();
    private Map<User, Collection<Group>> userToGroupsMap = new TreeMap<User, Collection<Group>>();
    private Map<User, SimpleAuthToken> tokenMap = new TreeMap<User, SimpleAuthToken>();
    
    
    public UserDatabase() {
        Map<Operation, UserContainer> permissionMap = new TreeMap<Operation, UserContainer>();
        permissionMap.put(Operation.READ, UserContainer.EVERYONE);
        permissionMap.put(Operation.WRITE, UserContainer.EVERYONE);
    }
    
    public User addUser(String userName, String password) {
        User u = new User(userName);
        passwordMap.put(u, password);
        return u;
    }
    
    public void addGroup(User u, Group g) {
        Collection<Group> groups = userToGroupsMap.get(u);
        if (groups == null) {
            groups = new TreeSet<Group>();
            userToGroupsMap.put(u, groups);
        }
        groups.add(g);
    }
    
    void write(Writer writer) throws IOException {
        for (Entry<User, String> entry : passwordMap.entrySet()) {
            User u = entry.getKey();
            String password = entry.getValue();
            writer.write("User: ");
            writer.write(u.getUsername());
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
    
    public Collection<Group> getGroups(User u) {
        Collection<Group> groups = userToGroupsMap.get(u);
        if (groups == null) {
            return Collections.emptySet();
        }
        return new TreeSet<Group>(groups);
    }
    
    public Collection<User> getUsers() {
        return passwordMap.keySet();
    }
    
    public boolean checkPassword(User u, String password) {
        String actualPassword = passwordMap.get(u);
        return actualPassword != null && actualPassword.equals(password);
    }
    
    public SimpleAuthToken getToken(User u) {
        return tokenMap.get(u);
    }
    
    public void addToken(SimpleAuthToken token) {
        tokenMap.put(token.getUser(), token);
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
