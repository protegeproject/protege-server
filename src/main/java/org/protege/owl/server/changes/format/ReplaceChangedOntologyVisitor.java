package org.protege.owl.server.changes.format;

import java.util.ArrayList;
import java.util.List;

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

public class ReplaceChangedOntologyVisitor implements OWLOntologyChangeVisitorEx<OWLOntologyChange> {
	
	public static List<OWLOntologyChange> mutate(OWLOntology replacementOntology, List<OWLOntologyChange> changes) {
		List<OWLOntologyChange> newChanges = new ArrayList<OWLOntologyChange>();
		ReplaceChangedOntologyVisitor mutator = new ReplaceChangedOntologyVisitor(replacementOntology);
		for (OWLOntologyChange change : changes) {
			newChanges.add(change.accept(mutator));
		}
		return newChanges;
	}
	
	
	private OWLOntology replacementOntology;
	
	
	public ReplaceChangedOntologyVisitor(OWLOntology replacementOntology) {
		this.replacementOntology = replacementOntology;
	}

	@Override
	public OWLOntologyChange visit(AddAxiom change) {
		return new AddAxiom(replacementOntology, change.getAxiom());
	}

	@Override
	public OWLOntologyChange visit(RemoveAxiom change) {
		return new RemoveAxiom(replacementOntology, change.getAxiom());
	}

	@Override
	public OWLOntologyChange visit(SetOntologyID change) {
		return new SetOntologyID(replacementOntology, change.getNewOntologyID());
	}

	@Override
	public OWLOntologyChange visit(AddImport change) {
		return new AddImport(replacementOntology, change.getImportDeclaration());
	}

	@Override
	public OWLOntologyChange visit(RemoveImport change) {
		return new RemoveImport(replacementOntology, change.getImportDeclaration());
	}

	@Override
	public OWLOntologyChange visit(AddOntologyAnnotation change) {
		return new AddOntologyAnnotation(replacementOntology, change.getAnnotation());
	}

	@Override
	public OWLOntologyChange visit(RemoveOntologyAnnotation change) {
		return new RemoveOntologyAnnotation(replacementOntology, change.getAnnotation());
	}



}
