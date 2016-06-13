package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.RevisionMetadata;

import org.semanticweb.binaryowl.BinaryOWLMetadata;
import org.semanticweb.binaryowl.BinaryOWLOntologyChangeLog;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit implements Serializable {

    private static final long serialVersionUID = -5600758982758166504L;

    private RevisionMetadata metadata;
    private List<OWLOntologyChange> changes = new ArrayList<>();
    
    public Commit(RevisionMetadata metadata, List<OWLOntologyChange> changes) {
        this.metadata = metadata;
        this.changes = changes;
    }

    public RevisionMetadata getMetadata() {
        return metadata;
    }

    public List<OWLOntologyChange> getChanges() {
        return changes;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            BinaryOWLMetadata metadataRecord = new BinaryOWLMetadata();
            metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_USERNAME, metadata.getAuthorId());
            metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_NAME, metadata.getAuthorName());
            metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_EMAIL, metadata.getAuthorEmail());
            metadataRecord.setLongAttribute(RevisionMetadata.CHANGE_DATE, metadata.getDate().getTime());
            metadataRecord.setStringAttribute(RevisionMetadata.CHANGE_COMMENT, metadata.getComment());
            
            /*
             * This commit object doesn't have a revision number assigned yet thus
             * -1 is used. In this method call we use the parameter (timestamp:long)
             * as the revision number. TODO: Fix this
             */
            log.appendChanges(changes, -1, metadataRecord, out);
        }
        finally {
            out.flush();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
            OWLOntology placeholder = owlManager.createOntology();
            final List<OWLOntologyChange> changeRead = new ArrayList<>();
            log.readChanges(in, owlManager.getOWLDataFactory(), (list, skipSetting, filePosition) -> {
                // Get the metadata
                BinaryOWLMetadata metadataRecord = list.getMetadata();
                String authorId = metadataRecord.getStringAttribute(RevisionMetadata.AUTHOR_USERNAME, "");
                String authorName = metadataRecord.getStringAttribute(RevisionMetadata.AUTHOR_NAME, "");
                String authorEmail = metadataRecord.getStringAttribute(RevisionMetadata.AUTHOR_EMAIL, "");
                Date changeDate = new Date(metadataRecord.getLongAttribute(RevisionMetadata.CHANGE_DATE, 0L));
                String comment = metadataRecord.getStringAttribute(RevisionMetadata.CHANGE_COMMENT, "");
                metadata = new RevisionMetadata(authorId, authorName, authorEmail, changeDate, comment);
                
                // Get the changes
                List<OWLOntologyChangeRecord> changeRecords = list.getChangeRecords();
                for (OWLOntologyChangeRecord cr : changeRecords) {
                    OWLOntologyChangeData changeData = cr.getData();
                    OWLOntologyChange change = changeData.createOntologyChange(placeholder);
                    changeRead.add(change);
                }
            });
            changes = changeRead;
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Internal error while reading commit object", e);
        }
    }
}
