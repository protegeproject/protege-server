package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class Utilities {
    
    public static RemoteOntologyRevisions getRevisionsFromShortName(Set<RemoteOntologyRevisions> revisionSet,
                                                                    String shortName) {
        for (RemoteOntologyRevisions revisions : revisionSet) {
            if (revisions.getShortName().equals(shortName)) {
                return revisions;
            }
        }
        return null;
    }
    
    
    public static RemoteOntologyRevisions getRevisionsFromOntologyName(Set<RemoteOntologyRevisions> revisionSet,
                                                                       IRI ontologyName) {
        for (RemoteOntologyRevisions revisions : revisionSet) {
            if (revisions.getOntologyName().equals(ontologyName)) {
                return revisions;
            }
        }
        return null;
    }

    /**
     * This routine removes all set ontology id changes.  Other than this, the result of this call is to 
     * return a minimal list of changes in the sense that 
     * <ol> 
     * <li> the returned list of changes has the same net effect as the original changes and
     * <li> the list of changes is minimal.
     * </ol>
     * That is to say that changes that will have no effect on the net result are removed.
     */
    public static List<OWLOntologyChange> removeRedundantChanges(List<OWLOntologyChange> changes) {
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

    protected static class OverlapVisitor implements OWLOntologyChangeVisitor {
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
