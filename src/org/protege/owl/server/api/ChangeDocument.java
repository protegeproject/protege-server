package org.protege.owl.server.api;

import java.io.Serializable;
import java.util.List;

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
	
	ServerRevision getStartRevision();
	
	ServerRevision getEndRevision();
	
	List<OntologyDocumentChange> getChanges();
}
