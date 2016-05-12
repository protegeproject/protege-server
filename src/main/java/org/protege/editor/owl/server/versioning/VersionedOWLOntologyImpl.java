package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.VersionedOWLOntology;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class VersionedOWLOntologyImpl implements VersionedOWLOntology {

    /*
     * Hidden directory name to store history file and other versioning resources
     */
    public static final String VERSION_DOCUMENT_DIRECTORY = ".owlserver";

    /*
     * Extension for the metadata file
     */
    public static final String VERSION_DOCUMENT_EXTENSION = ".vontology";

    private ServerDocument serverDocument;
    private OWLOntology ontology;
    private DocumentRevision revision;
    private ChangeHistory localHistory;
    private boolean isHistoryDirty = false;

    public VersionedOWLOntologyImpl(ServerDocument serverDocument, OWLOntology ontology,
            DocumentRevision revision, ChangeHistory localHistory) {
        this.serverDocument = serverDocument;
        this.ontology = ontology;
        this.revision = revision;
        this.localHistory = localHistory;
    }

    public VersionedOWLOntologyImpl(ServerDocument serverDocument, OWLOntology ontology) {
        this.serverDocument = serverDocument;
        this.ontology = ontology;
        this.revision = DocumentRevision.START_REVISION;
        this.localHistory = ChangeHistoryImpl.createEmptyChangeHistory();
    }

    public static File getMetadataFile(File ontologyFile) {
        File parentDir = getVersioningDirectory(ontologyFile);
        return new File(parentDir, ontologyFile.getName() + VERSION_DOCUMENT_EXTENSION);
    }

    public static HistoryFile getHistoryFile(File ontologyFile) throws InvalidHistoryFileException {
        File parentDir = getVersioningDirectory(ontologyFile);
        File historyFile = new File(parentDir, ontologyFile.getName() + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
        return HistoryFile.openExisting(historyFile.getAbsolutePath());
    }

    public static File getVersioningDirectory(File ontologyFile) {
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
    public DocumentRevision getRevision() { // TODO: Rename to getHeadRevision()?
        return revision;
    }

    @Override
    public void setRevision(DocumentRevision revision) {
        this.revision = revision;
    }

    @Override
    public boolean saveMetadata() throws Exception {
        File ontologyFile = getBackingStore(ontology);
        if (ontologyFile == null) {
            return false;
        }
        File metadataFile = getMetadataFile(ontologyFile);
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
            ChangeHistoryUtils.writeChanges(localHistory, historyFile);
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
