package org.protege.owl.server.policy;

public class Group implements Comparable<Group> {
    private String groupName;
    
    public Group(String groupName) {
        this.groupName = groupName;
    }
    
    public String getGroupName() {
        return groupName;
    }

    @Override
    public int compareTo(Group o) {
        return groupName.compareTo(o.getGroupName());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group)) {
            return false;
        }
        Group other = (Group) obj;
        return groupName.equals(other.getGroupName());
    }
    
    @Override
    public int hashCode() {
        return groupName.hashCode();
    }
    
    @Override
    public String toString() {
        return "[Group " + groupName + "]";
    }
}
