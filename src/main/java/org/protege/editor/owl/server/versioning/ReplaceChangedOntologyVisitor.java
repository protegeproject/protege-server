package org.protege.editor.owl.server.versioning;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

import java.util.ArrayList;
import java.util.List;

public class ReplaceChangedOntologyVisitor implements OWLOntologyChangeVisitorEx<OWLOntologyChange> {

    public static List<OWLOntologyChange> mutate(OWLOntology placeholder, List<OWLOntologyChange> changes) {
        List<OWLOntologyChange> newChanges = new ArrayList<OWLOntologyChange>();
        ReplaceChangedOntologyVisitor mutator = new ReplaceChangedOntologyVisitor(placeholder);
        for (OWLOntologyChange change : changes) {
            newChanges.add(change.accept(mutator));
        }
        return newChanges;
    }

    private OWLOntology ontology;

    public ReplaceChangedOntologyVisitor(OWLOntology ontology) {
        this.ontology = ontology;
    }

    @Override
    public OWLOntologyChange visit(AddAxiom change) {
        return new AddAxiom(ontology, change.getAxiom());
    }

    @Override
    public OWLOntologyChange visit(RemoveAxiom change) {
        return new RemoveAxiom(ontology, change.getAxiom());
    }

    @Override
    public OWLOntologyChange visit(SetOntologyID change) {
        return new SetOntologyID(ontology, change.getNewOntologyID());
    }

    @Override
    public OWLOntologyChange visit(AddImport change) {
        return new AddImport(ontology, change.getImportDeclaration());
    }

    @Override
    public OWLOntologyChange visit(RemoveImport change) {
        return new RemoveImport(ontology, change.getImportDeclaration());
    }

    @Override
    public OWLOntologyChange visit(AddOntologyAnnotation change) {
        return new AddOntologyAnnotation(ontology, change.getAnnotation());
    }

    @Override
    public OWLOntologyChange visit(RemoveOntologyAnnotation change) {
        return new RemoveOntologyAnnotation(ontology, change.getAnnotation());
    }
}
