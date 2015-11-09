package org.protege.owl.server.render;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class RenderOntologyChangeVisitor implements OWLOntologyChangeVisitor {
    private StringBuffer sb = new StringBuffer();
    private OWLObjectRenderer renderer;
    
    
    
    public RenderOntologyChangeVisitor(OWLObjectRenderer renderer) {
        this.renderer = renderer;
    }

    public String getRendering() {
        return sb.toString();
    }
    
    @Override
    public void visit(AddAxiom change) {
        sb.append("Add Axiom: ");
        sb.append(renderer.render(change.getAxiom()));
    }
    @Override
    public void visit(RemoveAxiom change) {
        sb.append("Remove Axiom: ");
        sb.append(renderer.render(change.getAxiom()));
    }
    @Override
    public void visit(SetOntologyID change) {
        sb.append("Set Ontology Id: ");
        sb.append(renderer.render(change.getNewOntologyID().getDefaultDocumentIRI()));
    }
    @Override
    public void visit(AddImport change) {
        sb.append("Add Import: ");
        sb.append(renderer.render(change.getImportDeclaration().getIRI()));
    }
    @Override
    public void visit(RemoveImport change) {
        sb.append("Remove Import: ");
        sb.append(renderer.render(change.getImportDeclaration().getIRI()));
    }
    @Override
    public void visit(AddOntologyAnnotation change) {
        sb.append("Add Ontology Annotation: ");
        sb.append(renderer.render(change.getAnnotation()));
    }
    @Override
    public void visit(RemoveOntologyAnnotation change) {
        sb.append("Remove Ontology Annotation: ");
        sb.append(renderer.render(change.getAnnotation()));
    }

}
