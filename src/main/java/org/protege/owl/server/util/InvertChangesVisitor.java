package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

@Deprecated
public class InvertChangesVisitor implements OWLOntologyChangeVisitor {
    List<OWLOntologyChange> invertedChanges = new ArrayList<OWLOntologyChange>();
    private CollectingChangeVisitor collectedFirstChanges;
    
    public InvertChangesVisitor(List<OWLOntologyChange> firstChanges) {
        collectedFirstChanges = CollectingChangeVisitor.collectChanges(firstChanges);
    }

    public List<OWLOntologyChange> getInvertedChanges() {
        return invertedChanges;
    }
    
    @Override
    public void visit(AddAxiom change) {
        OWLOntologyChange baselineChange = collectedFirstChanges.getLastAxiomChangeMap().get(change.getAxiom());
        if (baselineChange == null || !baselineChange.equals(change)) {
            invertedChanges.add(new RemoveAxiom(change.getOntology(), change.getAxiom()));
        }
    }

    @Override
    public void visit(RemoveAxiom change) {
        OWLOntologyChange baselineChange = collectedFirstChanges.getLastAxiomChangeMap().get(change.getAxiom());
        if (baselineChange != null && !baselineChange.equals(change)) {
            invertedChanges.add(new AddAxiom(change.getOntology(), change.getAxiom()));
        }
    }

    @Override
    public void visit(SetOntologyID change) {
        SetOntologyID baselineChange = collectedFirstChanges.getLastOntologyIDChange();
        if (baselineChange != null) {
            invertedChanges.add(baselineChange);
        }
    }

    @Override
    public void visit(AddImport change) {
        OWLOntologyChange baselineChange = collectedFirstChanges.getLastImportChangeMap().get(change.getImportDeclaration());
        if (baselineChange == null || !baselineChange.equals(change)) {
            invertedChanges.add(new RemoveImport(change.getOntology(), change.getImportDeclaration()));
        }
    }

    @Override
    public void visit(RemoveImport change) {
        OWLOntologyChange baselineChange = collectedFirstChanges.getLastImportChangeMap().get(change.getImportDeclaration());
        if (baselineChange != null && !baselineChange.equals(change)) {
            invertedChanges.add(new AddImport(change.getOntology(), change.getImportDeclaration()));
        }
    }

    @Override
    public void visit(AddOntologyAnnotation change) {
        OWLOntologyChange baselineChange = collectedFirstChanges.getLastOntologyAnnotationChangeMap().get(change.getAnnotation());
        if (baselineChange == null || !baselineChange.equals(change)) {
            invertedChanges.add(new RemoveOntologyAnnotation(change.getOntology(), change.getAnnotation()));
        }
    }

    @Override
    public void visit(RemoveOntologyAnnotation change) {
        OWLOntologyChange baselineChange = collectedFirstChanges.getLastOntologyAnnotationChangeMap().get(change.getAnnotation());
        if (baselineChange == null || !baselineChange.equals(change)) {
            invertedChanges.add(new AddOntologyAnnotation(change.getOntology(), change.getAnnotation()));
        }
    }
    
}