package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public abstract class AbstractServer implements Server {
    private OWLOntologyManager ontologyManager;
    private ConflictManager conflictManager;
    
    protected AbstractServer(OWLOntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }
    
    protected abstract boolean contains(IRI ontologyName, int revision, OWLAxiom axiom);
    protected abstract boolean contains(IRI ontologyName, int revision, OWLImportsDeclaration owlImport);
    protected abstract boolean contains(IRI ontologyName, int revision, OWLAnnotation annotation);
    
    @Override
    public OWLOntologyManager getOntologyManager() {
        return ontologyManager;
    }
    
    @Override
    public ConflictManager getConflictManager() {
        return conflictManager;
    }
    
    @Override
    public void setConflictManager(ConflictManager conflictManager) {
        this.conflictManager = conflictManager;
    }
    
    
    @Override
    public synchronized void applyChanges(Map<IRI, Integer> versions, List<OWLOntologyChange> changes) throws RemoteOntologyChangeException {
        changes = reduceChangeList(versions, changes);
        if (conflictManager != null) {
            conflictManager.validateChanges(versions, changes);
        }
        ontologyManager.applyChanges(changes);
    }
    
    @Override
    public List<OWLOntologyChange> reduceChangeList(Map<IRI, Integer> versions, List<OWLOntologyChange> changes) {
        changes = removeRedundantChanges(changes);
        List<OWLOntologyChange> reducedList = new ArrayList<OWLOntologyChange>();
        AcceptRejectChangeVisitor visitor = new AcceptRejectChangeVisitor(versions);
        for (OWLOntologyChange change : changes) {
            change.accept(visitor);
            if (visitor.isAccepted()) {
                reducedList.add(change);
            }
        }
        return reducedList;
    }
    
    private class AcceptRejectChangeVisitor implements OWLOntologyChangeVisitor {
        private boolean accepted;
        private Map<IRI, Integer> versions;
        
        public AcceptRejectChangeVisitor(Map<IRI, Integer> versions) {
            this.versions = versions;
        }
        
        public boolean isAccepted() {
            return accepted;
        }

        @Override
        public void visit(AddAxiom change) {
            IRI ontologyName = change.getOntology().getOntologyID().getOntologyIRI();
            int version = versions.get(ontologyName);
            accepted = !contains(ontologyName, version, change.getAxiom());
        }

        @Override
        public void visit(RemoveAxiom change) {
            IRI ontologyName = change.getOntology().getOntologyID().getOntologyIRI();
            int version = versions.get(ontologyName);
            accepted = contains(ontologyName, version, change.getAxiom());
        }

        @Override
        public void visit(SetOntologyID change) {
            accepted = false;
        }

        @Override
        public void visit(AddImport change) {
            IRI ontologyName = change.getOntology().getOntologyID().getOntologyIRI();
            int version = versions.get(ontologyName);
            accepted = !contains(ontologyName, version, change.getImportDeclaration());
        }

        @Override
        public void visit(RemoveImport change) {
            IRI ontologyName = change.getOntology().getOntologyID().getOntologyIRI();
            int version = versions.get(ontologyName);
            accepted = contains(ontologyName, version, change.getImportDeclaration());    
        }

        @Override
        public void visit(AddOntologyAnnotation change) {
            IRI ontologyName = change.getOntology().getOntologyID().getOntologyIRI();
            int version = versions.get(ontologyName);
            accepted = !contains(ontologyName, version, change.getAnnotation());
        }

        @Override
        public void visit(RemoveOntologyAnnotation change) {
            IRI ontologyName = change.getOntology().getOntologyID().getOntologyIRI();
            int version = versions.get(ontologyName);
            accepted = contains(ontologyName, version, change.getAnnotation());
        }
        
    }
    
    protected static List<OWLOntologyChange> removeRedundantChanges(List<OWLOntologyChange> changes) {
        List<OWLOntologyChange> result = new ArrayList<OWLOntologyChange>();
        for (int i = 0; i < changes.size(); i++) {
            OWLOntologyChange change1 = changes.get(i);
            if (!(change1 instanceof SetOntologyID)) {
                boolean overlap = false;
                for (int j = i + 1; j < changes.size(); j++) {
                    OWLOntologyChange change2 = changes.get(j);
                    if (overlappingChange(change1, change2)) {
                        overlap = true;
                        break;
                    }
                }
                if (!overlap) {
                    result.add(change1);
                }
            }
        }
        return result;
    }
    
    private static boolean overlappingChange(OWLOntologyChange change1, OWLOntologyChange change2) {
        if (change1.getOntology().equals(change2.getOntology())) {
            OverlapVisitor visitor = new OverlapVisitor(change1);
            change2.accept(visitor);
            return visitor.isOverlapping();
        }
        return false;
    }
    
    private static class OverlapVisitor implements OWLOntologyChangeVisitor {
        private OWLOntologyChange testChange;
        private boolean overlapping;
        
        public OverlapVisitor(OWLOntologyChange change) {
            testChange = change;
            overlapping = false;
        }
        
        public boolean isOverlapping() {
            return overlapping;
        }

        @Override
        public void visit(AddAxiom change) {
            overlapping = testChange instanceof OWLAxiomChange 
                                && ((OWLAxiomChange) testChange).getAxiom().equals(change.getAxiom());
        }

        @Override
        public void visit(RemoveAxiom change) {
            overlapping = testChange instanceof OWLAxiomChange 
                                && ((OWLAxiomChange) testChange).getAxiom().equals(change.getAxiom());
        }

        @Override
        public void visit(SetOntologyID change) {
            overlapping = testChange instanceof SetOntologyID;
        }

        @Override
        public void visit(AddImport change) {
            overlapping = testChange instanceof ImportChange 
                               && ((ImportChange) testChange).getImportDeclaration().equals(change.getImportDeclaration());
        }

        @Override
        public void visit(RemoveImport change) {
            overlapping = testChange instanceof ImportChange 
                              && ((ImportChange) testChange).getImportDeclaration().equals(change.getImportDeclaration());
        }

        @Override
        public void visit(AddOntologyAnnotation change) {
            if (testChange instanceof AddOntologyAnnotation) {
                overlapping = ((AddOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
            else if (testChange instanceof RemoveOntologyAnnotation) {
                overlapping = ((RemoveOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
        }

        @Override
        public void visit(RemoveOntologyAnnotation change) {
            if (testChange instanceof AddOntologyAnnotation) {
                overlapping = ((AddOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
            else if (testChange instanceof RemoveOntologyAnnotation) {
                overlapping = ((RemoveOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
        }
        
    }

}
