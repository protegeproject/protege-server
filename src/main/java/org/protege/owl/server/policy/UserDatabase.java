package org.protege.owl.server.policy;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;


public class UserDatabase {
    private Map<String, SimpleAuthToken> userMap = new TreeMap<String, SimpleAuthToken>();
    private Map<String, Group> groupMap = new TreeMap<String, Group>();
    private Map<SimpleAuthToken, Collection<Group>> userToGroupsMap = new TreeMap<SimpleAuthToken, Collection<Group>>();
    
    
    public void addUser(SimpleAuthToken u) {
        userMap.put(u.getUserId().getUserName(), u);
    }
    
    public void addGroup(SimpleAuthToken u, Group g) {
        addUser(u);
        groupMap.put(g.getGroupName(), g);
        Collection<Group> groups = userToGroupsMap.get(u);
        if (groups == null) {
            groups = new TreeSet<Group>();
            userToGroupsMap.put(u, groups);
        }
        groups.add(g);
    }
    
    public Collection<Group> getGroups() {
        return groupMap.values();
    }
    
    public Collection<Group> getGroups(SimpleAuthToken u) {
        return userToGroupsMap.get(u);
    }
    
    public Collection<SimpleAuthToken> getUsers() {
        return userMap.values();
    }
    
    public SimpleAuthToken getUser(String name) {
        return userMap.get(name);
    }
    

}
