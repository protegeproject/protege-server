package org.protege.owl.server.api.exception;

import org.protege.owl.server.versioning.OntologyDocumentRevision;
import org.protege.owl.server.versioning.api.ChangeHistory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ConflictException extends OWLServerException {
    private static final long serialVersionUID = 4355727797287471775L;
    private ChangeHistory conflicts;
    
    public ConflictException(ChangeHistory conflicts) {
        super(getConflictMessage(conflicts));
        this.conflicts = conflicts;
    }
    
    public ChangeHistory getConflicts() {
        return conflicts;
    }

    
    
    private static String getConflictMessage(ChangeHistory conflicts) {
        int totalSize = getTotalConflicts(conflicts);
        if (totalSize == 1) {
            return "Conflict found";
        }
        else {
            return "" + totalSize + " conflicts found.";
        }
    }

    private static int getTotalConflicts(ChangeHistory conflicts) {
        OWLOntology fakeOntology;
        try {
            fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
        }
        catch (OWLOntologyCreationException ooce) {
            throw new RuntimeException("Could not create empty ontology??", ooce);
        }
        int size = 0;
        for (OntologyDocumentRevision revision = conflicts.getStartRevision();
                revision.compareTo(conflicts.getEndRevision()) < 0;
                revision = revision.next()) {
            size += conflicts.cropChanges(revision, revision.next()).getChanges(fakeOntology).size();
        }
        return size;
    }

 
}
