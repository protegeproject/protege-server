package org.protege.owl.server.policy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.api.UserId;

/*
 * ToDo Add token expiration...
 */

public class UserDatabase {
    private Map<UserId, String> passwordMap = new TreeMap<UserId, String>();
    private Map<UserId, Collection<Group>> userToGroupsMap = new TreeMap<UserId, Collection<Group>>();
    private Map<UserId, SimpleAuthToken> tokenMap = new TreeMap<UserId, SimpleAuthToken>();
    
    
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
    
    public Collection<Group> getGroups(UserId u) {
        return userToGroupsMap.get(u);
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
    

}
