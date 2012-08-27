package org.protege.owl.server.api.exception;

import java.util.List;

import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ConflictException extends OWLServerException {
    private static final long serialVersionUID = 4355727797287471775L;
    
    private static String getConflictMessage(List<OWLOntologyChange> conflicts) {
        if (conflicts.size() == 1) {
            return "Conflict found";
        }
        else {
            return "" + conflicts.size() + " conflicts found.";
        }
        
    }
    
    public ConflictException(DocumentFactory factory, List<OWLOntologyChange> conflicts) {
        super(getConflictMessage(conflicts));
        factory.createChangeDocument(conflicts, new ChangeMetaData("Conflicts"), OntologyDocumentRevision.START_REVISION);
    }
    
    
    

}
