package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.List;

import org.protege.owl.server.api.ClientConnection;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class AbstractClientConnection implements ClientConnection {
    private OWLOntologyManager manager;
    private List<OWLOntologyChange> uncommittedChanges = new ArrayList<OWLOntologyChange>();
    private OWLOntologyChangeListener uncommittedChangesListener = new OWLOntologyChangeListener() {

        @Override
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            uncommittedChanges.addAll(changes);
        }
        
    };
    
    protected AbstractClientConnection(OWLOntologyManager manager) {
        this.manager = manager;
        manager.addOntologyChangeListener(uncommittedChangesListener);
    }
    
    @Override
    public OWLOntologyManager getOntologyManager() {
        return manager;
    }
    
    protected void clearUncommittedChanges(OWLOntology ontology) {
        uncommittedChanges.removeAll(getUncommittedChanges(ontology));
    }
    
    public List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLOntologyChange change : uncommittedChanges) {
            if (change.getOntology().equals(ontology)) {
                changes.add(change);
            }
        }
        return changes;
    }
    
    @Override
    public void dispose() {
        manager.removeOntologyChangeListener(uncommittedChangesListener);
    }

}
