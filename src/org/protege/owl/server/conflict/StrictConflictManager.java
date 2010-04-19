package org.protege.owl.server.conflict;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.util.Utilities;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class StrictConflictManager implements ConflictManager {
    private Server server;
    
    @Override
    public void initialise(Server server) {
        this.server = server;
    }

    @Override
    public void validateChanges(Map<IRI, Integer> versions, List<OWLOntologyChange> changes) throws OntologyConflictException {
        Map<IRI, ServerOntologyInfo> ontologyInfoMap = Utilities.getOntologyInfoByOntologyName(server.getOntologyList());
        Set<IRI> rejectedIris = new HashSet<IRI>();
        List<OWLOntologyChange> rejectedChanges = new ArrayList<OWLOntologyChange>();
        for (Entry<IRI, Integer> entry : versions.entrySet()) {
            IRI iri = entry.getKey();
            int version = entry.getValue();
            ServerOntologyInfo info = ontologyInfoMap.get(iri);
            if (info.getMaxRevision() > version) {
                rejectedIris.add(iri);
            }
        }
        for (OWLOntologyChange change : changes) {
            if (rejectedIris.contains(change.getOntology().getOntologyID().getOntologyIRI())) {
                rejectedChanges.add(change);
            }
        }
        if (!rejectedChanges.isEmpty()) {
            throw new OntologyConflictException("Client is out of date", rejectedChanges);
        }
    }

}
