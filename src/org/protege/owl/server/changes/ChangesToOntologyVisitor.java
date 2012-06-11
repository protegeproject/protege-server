package org.protege.owl.server.changes;

import java.util.ArrayList;
import java.util.List;
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
	
	public static OWLOntology createChangesOntology(OntologyDocumentRevision startRevision, List<OWLOntologyChange> changes) {
		ChangesToOntologyVisitor visitor = new ChangesToOntologyVisitor(startRevision);
		for (OWLOntologyChange change : changes) {
			change.accept(visitor);
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
		annotations.add(factory.getOWLAnnotation(ChangeOntology.REVISION, factory.getOWLLiteral(end.getRevision())));
		annotations.add(factory.getOWLAnnotation(ChangeOntology.IS_AXIOM_ADDED, factory.getOWLLiteral(true)));
		OWLAxiom axiom = change.getAxiom().getAnnotatedAxiom(annotations);
		changes.add(new AddAxiom(ontology, axiom));
		incrementEndRevision();
	}

	@Override
	public void visit(RemoveAxiom change) {
		Set<OWLAnnotation> annotations = new TreeSet<OWLAnnotation>();
		annotations.add(factory.getOWLAnnotation(ChangeOntology.REVISION, factory.getOWLLiteral(end.getRevision())));
		annotations.add(factory.getOWLAnnotation(ChangeOntology.IS_AXIOM_ADDED, factory.getOWLLiteral(false)));
		OWLAxiom axiom = change.getAxiom().getAnnotatedAxiom(annotations);
		changes.add(new AddAxiom(ontology, axiom));
		incrementEndRevision();
	}

	@Override
	public void visit(SetOntologyID change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AddImport change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RemoveImport change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AddOntologyAnnotation change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RemoveOntologyAnnotation change) {
		// TODO Auto-generated method stub
		
	}

	private void incrementEndRevision() {
		end = new OntologyDocumentRevision(end.getRevision());
	}
	
}
