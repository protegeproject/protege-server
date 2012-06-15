package org.protege.owl.server.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
	/**
	 * Get the start revision of this collection of changes.
	 * 
	 * @return
	 */
	OntologyDocumentRevision getStartRevision();
	
	/**
	 * Get the end revision of this collection of changes.
	 * 
	 * @return
	 */
	OntologyDocumentRevision getEndRevision();
	
	/**
	 * For each revision from the start revision (inclusive) to the end revision (exclusive) there 
	 * will be a commit comment which represents what the user said about the ontology at that 
	 * stage of the development.
	 * 
	 * @param revision
	 * @return
	 */
	Map<OntologyDocumentRevision, String> getComments();
	
	/**
	 * This call will return the change document obtained by restricting the set of changes from the 
	 * start revision to the end revision.
	 * <p/>
	 * If the start or the end revision is out of the range of the ChangeDocument then this call can 
	 * fail with an exception.  A primary use of this call is to handle a collection of changes that have
	 * been deserialized off of the disk.  The routine that is deserializing the change document may 
	 * only want a subset of the changes from the ChangeDocument.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	ChangeDocument cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end);
	
	/**
	 * Gets the list of changes associated with this document and associates them with the ontology.  
	 * <p/>
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
