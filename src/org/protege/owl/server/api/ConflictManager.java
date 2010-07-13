package org.protege.owl.server.api;

import java.util.List;
import java.util.Map;

import org.protege.owl.server.exception.OntologyConflictException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * This is an early interface to be implemented by the conflict manager  of a server.  The job 
 * of the conflict manager is to detect conflicts between updates made by different clients.  We 
 * hope to eventually include the following conflict managers:
 * <ul>
 * <li> a strict conflict manager that only allows a client to make a change if that client is holding the 
 *      latest  version of the ontology.  This is currently the only implemented conflict  manager and  it
 *      will probably be the least useful one in practice.
 * <li> a lenient conflict manager that allows any change even if the client is out of date and this change 
 *      has already been overridden.  Experience suggests that this conflict manager is more useful than one would
 *      expect.
 * <li> a entity based conflict manager that is based on the (not formally defined) notion that most changes are actually 
 *      about some entity in the ontology.  Thus removing an equivalence class assertion between a named class and an 
 *      anonymous class is about the named class.  If two changes involve the same entity then they are in conflict.
 * <li> a conflict manager based on Julian Seidenberg's work.
 * </ul>
 * @author tredmond
 *
 */
public interface ConflictManager {
    void initialise(Server server);
    
    void validateChanges(Map<IRI, Integer> versions, List<OWLOntologyChange> changes) throws OntologyConflictException;
}
