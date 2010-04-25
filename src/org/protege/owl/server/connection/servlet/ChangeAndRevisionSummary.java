/**
 * 
 */
package org.protege.owl.server.connection.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.util.AxiomToChangeConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ChangeAndRevisionSummary {
    private List<OWLOntologyChange> changes;
    private Map<IRI,Integer> revisions;
    
    public List<OWLOntologyChange> getChanges() {
        return changes;
    }
    public void setChanges(List<OWLOntologyChange> changes) {
        this.changes = changes;
    }
    public Map<IRI, Integer> getRevisions() {
        return revisions;
    }
    public void setRevisions(Map<IRI, Integer> revisions) {
        this.revisions = revisions;
    }
    
    public static ChangeAndRevisionSummary getChanges(Set<OWLOntology> localOntologies, OWLOntology changeOntology) {
        AxiomToChangeConverter converter = new AxiomToChangeConverter(changeOntology, localOntologies);
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLAxiom axiom : changeOntology.getAxioms()) {
            axiom.accept(converter);
            OWLOntologyChange change = converter.getChange();
            if (change != null) {
                changes.add(change);
            }
        }
        ChangeAndRevisionSummary clientChanges = new ChangeAndRevisionSummary();
        clientChanges.setChanges(changes);
        clientChanges.setRevisions(converter.getRevisionMap());
        return clientChanges;
    }
}