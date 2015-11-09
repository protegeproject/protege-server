package org.protege.owl.server.render;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class DiffsByEntity {
    private Map<OWLEntity, Set<OWLOntologyChange>> referencedEntityMap  = new TreeMap<OWLEntity, Set<OWLOntologyChange>>();
    private Map<IRI, Set<OWLOntologyChange>>       referencedIRIMap     = new TreeMap<IRI, Set<OWLOntologyChange>>();
    private Set<OWLOntologyChange>                 uncategorizedChanges = new TreeSet<OWLOntologyChange>();

    public DiffsByEntity(List<OWLOntologyChange> changes) {
        GetAxiomSourceVisitor visitor = new GetAxiomSourceVisitor();
        for (OWLOntologyChange change : changes) {
            if (change instanceof OWLAxiomChange) {
                visitor.reset();
                OWLAxiom axiom = ((OWLAxiomChange) change).getAxiom();
                axiom.accept(visitor);
                for (OWLEntity e : visitor.getReferencedEntities()) {
                    addReference(e, change);
                }
                if (visitor.getReferencedEntities().isEmpty() && visitor.getReferencedIri() != null) {
                    addReference(visitor.getReferencedIri(), change);
                }
                else if (visitor.getReferencedEntities().isEmpty()) {
                    uncategorizedChanges.add(change);
                }
            }
        }
    }
    
    private void addReference(OWLEntity e, OWLOntologyChange change) {
        Set<OWLOntologyChange> changes = referencedEntityMap.get(e);
        if (changes == null) {
            changes = new HashSet<OWLOntologyChange>();
            referencedEntityMap.put(e, changes);
        }
        changes.add(change);        
    }

    private void addReference(IRI iri, OWLOntologyChange change) {
        Set<OWLOntologyChange> changes = referencedEntityMap.get(iri);
        if (changes == null) {
            changes = new HashSet<OWLOntologyChange>();
            referencedIRIMap.put(iri, changes);
        }
        changes.add(change);        
    }
    
    public Map<OWLEntity, Set<OWLOntologyChange>> getReferencedEntityMap() {
        return referencedEntityMap;
    }
    
    public Map<IRI, Set<OWLOntologyChange>> getReferencedIRIMap() {
        return referencedIRIMap;
    }
    
    public Set<OWLOntologyChange> getUncategorizedChanges() {
        return uncategorizedChanges;
    }
}
