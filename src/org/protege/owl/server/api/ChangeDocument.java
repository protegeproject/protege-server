package org.protege.owl.server.api;

import java.io.Serializable;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * This is a lightweight class that captures the collection of changes to a collection of ontology documents 
 * between two revisions.  So for example, using Matthew's binary data format, the data in this class will consist
 * of a start revision, an end revision and the file containing the change set.  When the data is serialized
 * essentially a selection of the change file is sent and it is reconstituted on the remote side.
 * 
 * @author tredmond
 *
 */

public interface ChangeDocument extends Serializable {
	
	OntologyDocument getOntologyDocument();
	
	ServerRevision getStartRevision();
	
	ServerRevision getEndRevision();
	
	ChangeDocument cropChanges(ServerRevision start, ServerRevision end);
	
	/**
	 * Gets the list of changes associated with this document and associates them with the ontology.  
	 * 
	 * The changes 
	 * returned are guaranteed to be minimal.  That is if an axiom is added it will not be added again or removed.
	 * Similarly for annotations and imports.  The SetOntologyID change can only occur once.
	 * 
	 * The minimality simplifies processing of the returned changes.  For example the order of the changes
	 * is no longer important.
	 * 
	 * @param ontology
	 * @return
	 */
	List<OWLOntologyChange> getChanges(OWLOntology ontology);
}
