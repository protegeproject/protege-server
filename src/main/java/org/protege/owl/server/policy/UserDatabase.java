package org.protege.owl.server.policy;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;


public class UserDatabase {
    private Map<String, UserExt> userMap = new TreeMap<String, UserExt>();
    private Map<String, Group> groupMap = new TreeMap<String, Group>();
    private Map<UserExt, Collection<Group>> userToGroupsMap = new TreeMap<UserExt, Collection<Group>>();
    
    
    public void addUser(UserExt u) {
        userMap.put(u.getUserName(), u);
    }
    
    public void addGroup(UserExt u, Group g) {
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
    
    public Collection<Group> getGroups(UserExt u) {
        return userToGroupsMap.get(u);
    }
    
    public Collection<UserExt> getUsers() {
        return userMap.values();
    }
    
    public UserExt getUser(String name) {
        return userMap.get(name);
    }
    

}
