package org.protege.owl.server.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.RemoteOntology;
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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
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
    
    protected abstract boolean contains(RemoteOntology ontology, OWLAxiom axiom);
    protected abstract boolean contains(RemoteOntology ontology, OWLImportsDeclaration owlImport);
    protected abstract boolean contains(RemoteOntology ontology, OWLAnnotation annotation);
    
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
    public void applyChanges(Set<RemoteOntology> versions, List<OWLOntologyChange> changes) throws RemoteOntologyChangeException {
        changes = reduceChangeList(versions, changes);
        if (conflictManager != null) {
            conflictManager.validateChanges(versions, changes);
        }
        ontologyManager.applyChanges(changes);
    }
    
    @Override
    public List<OWLOntologyChange> reduceChangeList(Set<RemoteOntology> versions, List<OWLOntologyChange> changes) {
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
    
    @Override
    public void save(RemoteOntology remoteOntology, IRI versionId, File location) throws IOException, OWLOntologyStorageException {
        OWLOntologyID id = new OWLOntologyID(remoteOntology.getOntologyName());
        OWLOntologyID savedId = new OWLOntologyID(remoteOntology.getOntologyName(), versionId);
        OWLOntology ontology = ontologyManager.getOntology(id);
        ontologyManager.applyChange(new SetOntologyID(ontology, savedId));
        ontologyManager.saveOntology(ontology, IRI.create(location));
        ontologyManager.applyChange(new SetOntologyID(ontology, id));
    }
    
    private class AcceptRejectChangeVisitor implements OWLOntologyChangeVisitor {
        private boolean accepted;
        private Set<RemoteOntology> versions;
        
        public AcceptRejectChangeVisitor(Set<RemoteOntology> versions) {
            this.versions = versions;
        }
        
        public boolean isAccepted() {
            return accepted;
        }

        @Override
        public void visit(AddAxiom change) {
            RemoteOntology version = lookup(versions, change.getOntology().getOntologyID().getOntologyIRI());
            accepted = !contains(version, change.getAxiom());
        }

        @Override
        public void visit(RemoveAxiom change) {
            RemoteOntology version = lookup(versions, change.getOntology().getOntologyID().getOntologyIRI());
            accepted = contains(version, change.getAxiom());
        }

        @Override
        public void visit(SetOntologyID change) {
            accepted = false;
        }

        @Override
        public void visit(AddImport change) {
            RemoteOntology version = lookup(versions, change.getOntology().getOntologyID().getOntologyIRI());
            accepted = !contains(version, change.getImportDeclaration());
        }

        @Override
        public void visit(RemoveImport change) {
            RemoteOntology version = lookup(versions, change.getOntology().getOntologyID().getOntologyIRI());
            accepted = contains(version, change.getImportDeclaration());    
        }

        @Override
        public void visit(AddOntologyAnnotation change) {
            RemoteOntology version = lookup(versions, change.getOntology().getOntologyID().getOntologyIRI());
            accepted = !contains(version, change.getAnnotation());
        }

        @Override
        public void visit(RemoveOntologyAnnotation change) {
            RemoteOntology version = lookup(versions, change.getOntology().getOntologyID().getOntologyIRI());
            accepted = contains(version, change.getAnnotation());
        }
        
    }
    
    protected RemoteOntology lookup(Set<RemoteOntology> versions, IRI ontologyIRI) {
        for (RemoteOntology version : versions) {
            if (version.getOntologyName().equals(ontologyIRI)) {
                return version;
            }
        }
        return null;
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
