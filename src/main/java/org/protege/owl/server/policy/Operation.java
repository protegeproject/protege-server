package org.protege.owl.server.policy;

@Deprecated
public final class Operation implements Comparable<Operation> {
    public static final Operation READ   = new Operation("read");
    public static final Operation WRITE  = new Operation("write");
    public static final Operation DELETE = new Operation("delete");
    
    private String name;
    
    public Operation(String name) {
        this.name = name.toUpperCase();
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Operation)) {
            return false;
        }
        return name.equals(((Operation) obj).getName());
    }
    
    @Override
    public int compareTo(Operation o) {
        return name.compareTo(o.name);
    }
    
    @Override
    public String toString() {
        return name;
    }

}
