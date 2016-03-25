package org.protege.owl.server.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.SingletonChangeHistory;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface DocumentFactory {

    ChangeHistory createEmptyChangeDocument(OntologyDocumentRevision revision);

    SingletonChangeHistory createChangeDocument(List<OWLOntologyChange> changes, ChangeMetaData metadata, OntologyDocumentRevision start);

    ChangeHistory readChangeDocument(InputStream in, OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException;

    boolean hasServerMetadata(OWLOntology ontology);

    IRI getServerLocation(OWLOntology ontology) throws IOException;

    boolean hasServerMetadata(IRI ontologyDocumentLocation);

    IRI getServerLocation(IRI ontologyDocumentLocation) throws IOException;

    VersionedOntologyDocument getVersionedOntologyDocument(OWLOntology ontology) throws IOException;

    VersionedOntologyDocument createVersionedOntology(OWLOntology ontology, RemoteOntologyDocument remoteDocument, OntologyDocumentRevision revision);

    OWLObjectRenderer getOWLRenderer();
}
