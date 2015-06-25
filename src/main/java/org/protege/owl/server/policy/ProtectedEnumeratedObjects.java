package org.protege.owl.server.policy;

public enum ProtectedEnumeratedObjects implements ProtectedObject {
    SERVER("Server");
    
    private String policyRepresentation;
    
        ProtectedEnumeratedObjects(String policyRepresentation) {
        this.policyRepresentation = policyRepresentation;
    }
    
    @Override
    public String getPolicyRepresentation() {
        return policyRepresentation;
    }

}
