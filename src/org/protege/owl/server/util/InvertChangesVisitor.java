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

public class InvertChangesVisitor implements OWLOntologyChangeVisitor {
	private List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
	
	public void clear() {
		changes.clear();
	}
	
	public List<OWLOntologyChange> getChanges() {
		return changes;
	}
	
	public static List<OWLOntologyChange> invertChanges(List<OWLOntologyChange> changes) {
		InvertChangesVisitor visitor = new InvertChangesVisitor();
		for (OWLOntologyChange change : changes) {
			change.accept(visitor);
		}
		return visitor.getChanges();
	}

	
	public void visit(AddAxiom change) {
		changes.add(new RemoveAxiom(change.getOntology(), change.getAxiom()));
	}

	
	public void visit(RemoveAxiom change) {
		changes.add(new AddAxiom(change.getOntology(), change.getAxiom()));
	}

	
	public void visit(SetOntologyID change) {
		;
	}

	
	public void visit(AddImport change) {
		changes.add(new RemoveImport(change.getOntology(), change.getImportDeclaration()));
	}

	
	public void visit(RemoveImport change) {
		changes.add(new AddImport(change.getOntology(), change.getImportDeclaration()));
	}

	
	public void visit(AddOntologyAnnotation change) {
		changes.add(new RemoveOntologyAnnotation(change.getOntology(), change.getAnnotation()));
	}

	
	public void visit(RemoveOntologyAnnotation change) {
		changes.add(new AddOntologyAnnotation(change.getOntology(), change.getAnnotation()));
	}

}
