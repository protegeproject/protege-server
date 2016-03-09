package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;

@Deprecated
public class ChangeNormalizer {


    
    public static List<OWLOntologyChange> normalizeChangeDelta(List<OWLOntologyChange> changes) {
        return new ChangeNormalizer().run(changes);
    }
    
    private ChangeNormalizer() {

    }
    
    public List<OWLOntologyChange> run(List<OWLOntologyChange> changes) {
        CollectingChangeVisitor visitor = CollectingChangeVisitor.collectChanges(changes);
        List<OWLOntologyChange> normalizedChanges = new ArrayList<OWLOntologyChange>();
        if (visitor.getLastOntologyIDChange() != null) {
            normalizedChanges.add(visitor.getLastOntologyIDChange());
        }
        normalizedChanges.addAll(visitor.getLastImportChangeMap().values());
        normalizedChanges.addAll(visitor.getLastOntologyAnnotationChangeMap().values());
        normalizedChanges.addAll(visitor.getLastAxiomChangeMap().values());
        return normalizedChanges;
    }

}
