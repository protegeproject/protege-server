/**
 * 
 */
package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ClientOntologyInfo {
	private List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
	private String shortName;
	private int revision;

	
	public ClientOntologyInfo(String shortName, int revision) {
		super();
		this.shortName = shortName;
		this.revision = revision;
	}
	
	public String getShortName() {
        return shortName;
    }
	
	public int getRevision() {
		return revision;
	}
	public void setRevision(int revision) {
		this.revision = revision;
	}
	public List<OWLOntologyChange> getChanges() {
		return changes;
	}
	public void setChanges(List<OWLOntologyChange> changes) {
		this.changes = changes;
	}
	
	public void addChange(OWLOntologyChange change) {
		changes.add(change);
	}
	
	public void removeChange(OWLOntologyChange change) {
	    changes.remove(change);
	}
	
	public List<OWLOntologyChange> clearChanges() {
		List<OWLOntologyChange> oldChanges = changes;
		changes = new ArrayList<OWLOntologyChange>();
		return oldChanges;
	}
	
}