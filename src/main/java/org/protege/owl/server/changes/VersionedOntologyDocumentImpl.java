package org.protege.owl.server.changes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import edu.stanford.protege.metaproject.api.Address;
import edu.stanford.protege.metaproject.api.Host;

import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.api.RemoteOntologyDocument;
import org.protege.owl.server.changes.api.VersionedOntologyDocument;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class VersionedOntologyDocumentImpl implements VersionedOntologyDocument {

    public static final String BACKING_STORE_PROPERTY = "server.location";
    public static final String VERSION_PROPERTY = "version";
    public static final String VERSION_DOCUMENT_DIRECTORY = ".owlserver";
    public static final String VERSION_DOCUMENT_EXTENSION = ".vontology";

    public static File getMetaDataFile(File ontologyFile) {
        File versionInfoDir = getVersionInfoDirectory(ontologyFile);
        return new File(versionInfoDir, ontologyFile.getName() + VERSION_DOCUMENT_EXTENSION);
    }

    public static File getHistoryFile(File ontologyFile) {
        File versionInfoDir = getVersionInfoDirectory(ontologyFile);
        return new File(versionInfoDir, ontologyFile.getName() + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
    }

    public static File getVersionInfoDirectory(File ontologyFile) {
        File dir = ontologyFile.getParentFile();
        return new File(dir, VERSION_DOCUMENT_DIRECTORY);
    }

    public static File getBackingStore(OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        IRI documentLocation = manager.getOntologyDocumentIRI(ontology);
        if (!documentLocation.getScheme().equals("file")) {
            return null;
        }
        return new File(documentLocation.toURI());
    }

    private Host remoteHost;
    private Address remoteAddress;

    private OWLOntology ontology;
    private RemoteOntologyDocument serverDocument;
    private OntologyDocumentRevision revision;
    private ChangeHistory localHistory;
    private boolean isHistoryDirty = false;

    @Deprecated
    public VersionedOntologyDocumentImpl(OWLOntology ontology, RemoteOntologyDocument serverDocument,
            OntologyDocumentRevision revision, ChangeHistory localHistory) {
        this.ontology = ontology;
        this.serverDocument = serverDocument;
        this.revision = revision;
        this.localHistory = localHistory;
    }

    public VersionedOntologyDocumentImpl(Host remoteHost, Address remoteAddress, OWLOntology ontology, 
            OntologyDocumentRevision revision, ChangeHistory localHistory) {
        this.remoteHost = remoteHost;
        this.remoteAddress = remoteAddress;
        this.ontology = ontology;
        this.revision = revision;
        this.localHistory = localHistory;
    }

    @Override
    public Host getRemoteHost() {
        return remoteHost;
    }

    @Override
    public Address getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    @Override
    public RemoteOntologyDocument getServerDocument() {
        return serverDocument;
    }

    @Override
    public ChangeHistory getLocalHistory() {
        return localHistory;
    }

    @Override
    public void appendLocalHistory(ChangeHistory changes) {
        localHistory = localHistory.appendChanges(changes);
        isHistoryDirty = true;
    }

    @Override
    public OntologyDocumentRevision getRevision() {
        return revision;
    }

    @Override
    public void setRevision(OntologyDocumentRevision revision) {
        this.revision = revision;
    }

    @Override
    public boolean saveMetaData() throws IOException {
        File ontologyFile = getBackingStore(ontology);
        if (ontologyFile == null) {
            return false;
        }
        File metadataFile = getMetaDataFile(ontologyFile);
        metadataFile.getParentFile().mkdirs();
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(metadataFile)));
        try {
            oos.writeObject(serverDocument);
            oos.writeObject(revision);
        }
        finally {
            oos.flush();
            oos.close();
        }
        saveLocalHistory();
        return true;
    }

    @Override
    public boolean saveLocalHistory() throws IOException {
        File ontologyFile = getBackingStore(ontology);
        if (ontologyFile == null) {
            return false;
        }
        File historyFile = getHistoryFile(ontologyFile);
        if (isHistoryDirty || !historyFile.exists()) {
            historyFile.getParentFile().mkdirs();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(historyFile));
            try {
                localHistory.writeChangeDocument(os);
            }
            finally {
                os.flush();
                os.close();
            }
            isHistoryDirty = false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[Document " + ontology.getOntologyID() + " from server " + serverDocument.getServerLocation()
                + " revision " + revision + "]";
    }
}
