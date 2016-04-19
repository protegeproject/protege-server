package org.protege.owl.server.versioning;

import org.protege.owl.server.versioning.api.ChangeHistory;
import org.protege.owl.server.versioning.api.VersionedOWLOntology;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class VersionedOntologyDocumentImpl implements VersionedOWLOntology {

    public static final String BACKING_STORE_PROPERTY = "server.location";
    public static final String VERSION_PROPERTY = "version";
    public static final String VERSION_DOCUMENT_DIRECTORY = ".owlserver";
    public static final String VERSION_DOCUMENT_EXTENSION = ".vontology";

    public static File getMetaDataFile(File ontologyFile) {
        File versionInfoDir = getVersionInfoDirectory(ontologyFile);
        return new File(versionInfoDir, ontologyFile.getName() + VERSION_DOCUMENT_EXTENSION);
    }

    public static HistoryFile getHistoryFile(File ontologyFile) throws InvalidHistoryFileException {
        File versionInfoDir = getVersionInfoDirectory(ontologyFile);
        File historyFile = new File(versionInfoDir, ontologyFile.getName() + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
        return new HistoryFile(historyFile);
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

    private ServerDocument serverDocument;
    private OWLOntology ontology;
    private OntologyDocumentRevision revision;
    private ChangeHistory localHistory;
    private boolean isHistoryDirty = false;

    public VersionedOntologyDocumentImpl(ServerDocument serverDocument, OWLOntology ontology,
            OntologyDocumentRevision revision, ChangeHistory localHistory) {
        this.serverDocument = serverDocument;
        this.ontology = ontology;
        this.revision = revision;
        this.localHistory = localHistory;
    }

    public VersionedOntologyDocumentImpl(ServerDocument serverDocument, OWLOntology ontology) {
        this.serverDocument = serverDocument;
        this.ontology = ontology;
        this.revision = OntologyDocumentRevision.START_REVISION;
        this.localHistory = ChangeHistoryImpl.createEmptyChangeHistory();
    }

    @Override
    public ServerDocument getServerDocument() {
        return serverDocument;
    }

    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    @Override
    public String getDisplayName() {
        return ontology.getOntologyID().getOntologyIRI().get().toString();
    }

    @Override
    public ChangeHistory getLocalHistory() {
        return localHistory;
    }

    @Override
    public void appendChangeHistory(ChangeHistory changes) {
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
    public boolean saveMetaData() throws Exception {
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
    public boolean saveLocalHistory() throws IOException, InvalidHistoryFileException {
        File ontologyFile = getBackingStore(ontology);
        if (ontologyFile == null) {
            return false;
        }
        HistoryFile historyFile = getHistoryFile(ontologyFile);
        if (isHistoryDirty || !historyFile.exists()) {
            historyFile.getParentFile().mkdirs();
            ChangeHistoryUtilities.writeChanges(localHistory, historyFile);
            isHistoryDirty = false;
        }
        return true;
    }

    @Override
    public String toString() {
        String template = "%s at revision #%s [Remote HEAD %s]";
        return String.format(template, ontology.getOntologyID(), revision, serverDocument.getHost());
    }
}
