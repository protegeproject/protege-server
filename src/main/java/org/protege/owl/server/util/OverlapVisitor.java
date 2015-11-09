package org.protege.owl.server.util;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class OverlapVisitor implements OWLOntologyChangeVisitor {
    private OWLOntologyChange testChange;
    private boolean overlapping;
    
    public OverlapVisitor(OWLOntologyChange change) {
        testChange = change;
        overlapping = false;
    }
    
    public boolean isOverlapping() {
        return overlapping;
    }

    
    public void visit(AddAxiom change) {
        overlapping = testChange instanceof OWLAxiomChange 
                            && ((OWLAxiomChange) testChange).getAxiom().equals(change.getAxiom());
    }

    
    public void visit(RemoveAxiom change) {
        overlapping = testChange instanceof OWLAxiomChange 
                            && ((OWLAxiomChange) testChange).getAxiom().equals(change.getAxiom());
    }

    
    public void visit(SetOntologyID change) {
        overlapping = testChange instanceof SetOntologyID;
    }

    
    public void visit(AddImport change) {
        overlapping = testChange instanceof ImportChange 
                           && ((ImportChange) testChange).getImportDeclaration().equals(change.getImportDeclaration());
    }

    
    public void visit(RemoveImport change) {
        overlapping = testChange instanceof ImportChange 
                          && ((ImportChange) testChange).getImportDeclaration().equals(change.getImportDeclaration());
    }

    
    public void visit(AddOntologyAnnotation change) {
        if (testChange instanceof AddOntologyAnnotation) {
            overlapping = ((AddOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
        }
        else if (testChange instanceof RemoveOntologyAnnotation) {
            overlapping = ((RemoveOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
        }
    }

    
    public void visit(RemoveOntologyAnnotation change) {
        if (testChange instanceof AddOntologyAnnotation) {
            overlapping = ((AddOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
        }
        else if (testChange instanceof RemoveOntologyAnnotation) {
            overlapping = ((RemoveOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
        }
    }
    
}