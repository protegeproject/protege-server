package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeHistoryImpl implements ChangeHistory {

    private static final long serialVersionUID = -8436246195642942303L;

    public static Logger logger = LoggerFactory.getLogger(ChangeHistoryImpl.class);

    private DocumentRevision baseRevision;
    private DocumentRevision headRevision;
    private SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = new TreeMap<>();
    private SortedMap<DocumentRevision, RevisionMetadata> logs = new TreeMap<>();

    public ChangeHistoryImpl() {
        this.baseRevision = DocumentRevision.START_REVISION;
        this.headRevision = DocumentRevision.START_REVISION;
    }

    public ChangeHistoryImpl(@Nonnull DocumentRevision baseRevision) {
        this.baseRevision = baseRevision;
        this.headRevision = baseRevision;
    }

    private ChangeHistoryImpl(@Nonnull DocumentRevision startRevision,
            @Nonnull SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions,
            @Nonnull SortedMap<DocumentRevision, RevisionMetadata> logs) {
        this.baseRevision = startRevision;
        this.revisions = revisions;
        this.logs = logs;
        headRevision = startRevision.next(revisions.size());
    }

    public static ChangeHistoryImpl createEmptyChangeHistory() {
        return new ChangeHistoryImpl();
    }

    public static ChangeHistoryImpl createEmptyChangeHistory(DocumentRevision baseRevision) {
        return new ChangeHistoryImpl(baseRevision);
    }

    public static ChangeHistoryImpl createEmptyChangeHistory(int baseRevisionNumber) {
        return new ChangeHistoryImpl(DocumentRevision.create(baseRevisionNumber));
    }

    public static ChangeHistoryImpl recreate(@Nonnull DocumentRevision startRevision,
            @Nonnull SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions,
            @Nonnull SortedMap<DocumentRevision, RevisionMetadata> logs) {
        return new ChangeHistoryImpl(startRevision, revisions, logs);
    }

    @Override
    public void addRevision(RevisionMetadata metadata, List<OWLOntologyChange> changes) {
        DocumentRevision nextRevision = headRevision.next();
        logs.put(nextRevision, metadata);
        revisions.put(nextRevision, changes);
        headRevision = nextRevision;
    }

    @Override
    public DocumentRevision getBaseRevision() {
        return baseRevision;
    }

    @Override
    public DocumentRevision getHeadRevision() {
        return headRevision;
    }

    @Override
    public RevisionMetadata getMetadataForRevision(DocumentRevision revision) {
        return logs.get(revision);
    }

    @Override
    public List<OWLOntologyChange> getChangesForRevision(DocumentRevision revision) {
        return revisions.get(revision);
    }

    @Override
    public SortedMap<DocumentRevision, List<OWLOntologyChange>> getRevisions() {
        return revisions;
    }

    @Override
    public SortedMap<DocumentRevision, RevisionMetadata> getMetadata() {
        return logs;
    }

    @Override
    public boolean isEmpty() {
        return revisions.isEmpty();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            DocumentRevision base = getBaseRevision();
            DocumentRevision head = getHeadRevision();
            for (DocumentRevision current = base.next(); current.behindOrSameAs(head); current = current.next()) {
                List<OWLOntologyChange> changeSet = getChangesForRevision(current);
                RevisionMetadata metadata = getMetadataForRevision(current);
                BinaryOWLMetadata changeMetadata = getBinaryOWLMetadata(metadata);
                log.appendChanges(changeSet, current.getRevisionNumber(), changeMetadata, out);
            }
        }
        finally {
            out.flush();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        final SortedMap<DocumentRevision, RevisionMetadata> logs = new TreeMap<>();
        final SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = new TreeMap<>();
        readBinaryOWLChangeLog(in, logs, revisions);
        if (logs.isEmpty() && revisions.isEmpty()) {
            this.baseRevision = DocumentRevision.START_REVISION;
            this.headRevision = DocumentRevision.START_REVISION;
            this.logs = logs;
            this.revisions = revisions;
        }
        else {
            this.baseRevision = revisions.firstKey().previous();
            this.headRevision = baseRevision.next(revisions.size());
            this.logs = logs;
            this.revisions = revisions;
        }
    }

    private static void readBinaryOWLChangeLog(ObjectInputStream inputStream,
            SortedMap<DocumentRevision, RevisionMetadata> resultMetadata,
            SortedMap<DocumentRevision, List<OWLOntologyChange>> resultRevisions) throws IOException {
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
            OWLOntology placeholder = owlManager.createOntology();
            log.readChanges(inputStream, owlManager.getOWLDataFactory(), (list, skipSetting, filePosition) -> {
                // Get the revision number
                int revision = (int) list.getTimestamp(); // TODO Report API misuse, timestamp == revision number
                
                // Get the metadata
                BinaryOWLMetadata metadataRecord = list.getMetadata();
                RevisionMetadata metadata = getRevisionMetadata(metadataRecord);
                resultMetadata.put(DocumentRevision.create(revision), metadata);
    
                // Get the changes 
                List<OWLOntologyChangeRecord> changeRecords = list.getChangeRecords();
                List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
                for (OWLOntologyChangeRecord cr : changeRecords) {
                    OWLOntologyChangeData changeData = cr.getData();
                    OWLOntologyChange change = changeData.createOntologyChange(placeholder);
                    changes.add(change);
                }
                resultRevisions.put(DocumentRevision.create(revision), changes);
            });
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Internal error while computing changes", e);
        }
    }

    private static BinaryOWLMetadata getBinaryOWLMetadata(RevisionMetadata metadata) {
        BinaryOWLMetadata metadataRecord = new BinaryOWLMetadata();
        metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_USERNAME, metadata.getAuthorId());
        metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_NAME, metadata.getAuthorName());
        metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_EMAIL, metadata.getAuthorEmail());
        metadataRecord.setLongAttribute(RevisionMetadata.CHANGE_DATE, metadata.getDate().getTime());
        metadataRecord.setStringAttribute(RevisionMetadata.CHANGE_COMMENT, metadata.getComment());
        return metadataRecord;
    }

    private static RevisionMetadata getRevisionMetadata(BinaryOWLMetadata metadata) {
        String authorId = metadata.getStringAttribute(RevisionMetadata.AUTHOR_USERNAME, "");
        String authorName = metadata.getStringAttribute(RevisionMetadata.AUTHOR_NAME, "");
        String authorEmail = metadata.getStringAttribute(RevisionMetadata.AUTHOR_EMAIL, "");
        Date changeDate = new Date(metadata.getLongAttribute(RevisionMetadata.CHANGE_DATE, 0L));
        String comment = metadata.getStringAttribute(RevisionMetadata.CHANGE_COMMENT, "");
        return new RevisionMetadata(authorId, authorName, authorEmail, changeDate, comment);
    }

    @Override
    public int hashCode() {
        return getBaseRevision().hashCode() + getHeadRevision().hashCode() + 42 * getRevisions().hashCode()
                + getMetadata().hashCode() / 42;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChangeHistoryImpl)) {
            return false;
        }
        ChangeHistoryImpl other = (ChangeHistoryImpl) obj;
        return other.getBaseRevision().equals(getBaseRevision()) && other.getHeadRevision().equals(getHeadRevision())
                && other.getRevisions().equals(getRevisions())
                && other.getMetadata().equals(getMetadata());
    }

    @Override
    public String toString() {
        final int SHOW_LIMIT = 8;
        StringBuffer sb = new StringBuffer();
        if (isEmpty()) {
            sb.append("(empty)");
        }
        else {
            boolean needNewline = false;
            for (DocumentRevision revision : revisions.keySet()) {
                if (needNewline) {
                    sb.append("\n");
                }
                sb.append("Revision ");
                sb.append(revision);
                sb.append(": ");
                
                List<OWLOntologyChange> changes = revisions.get(revision);
                boolean needInnerSerparator = false;
                int i = 1;
                for (OWLOntologyChange change : changes) {
                    if (i++ > SHOW_LIMIT) {
                        break;
                    }
                    if (needInnerSerparator) {
                        sb.append(",");
                        sb.append("\n");
                    }
                    sb.append(change);
                    needInnerSerparator = true;
                }
                int remainings = changes.size() - SHOW_LIMIT;
                if (remainings > 0) {
                    sb.append("\n");
                    sb.append("... ");
                    sb.append("(").append(remainings).append(" more)");
                }
                needNewline = true;
            }
        }
        return sb.toString();
    }
}
