package org.protege.owl.server.api;

import java.io.Serializable;

public class RevisionPointer implements Serializable {
    private static final long serialVersionUID = 8902418724621318750L;

    private RevisionPointerType type;
    private OntologyDocumentRevision revision;

    
    public static final RevisionPointer HEAD_REVISION;
    static {
        HEAD_REVISION = new RevisionPointer();
        HEAD_REVISION.type = RevisionPointerType.HEAD;
    }
    
    public static final RevisionPointer START_REVISION = OntologyDocumentRevision.START_REVISION.asPointer();

    private RevisionPointer() {

    }

    public RevisionPointer(OntologyDocumentRevision revision) {
        this.revision = revision;
        this.type     = RevisionPointerType.DOCUMENT_REVISION;
    }
    
    public RevisionPointerType getType() {
        return type;
    }
    
    public boolean isSymbolic() {
        return type != RevisionPointerType.DOCUMENT_REVISION;
    }
    
    public boolean isOntologyDocumentRevision() {
        return type == RevisionPointerType.DOCUMENT_REVISION;
    }
    
    public OntologyDocumentRevision asOntologyDocumentRevision() {
        if (isSymbolic()) {
            throw new IllegalArgumentException("Programmer error: tried to unravel a symbolic document revision");
        }
        return revision;
    }
    
    @Override
    public String toString() {
        switch (type) {
        case DOCUMENT_REVISION:
            return revision.toString();
        case HEAD:
            return "Head Revision";
        default:
           return "Strange Revision Type";     
        }
    }

    /**
     * Revision pointer types
     */
    public enum RevisionPointerType {
        DOCUMENT_REVISION, HEAD
    }
}
