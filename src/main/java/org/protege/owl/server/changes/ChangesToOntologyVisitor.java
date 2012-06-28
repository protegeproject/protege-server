package org.protege.owl.server.changes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * 
 * @author tredmond
 * @deprecated replace with Matthew's binary serialization format.
 */
@Deprecated
public class ChangesToOntologyVisitor implements OWLOntologyChangeVisitor {
	
	public static OWLOntology createChangesOntology(OntologyDocumentRevision startRevision, List<OWLOntologyChange> changes, Map<OntologyDocumentRevision, String> commitComments) {
		ChangesToOntologyVisitor visitor = new ChangesToOntologyVisitor(startRevision);
		for (OWLOntologyChange change : changes) {
			change.accept(visitor);
		}
		for (Entry<OntologyDocumentRevision, String> commitComment : commitComments.entrySet()) {
			visitor.addCommitComment(commitComment.getKey(), commitComment.getValue());
		}
		return visitor.getUpdatedOntology();
	}
	
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
	
	private OntologyDocumentRevision start;
	private OntologyDocumentRevision end ;
	
	public ChangesToOntologyVisitor(OntologyDocumentRevision revision) {
		try {
			manager = OWLManager.createOWLOntologyManager();
			factory = manager.getOWLDataFactory();
			ontology = manager.createOntology();
			
			start = revision;
			end   = revision;
		}
		catch (OWLOntologyCreationException ooce) {
			throw new RuntimeException("Could not create new empty ontology", ooce);
		}
	}
	
	public OWLOntology getUpdatedOntology() {
		manager.applyChanges(changes);
		changes.clear();
		return ontology;
	}
	
	public OntologyDocumentRevision getStartRevision() {
		return start;
	}
	
	public OntologyDocumentRevision getEndRevision() {
		return end;
	}

	@Override
	public void visit(AddAxiom change) {
		Set<OWLAnnotation> annotations = new TreeSet<OWLAnnotation>();
		addRevisionAnnotation(annotations);
		addAddRemoveAnnotation(annotations, true);
		OWLAxiom axiom = change.getAxiom().getAnnotatedAxiom(annotations);
		changes.add(new AddAxiom(ontology, axiom));
	}

	@Override
	public void visit(RemoveAxiom change) {
		Set<OWLAnnotation> annotations = new TreeSet<OWLAnnotation>();
		addRevisionAnnotation(annotations);
		addAddRemoveAnnotation(annotations, false);
		OWLAxiom axiom = change.getAxiom().getAnnotatedAxiom(annotations);
		changes.add(new AddAxiom(ontology, axiom));
	}

	@Override
	public void visit(SetOntologyID change) {
		OWLOntologyID id = change.getNewOntologyID();

		Set<OWLAnnotation> metaAnnotations = new TreeSet<OWLAnnotation>();
		addRevisionAnnotation(metaAnnotations);
		addAddRemoveAnnotation(metaAnnotations, !id.isAnonymous());
		if (!id.isAnonymous()) {
			changes.add(new AddOntologyAnnotation(ontology, factory.getOWLAnnotation(ChangeOntology.SET_ONTOLOGY_ID, id.getOntologyIRI(), metaAnnotations)));
		}
	}

	@Override
	public void visit(AddImport change) {
		Set<OWLAnnotation> metaAnnotations = new TreeSet<OWLAnnotation>();
		addRevisionAnnotation(metaAnnotations);
		addAddRemoveAnnotation(metaAnnotations, true);
		changes.add(new AddOntologyAnnotation(ontology, factory.getOWLAnnotation(ChangeOntology.IMPORTS, change.getImportDeclaration().getIRI(), metaAnnotations)));
	}

	@Override
	public void visit(RemoveImport change) {
		Set<OWLAnnotation> metaAnnotations = new TreeSet<OWLAnnotation>();
		addRevisionAnnotation(metaAnnotations);
		addAddRemoveAnnotation(metaAnnotations, false);
		changes.add(new AddOntologyAnnotation(ontology, factory.getOWLAnnotation(ChangeOntology.IMPORTS, change.getImportDeclaration().getIRI(), metaAnnotations)));
	}

	@Override
	public void visit(AddOntologyAnnotation change) {
		Set<OWLAnnotation> annotationAnnotations = new TreeSet<OWLAnnotation>(change.getAnnotation().getAnnotations());
		addAddRemoveAnnotation(annotationAnnotations, true);
		addRevisionAnnotation(annotationAnnotations);
		changes.add(new AddOntologyAnnotation(ontology, change.getAnnotation().getAnnotatedAnnotation(annotationAnnotations)));
	}

	@Override
	public void visit(RemoveOntologyAnnotation change) {
		Set<OWLAnnotation> annotationAnnotations = new TreeSet<OWLAnnotation>(change.getAnnotation().getAnnotations());
		addAddRemoveAnnotation(annotationAnnotations, false);
		addRevisionAnnotation(annotationAnnotations);
		changes.add(new AddOntologyAnnotation(ontology, change.getAnnotation().getAnnotatedAnnotation(annotationAnnotations)));
	}
	
	public void addCommitComment(OntologyDocumentRevision revision, String comment) {
		Set<OWLAnnotation> revisionAnnotations = Collections.singleton(factory.getOWLAnnotation(ChangeOntology.REVISION, factory.getOWLLiteral(revision.getRevision())));
		OWLAnnotation commitCommentAnnotation = factory.getOWLAnnotation(ChangeOntology.COMMIT_COMMENT, factory.getOWLLiteral(comment), revisionAnnotations);
		changes.add(new AddOntologyAnnotation(ontology, commitCommentAnnotation));
	}
	
	private void addAddRemoveAnnotation(Set<OWLAnnotation> annotations, boolean added) {
		annotations.add(factory.getOWLAnnotation(ChangeOntology.IS_AXIOM_ADDED, factory.getOWLLiteral(added)));
	}
	
	private void addRevisionAnnotation(Set<OWLAnnotation> annotations) {
		annotations.add(factory.getOWLAnnotation(ChangeOntology.REVISION, factory.getOWLLiteral(end.getRevision())));
		end = new OntologyDocumentRevision(end.getRevision() + 1);
	}
	
}
