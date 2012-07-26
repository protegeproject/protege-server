package org.protege.owl.server.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * When Matthew's change serialization code comes, this can be optimized.  It will then be possible to 
 * crop the changes of a large history file without reading all of the changes from the history file in.
 * This will spare the client the effort of loading an entire history document when he only wants a small section.
 * At that time this class will probably need to move from the util package to the changes or impl package.
 * 
 * @author redmond
 * @deprecated
 */
@Deprecated
public class LazyCroppedChangeDocument implements ChangeDocument {
	private static final long serialVersionUID = -7116780557239070763L;
	private ChangeDocument allChanges;
	private OntologyDocumentRevision startRevision;
	private OntologyDocumentRevision endRevision;
	private ChangeDocument delegate;
	
	public LazyCroppedChangeDocument(File historyFile, OntologyDocumentRevision startRevision, OntologyDocumentRevision endRevision) {
		allChanges = new LazyChangeDocument(historyFile);
		this.startRevision = startRevision;
		this.endRevision = endRevision;
	}
	
	private void ensureDelegateExists() {
		if (delegate == null) {
			if (endRevision == null) {
				endRevision = allChanges.getEndRevision();
			}
			delegate = allChanges.cropChanges(startRevision, endRevision);
		}
	}
	
	public OntologyDocumentRevision getStartRevision() {
		ensureDelegateExists();
		return delegate.getStartRevision();
	}
	public OntologyDocumentRevision getEndRevision() {
		ensureDelegateExists();
		return delegate.getEndRevision();
	}
	public Map<OntologyDocumentRevision, String> getComments() {
		ensureDelegateExists();
		return delegate.getComments();
	}
	public ChangeDocument cropChanges(OntologyDocumentRevision start,
			OntologyDocumentRevision end) {
		ensureDelegateExists();
		return delegate.cropChanges(start, end);
	}
	public ChangeDocument appendChanges(ChangeDocument additionalChanges) {
		ensureDelegateExists();
		return delegate.appendChanges(additionalChanges);
	}

	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		ensureDelegateExists();
		return delegate.getChanges(ontology);
	}	
}
